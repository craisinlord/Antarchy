package com.craisinlord.antarchy.neoforge.dev;

import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.compat.infinity.InfinityWarpResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.codexarchonic.infinity.access.MinecraftServerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev-only Infinite Dimensions fuzzer. Does nothing unless the JVM has {@code -Dantarchy.dimfuzz=true}.
 *
 * <p>On server start it logs in a mock player (NeoForge's game-test mock connection), reads
 * {@code <gamedir>/dimfuzz/job.json}, and for each dimension id warps the bot there through the same call
 * Rainbow Ants use, flies it along a straight line so new chunks load and mobs spawn, and records tick
 * times, slow-tick stack samples, stuck chunks, hangs and every WARN/ERROR log line to
 * {@code dimfuzz/results.jsonl}. The pack-side driver ({@code docs/tools/dimfuzz/run.py} in the
 * Integrated Boogaloo instance) writes the job, restarts the server between batches and triages results.
 */
public final class DimFuzz {
    public static final String PROPERTY = "antarchy.dimfuzz";
    private static final Logger LOG = LoggerFactory.getLogger("AntarchyDimFuzz");
    private static final Gson GSON = new Gson();

    private static Path dir;
    private static Job job;
    private static MinecraftServer server;
    private static ServerPlayer bot;
    private static EmbeddedChannel botChannel;
    private static Thread serverThread;
    private static final Deque<Long> queue = new ArrayDeque<>();
    private static volatile Run current;
    private static volatile long tickStart;
    /** End of the last finished tick; a freeze between ticks (task queue, chunk waits) counts from here. */
    private static volatile long lastTickEnd;
    private static int idleTicks;
    private static boolean finished;

    private DimFuzz() {
    }

    public static void initIfEnabled() {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }
        LOG.warn("Dimension fuzzer enabled");
        NeoForge.EVENT_BUS.addListener(DimFuzz::onServerStarted);
        NeoForge.EVENT_BUS.addListener(DimFuzz::onTickPre);
        NeoForge.EVENT_BUS.addListener(DimFuzz::onTickPost);
        NeoForge.EVENT_BUS.addListener(DimFuzz::onStopping);
    }

    // ---------------------------------------------------------------- job

    static final class Job {
        List<Long> ids = new ArrayList<>();
        int dwellSeconds = 45;
        int warpTimeoutSeconds = 40;
        int stuckSeconds = 20;
        int stepBlocks = 16;
        int stepTicks = 40;
        int hangSeconds = 20;
        int viewDistance = 10;
        boolean exitWhenDone = true;
    }

    enum Phase { WARP, ACTIVE }

    static final class Run {
        final long id;
        final ResourceLocation dim;
        final long startNanos = System.nanoTime();
        Phase phase = Phase.WARP;
        long phaseStartNanos = startNanos;
        long warpNanos;
        int activeTicks;
        int ticksSinceStep;
        long waitingSince;
        int steps;
        double x;
        double z;
        float maxChunkWaitMs;
        String status = "ok";
        String detail;
        final List<Float> tickMs = new ArrayList<>();
        final Map<String, LogSig> logs = new ConcurrentHashMap<>();
        final Map<String, AtomicInteger> slowStacks = new ConcurrentHashMap<>();
        final AtomicInteger slowSamples = new AtomicInteger();
        volatile boolean hangDumped;

        Run(long id) {
            this.id = id;
            this.dim = ResourceLocation.fromNamespaceAndPath("infinity", "generated_" + id);
        }
    }

    static final class LogSig {
        final String level;
        final String logger;
        final String thread;
        final String sample;
        final List<String> stack;
        final AtomicInteger count = new AtomicInteger();

        LogSig(String level, String logger, String thread, String sample, List<String> stack) {
            this.level = level;
            this.logger = logger;
            this.thread = thread;
            this.sample = sample;
            this.stack = stack;
        }
    }

    // ---------------------------------------------------------------- lifecycle

    private static void onServerStarted(ServerStartedEvent event) {
        server = event.getServer();
        serverThread = Thread.currentThread();
        dir = FMLPaths.GAMEDIR.get().resolve("dimfuzz");
        try {
            Files.createDirectories(dir.resolve("hangs"));
            Path jobFile = dir.resolve("job.json");
            job = Files.exists(jobFile) ? GSON.fromJson(Files.readString(jobFile), Job.class) : new Job();
        } catch (IOException | RuntimeException e) {
            LOG.error("Could not read dimfuzz/job.json", e);
            job = new Job();
        }
        queue.addAll(job.ids);
        installLogCapture();
        startSampler();
        if (((MinecraftServerAccess) server).infinity$needsInvocation()) {
            LOG.error("Infinite Dimensions needs invocation (config/infinity/modular/invocation.lock missing); nothing will warp");
        }
        spawnBot();
        JsonObject o = new JsonObject();
        o.addProperty("event", "server_started");
        o.addProperty("queued", queue.size());
        write(o);
        LOG.warn("Dimension fuzzer: {} dimensions queued", queue.size());
    }

    private static void onStopping(ServerStoppingEvent event) {
        Run run = current;
        if (run != null) {
            run.status = "server_stopped";
            finishRun(run);
        }
        current = null;
    }

    private static void spawnBot() {
        ServerLevel overworld = server.overworld();
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("antarchy-dimfuzz".getBytes(StandardCharsets.UTF_8)), "DimFuzzBot");
        ClientInformation info = new ClientInformation("en_us", job.viewDistance, ChatVisiblity.FULL, true, 0x7F,
                HumanoidArm.RIGHT, false, true);
        CommonListenerCookie cookie = new CommonListenerCookie(profile, 0, info, false);
        bot = new ServerPlayer(server, overworld, profile, info);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        botChannel = new EmbeddedChannel(connection);
        NetworkRegistry.configureMockConnection(connection);
        server.getPlayerList().placeNewPlayer(connection, bot, cookie);
        bot.setGameMode(GameType.CREATIVE);
        bot.setInvulnerable(true);
        bot.getAbilities().flying = true;
        bot.onUpdateAbilities();
        returnToOverworld();
    }

    private static void returnToOverworld() {
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        bot.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, 0f, 0f);
    }

    // ---------------------------------------------------------------- ticking

    private static void onTickPre(ServerTickEvent.Pre event) {
        tickStart = System.nanoTime();
    }

    private static void onTickPost(ServerTickEvent.Post event) {
        long end = System.nanoTime();
        long start = tickStart;
        // Keep tickStart set while our own handler runs, so a freeze inside requestWarp (dimension creation)
        // is still sampled and dumped.
        try {
            afterTick(start, end);
        } finally {
            lastTickEnd = System.nanoTime();
            tickStart = 0;
        }
    }

    private static void afterTick(long start, long end) {
        if (bot == null || finished) {
            return;
        }
        botChannel.outboundMessages().clear();
        try {
            bot.doTick();
        } catch (RuntimeException e) {
            LOG.error("Bot tick failed", e);
        }
        Run run = current;
        if (run != null) {
            run.tickMs.add((end - start) / 1_000_000f);
            try {
                step(run);
            } catch (RuntimeException e) {
                LOG.error("Fuzzer step failed in {}", run.dim, e);
                run.status = "fuzzer_error";
                run.detail = e.toString();
                finishRun(run);
            }
            return;
        }
        if (idleTicks++ < 40) {
            return;
        }
        idleTicks = 0;
        Long next = queue.poll();
        if (next == null) {
            finished = true;
            JsonObject o = new JsonObject();
            o.addProperty("event", "job_done");
            write(o);
            if (job.exitWhenDone) {
                server.halt(false);
            }
            return;
        }
        startRun(next);
    }

    private static void startRun(long id) {
        Run run = new Run(id);
        JsonObject o = new JsonObject();
        o.addProperty("event", "begin");
        o.addProperty("id", id);
        write(o);
        current = run;
        InfinityWarpResult result = InfinityCompat.get().requestWarp(bot, run.dim);
        if (result == InfinityWarpResult.REJECTED || result == InfinityWarpResult.FAILED_GENERATION) {
            run.status = "warp_" + result.name().toLowerCase();
            finishRun(run);
        }
    }

    private static void step(Run run) {
        long now = System.nanoTime();
        ServerLevel level = bot.serverLevel();
        if (run.phase == Phase.WARP) {
            if (level.dimension().location().equals(run.dim)) {
                run.warpNanos = now - run.startNanos;
                run.phase = Phase.ACTIVE;
                run.phaseStartNanos = now;
                run.x = bot.getX();
                run.z = bot.getZ();
                run.tickMs.clear();
            } else if (now - run.startNanos > job.warpTimeoutSeconds * 1_000_000_000L) {
                run.status = "warp_timeout";
                run.detail = "bot still in " + level.dimension().location();
                finishRun(run);
            }
            return;
        }

        run.activeTicks++;
        // The bot only moves into a chunk once it has loaded asynchronously, like a walking player sees it.
        // Teleporting straight into an ungenerated chunk forced a synchronous load and showed up as fake lag.
        int cx = Mth.floor(run.x) >> 4;
        int cz = Mth.floor(run.z) >> 4;
        boolean loaded = level.getChunkSource().getChunkNow(cx, cz) != null;
        if (run.waitingSince != 0) {
            if (!loaded) {
                net.minecraft.world.level.ChunkPos target = new net.minecraft.world.level.ChunkPos(cx, cz);
                level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.UNKNOWN, target, 1, target);
            }
            if (loaded) {
                run.maxChunkWaitMs = Math.max(run.maxChunkWaitMs, (now - run.waitingSince) / 1_000_000f);
                run.waitingSince = 0;
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(run.x), Mth.floor(run.z));
                double y = top > level.getMinBuildHeight() ? Math.min(top + 2, level.getMaxBuildHeight() - 2) : bot.getY();
                bot.teleportTo(level, run.x, y, run.z, bot.getYRot(), bot.getXRot());
                run.ticksSinceStep = 0;
            } else if (now - run.waitingSince > job.stuckSeconds * 1_000_000_000L) {
                run.status = "stuck_chunk";
                run.detail = "chunk " + cx + "," + cz + " not loaded after " + job.stuckSeconds + " s";
                dumpThreads(run, "stuck");
                finishRun(run);
                return;
            }
        } else if (++run.ticksSinceStep >= job.stepTicks) {
            run.x += job.stepBlocks;
            run.steps++;
            run.waitingSince = now;
        }

        boolean dwellDone = run.activeTicks >= job.dwellSeconds * 20;
        boolean wallDone = now - run.phaseStartNanos > job.dwellSeconds * 3L * 1_000_000_000L;
        if (dwellDone || wallDone) {
            if (!dwellDone) {
                run.status = "too_slow";
                run.detail = run.activeTicks + " ticks in " + job.dwellSeconds * 3 + " s";
            }
            finishRun(run);
        }
    }

    private static void finishRun(Run run) {
        if (current != run) {
            return;
        }
        current = null;
        JsonObject o = new JsonObject();
        o.addProperty("event", "end");
        o.addProperty("id", run.id);
        o.addProperty("status", run.status);
        if (run.detail != null) {
            o.addProperty("detail", run.detail);
        }
        o.addProperty("warp_ms", run.warpNanos / 1_000_000L);
        o.addProperty("active_ticks", run.activeTicks);
        o.addProperty("steps", run.steps);
        o.addProperty("max_chunk_wait_ms", Math.round(run.maxChunkWaitMs));

        float[] ms = new float[run.tickMs.size()];
        for (int i = 0; i < ms.length; i++) {
            ms[i] = run.tickMs.get(i);
        }
        Arrays.sort(ms);
        double sum = 0;
        int over50 = 0;
        int over250 = 0;
        for (float v : ms) {
            sum += v;
            if (v > 50) over50++;
            if (v > 250) over250++;
        }
        o.addProperty("mspt_avg", ms.length == 0 ? 0 : Math.round(sum / ms.length * 10) / 10.0);
        o.addProperty("mspt_p95", ms.length == 0 ? 0 : ms[(int) (ms.length * 0.95)]);
        o.addProperty("mspt_max", ms.length == 0 ? 0 : ms[ms.length - 1]);
        o.addProperty("ticks_over_50ms", over50);
        o.addProperty("ticks_over_250ms", over250);

        ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, run.dim));
        if (level != null) {
            o.addProperty("loaded_chunks", level.getChunkSource().getLoadedChunksCount());
            Map<String, Integer> byType = new HashMap<>();
            int total = 0;
            Map<String, Integer> items = new HashMap<>();
            for (Entity e : level.getAllEntities()) {
                byType.merge(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString(), 1, Integer::sum);
                if (e instanceof net.minecraft.world.entity.item.ItemEntity item) {
                    items.merge(BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString(), 1, Integer::sum);
                }
                total++;
            }
            // Thousands of dropped items usually mean a terrain block that can't survive and pops off.
            JsonObject topItems = new JsonObject();
            items.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(8)
                    .forEach(e -> topItems.addProperty(e.getKey(), e.getValue()));
            o.add("item_types", topItems);
            o.addProperty("entities", total);
            JsonObject top = new JsonObject();
            byType.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(8)
                    .forEach(e -> top.addProperty(e.getKey(), e.getValue()));
            o.add("entity_types", top);
        }

        JsonArray logs = new JsonArray();
        run.logs.values().stream().sorted((a, b) -> b.count.get() - a.count.get()).limit(60).forEach(s -> {
            JsonObject l = new JsonObject();
            l.addProperty("level", s.level);
            l.addProperty("logger", s.logger);
            l.addProperty("thread", s.thread);
            l.addProperty("count", s.count.get());
            l.addProperty("message", s.sample);
            if (s.stack != null) {
                JsonArray st = new JsonArray();
                s.stack.forEach(st::add);
                l.add("stack", st);
            }
            logs.add(l);
        });
        o.add("logs", logs);

        o.addProperty("slow_samples", run.slowSamples.get());
        JsonArray slow = new JsonArray();
        run.slowStacks.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).limit(6)
                .forEach(e -> {
                    JsonObject s = new JsonObject();
                    s.addProperty("samples", e.getValue().get());
                    JsonArray frames = new JsonArray();
                    for (String f : e.getKey().split("\n")) {
                        frames.add(f);
                    }
                    s.add("frames", frames);
                    slow.add(s);
                });
        o.add("slow_stacks", slow);
        write(o);

        if (bot != null && !bot.serverLevel().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            try {
                returnToOverworld();
            } catch (RuntimeException e) {
                LOG.error("Could not return the bot to the overworld", e);
            }
        }
        idleTicks = 0;
    }

    // ---------------------------------------------------------------- slow-tick sampler and hang dumps

    private static void startSampler() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
                long start = tickStart;
                Run run = current;
                if (start == 0) {
                    start = lastTickEnd;
                }
                if (start == 0 || run == null || serverThread == null) {
                    continue;
                }
                long elapsed = System.nanoTime() - start;
                // Between ticks the server idles up to 50 ms, so anything past 100 ms is a late or stuck thread.
                if (elapsed < 100_000_000L) {
                    continue;
                }
                StackTraceElement[] st = serverThread.getStackTrace();
                StringBuilder key = new StringBuilder();
                for (int i = 0; i < Math.min(st.length, 40); i++) {
                    if (i > 0) key.append('\n');
                    key.append(st[i].getClassName()).append('.').append(st[i].getMethodName());
                }
                run.slowStacks.computeIfAbsent(key.toString(), k -> new AtomicInteger()).incrementAndGet();
                run.slowSamples.incrementAndGet();
                if (!run.hangDumped && elapsed > job.hangSeconds * 1_000_000_000L) {
                    run.hangDumped = true;
                    dumpThreads(run, "hang");
                    JsonObject o = new JsonObject();
                    o.addProperty("event", "hang");
                    o.addProperty("id", run.id);
                    o.addProperty("tick_seconds", elapsed / 1_000_000_000L);
                    write(o);
                }
            }
        }, "AntarchyDimFuzz-Sampler");
        t.setDaemon(true);
        t.start();
    }

    private static void dumpThreads(Run run, String kind) {
        Path file = dir.resolve("hangs").resolve(run.id + "-" + kind + ".txt");
        StringBuilder sb = new StringBuilder();
        for (ThreadInfo ti : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
            sb.append('"').append(ti.getThreadName()).append("\" ").append(ti.getThreadState());
            if (ti.getLockName() != null) sb.append(" on ").append(ti.getLockName());
            if (ti.getLockOwnerName() != null) sb.append(" owned by \"").append(ti.getLockOwnerName()).append('"');
            sb.append('\n');
            for (StackTraceElement e : ti.getStackTrace()) {
                sb.append("    at ").append(e).append('\n');
            }
            sb.append('\n');
        }
        try {
            Files.writeString(file, sb.toString());
        } catch (IOException e) {
            LOG.error("Could not write thread dump", e);
        }
    }

    // ---------------------------------------------------------------- log capture

    private static void installLogCapture() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration cfg = ctx.getConfiguration();
        AbstractAppender appender = new AbstractAppender("AntarchyDimFuzz", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                Run run = current;
                if (run == null || "AntarchyDimFuzz".equals(event.getLoggerName())) {
                    return;
                }
                if (!event.getLevel().isMoreSpecificThan(org.apache.logging.log4j.Level.WARN)) {
                    return;
                }
                Throwable thrown = event.getThrown();
                String format = event.getMessage().getFormat();
                if (format == null) format = event.getMessage().getFormattedMessage();
                String norm = format == null ? "" : format.replaceAll("\\d+", "#");
                if (norm.length() > 200) norm = norm.substring(0, 200);
                List<String> stack = null;
                if (thrown != null) {
                    Throwable root = thrown;
                    while (root.getCause() != null && root.getCause() != root) root = root.getCause();
                    stack = new ArrayList<>();
                    stack.add(root.toString());
                    for (StackTraceElement e : root.getStackTrace()) {
                        if (stack.size() > 30) break;
                        stack.add(e.toString());
                    }
                    StackTraceElement[] rs = root.getStackTrace();
                    norm += " | " + root.getClass().getName() + (rs.length > 0 ? " @ " + rs[0].getClassName() + "." + rs[0].getMethodName() : "");
                }
                String key = event.getLevel() + " " + event.getLoggerName() + " " + norm;
                String msg = event.getMessage().getFormattedMessage();
                if (msg != null && msg.length() > 600) msg = msg.substring(0, 600);
                List<String> finalStack = stack;
                String finalMsg = msg;
                run.logs.computeIfAbsent(key, k -> new LogSig(event.getLevel().name(), event.getLoggerName(),
                        event.getThreadName(), finalMsg, finalStack)).count.incrementAndGet();
            }
        };
        appender.start();
        cfg.addAppender(appender);
        cfg.getRootLogger().addAppender(appender, org.apache.logging.log4j.Level.WARN, null);
        ctx.updateLoggers();
    }

    // ---------------------------------------------------------------- output

    private static synchronized void write(JsonObject o) {
        o.addProperty("t", System.currentTimeMillis());
        try (Writer w = Files.newBufferedWriter(dir.resolve("results.jsonl"), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(GSON.toJson(o));
            w.write('\n');
        } catch (IOException e) {
            LOG.error("Could not write dimfuzz results", e);
        }
    }

    private static final class Mth {
        static int floor(double v) {
            return net.minecraft.util.Mth.floor(v);
        }
    }
}
