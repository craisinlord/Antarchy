package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public final class ElythiaGenProfiler {
    public enum Stage {
        STRUCTURES("structure starts"),
        BIOMES("biomes"),
        NOISE("noise"),
        SURFACE("surface"),
        FEATURES("features"),
        POST_PROCESS("post-process");

        private final String label;

        Stage(String label) {
            this.label = label;
        }
    }

    private static final long SLOW_STAGE_NANOS = 100_000_000L;
    private static final long SLOW_DETAIL_NANOS = 50_000_000L;
    private static final int DETAIL_LINES = 10;
    private static final Map<String, StageStats[]> STATS = new ConcurrentHashMap<>();
    private static final Map<String, StageStats> DETAILS = new ConcurrentHashMap<>();
    private static final LongAdder AQUIFER_CALLS = new LongAdder();
    private static final LongAdder AQUIFER_VANILLA = new LongAdder();
    private static final LongAdder LUMEN_BLOCKS = new LongAdder();
    private static final LongAdder FLUID_UPDATES = new LongAdder();
    private static volatile boolean enabled;
    private static volatile long startedAt;

    private ElythiaGenProfiler() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void start() {
        STATS.clear();
        DETAILS.clear();
        AQUIFER_CALLS.reset();
        AQUIFER_VANILLA.reset();
        LUMEN_BLOCKS.reset();
        FLUID_UPDATES.reset();
        startedAt = System.nanoTime();
        enabled = true;
    }

    public static void stop() {
        enabled = false;
    }

    public static long begin() {
        return enabled ? System.nanoTime() : 0L;
    }

    public static void end(long start, ChunkGenerator generator, Stage stage, ChunkAccess chunk) {
        if (start != 0L) {
            record(label(generator), stage, System.nanoTime() - start, chunk.getPos());
        }
    }

    public static void end(long start, String label, Stage stage, ChunkPos pos) {
        if (start != 0L) {
            record(label, stage, System.nanoTime() - start, pos);
        }
    }

    public static void endDetail(long start, String kind, String id, ChunkPos pos) {
        if (start == 0L) {
            return;
        }
        long nanos = System.nanoTime() - start;
        StageStats stats = DETAILS.computeIfAbsent(kind + " " + id, key -> new StageStats());
        stats.count.increment();
        stats.nanos.add(nanos);
        stats.max.accumulateAndGet(nanos, Math::max);
        if (nanos >= SLOW_DETAIL_NANOS) {
            Antarchy.LOGGER.info("[Antarchy] Slow {} {} at chunk {}, {}: {} ms", kind, id, pos.x, pos.z,
                    String.format(Locale.ROOT, "%.1f", nanos / 1_000_000.0D));
        }
    }

    public static void countAquifer(boolean vanilla) {
        if (vanilla) {
            AQUIFER_VANILLA.increment();
        } else {
            AQUIFER_CALLS.increment();
        }
    }

    public static void countFluid(boolean lumen, boolean scheduled) {
        if (lumen) {
            LUMEN_BLOCKS.increment();
        }
        if (scheduled) {
            FLUID_UPDATES.increment();
        }
    }

    public static String label(ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGenerator noise) {
            return noise.generatorSettings().unwrapKey().map(key -> key.location().toString()).orElse("noise");
        }
        return generator.getClass().getSimpleName();
    }

    private static void record(String label, Stage stage, long nanos, ChunkPos pos) {
        StageStats stats = STATS.computeIfAbsent(label, key -> newStats())[stage.ordinal()];
        stats.count.increment();
        stats.nanos.add(nanos);
        stats.max.accumulateAndGet(nanos, Math::max);
        if (nanos >= SLOW_STAGE_NANOS) {
            Antarchy.LOGGER.info("[Antarchy] Slow {} {} at chunk {}, {}: {} ms", label, stage.label, pos.x, pos.z,
                    String.format(Locale.ROOT, "%.1f", nanos / 1_000_000.0D));
        }
    }

    private static StageStats[] newStats() {
        StageStats[] stats = new StageStats[Stage.values().length];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new StageStats();
        }
        return stats;
    }

    public static List<String> report() {
        List<String> lines = new ArrayList<>();
        double seconds = (System.nanoTime() - startedAt) / 1_000_000_000.0D;
        lines.add(String.format(Locale.ROOT, "Generation profile over %.1f s%s", seconds, enabled ? " (running)" : ""));
        STATS.forEach((label, stages) -> {
            long chunks = stages[Stage.NOISE.ordinal()].count.sum();
            lines.add(String.format(Locale.ROOT, "%s: %d chunks (%.1f/s)", label, chunks, chunks / Math.max(seconds, 0.001D)));
            for (Stage stage : Stage.values()) {
                StageStats stats = stages[stage.ordinal()];
                long count = stats.count.sum();
                if (count == 0L) {
                    continue;
                }
                lines.add(String.format(Locale.ROOT, "  %s: avg %.2f ms, max %.1f ms, total %.1f s over %d",
                        stage.label,
                        stats.nanos.sum() / (double) count / 1_000_000.0D,
                        stats.max.get() / 1_000_000.0D,
                        stats.nanos.sum() / 1_000_000_000.0D,
                        count));
            }
        });
        StageStats[] elythia = STATS.get("antarchy:elythia");
        long elythiaChunks = elythia == null ? 0L : elythia[Stage.NOISE.ordinal()].count.sum();
        long calls = AQUIFER_CALLS.sum();
        if (calls > 0L) {
            lines.add(String.format(Locale.ROOT, "Elythia aquifer: %d calls, %.2f%% sent to vanilla", calls, 100.0D * AQUIFER_VANILLA.sum() / calls));
        }
        if (elythiaChunks > 0L) {
            lines.add(String.format(Locale.ROOT, "Elythia rivers: %.1f lumen blocks and %.1f fluid updates per chunk",
                    LUMEN_BLOCKS.sum() / (double) elythiaChunks, FLUID_UPDATES.sum() / (double) elythiaChunks));
        }
        if (!DETAILS.isEmpty()) {
            lines.add("Top features and structures by total time:");
            DETAILS.entrySet().stream()
                    .sorted(Comparator.comparingLong((Map.Entry<String, StageStats> entry) -> entry.getValue().nanos.sum()).reversed())
                    .limit(DETAIL_LINES)
                    .forEach(entry -> {
                        StageStats stats = entry.getValue();
                        long count = stats.count.sum();
                        lines.add(String.format(Locale.ROOT, "  %s: total %.1f s, avg %.2f ms, max %.1f ms over %d",
                                entry.getKey(),
                                stats.nanos.sum() / 1_000_000_000.0D,
                                stats.nanos.sum() / (double) count / 1_000_000.0D,
                                stats.max.get() / 1_000_000.0D,
                                count));
                    });
        }
        if (STATS.isEmpty()) {
            lines.add("No chunks generated yet; fly into new terrain");
        }
        return lines;
    }

    private static final class StageStats {
        private final LongAdder count = new LongAdder();
        private final LongAdder nanos = new LongAdder();
        private final AtomicLong max = new AtomicLong();
    }
}
