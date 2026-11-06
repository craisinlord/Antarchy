package com.craisinlord.antarchy.content.migration;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;

public final class DimensionResetManager {
    public static final List<String> DIMENSIONS = List.of("elythia", "thoraxis", "cavaryn");
    public static final String COMMAND = "antarchyreset";
    private static final String STATE_FILE = "antarchy_world.json";
    private static final int TERRAIN_VERSION = 2;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static MinecraftServer currentServer;
    private static State currentState;

    private DimensionResetManager() {
    }

    public static void onServerGoingToStart(MinecraftServer server) {
        currentServer = server;
        Path root = server.getWorldPath(LevelResource.ROOT);
        Path stateFile = root.resolve(STATE_FILE);
        State state = Files.exists(stateFile) ? read(stateFile) : detect(root);
        currentState = state;

        if (!state.pending.isEmpty()) {
            List<String> reset = new ArrayList<>();
            for (String dimension : state.pending) {
                if (!DIMENSIONS.contains(dimension)) {
                    continue;
                }
                Path folder = dimensionFolder(root, dimension);
                if (deleteRecursively(folder)) {
                    reset.add(dimension);
                    Antarchy.LOGGER.warn("[Antarchy] Reset {}", folder);
                } else {
                    Antarchy.LOGGER.error("[Antarchy] Couldn't fully delete {}, will retry {} next start", folder, dimension);
                }
            }
            if (!reset.isEmpty()) {
                state.epoch++;
                for (String dimension : reset) {
                    state.legacy.remove(dimension);
                    state.selected.remove(dimension);
                    state.pending.remove(dimension);
                    state.resetEpochs.put(dimension, state.epoch);
                    state.portalCleanup.add(dimension);
                }
            }
        }

        write(server, state);

        if (!state.legacy.isEmpty() && !state.dismissed) {
            Antarchy.LOGGER.warn("[Antarchy] Old terrain in: {}. To reset, run '{} toggle <{}>', then '{} confirm', then restart. '{} dismiss' hides this.",
                    String.join(", ", state.legacy), COMMAND, String.join("|", state.legacy), COMMAND, COMMAND);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        State state = state(player.level().getServer());
        if (state == null) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        boolean changed = false;

        if (!state.portalCleanup.isEmpty()) {
            PortalGunSavedData.clearAntarchyDimensions(server, state.portalCleanup);
            state.portalCleanup.clear();
            changed = true;
        }

        String key = player.getUUID().toString();
        int playerEpoch = state.playerEpochs.getOrDefault(key, 0);
        Level level = player.level();
        String dimension = level.dimension().identifier().getPath();
        if (Antarchy.MODID.equals(level.dimension().identifier().getNamespace())
                && state.resetEpochs.getOrDefault(dimension, 0) > playerEpoch) {
            relocate(player, dimension);
        }
        if (playerEpoch != state.epoch) {
            state.playerEpochs.put(key, state.epoch);
            changed = true;
        }

        if (changed) {
            write(server, state);
        }

        if (!state.legacy.isEmpty() && !state.dismissed && canManage(player.createCommandSourceStack())) {
            sendPrompt(player.createCommandSourceStack(), state);
        }
    }

    public static boolean canManage(CommandSourceStack source) {
        if (Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source)) {
            return true;
        }
        return source.getEntity() instanceof ServerPlayer player
                && source.getServer().isSingleplayerOwner(player.nameAndId());
    }

    public static int status(CommandSourceStack source) {
        State state = state(source.getServer());
        if (state == null || state.legacy.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("message.antarchy.dimension_reset.no_legacy"), false);
            return 0;
        }
        sendPrompt(source, state);
        return 1;
    }

    public static int toggle(CommandSourceStack source, String dimension) {
        State state = state(source.getServer());
        if (state == null || !state.legacy.contains(dimension)) {
            source.sendFailure(Component.translatable("message.antarchy.dimension_reset.not_legacy", dimensionName(dimension)));
            return 0;
        }
        if (!state.selected.remove(dimension)) {
            state.selected.add(dimension);
        }
        write(source.getServer(), state);
        sendPrompt(source, state);
        return 1;
    }

    public static int confirm(CommandSourceStack source) {
        State state = state(source.getServer());
        if (state == null || state.legacy.isEmpty()) {
            source.sendFailure(Component.translatable("message.antarchy.dimension_reset.no_legacy"));
            return 0;
        }
        Set<String> chosen = new LinkedHashSet<>();
        for (String dimension : DIMENSIONS) {
            if (state.selected.contains(dimension) && state.legacy.contains(dimension)) {
                chosen.add(dimension);
            }
        }
        if (chosen.isEmpty()) {
            source.sendFailure(Component.translatable("message.antarchy.dimension_reset.none_selected"));
            return 0;
        }
        state.pending.clear();
        state.pending.addAll(chosen);
        write(source.getServer(), state);
        Antarchy.LOGGER.warn("[Antarchy] {} queued a reset of {} for next start",
                source.getTextName(), String.join(", ", chosen));
        Component names = joinNames(chosen);
        source.sendSuccess(() -> Component.translatable("message.antarchy.dimension_reset.confirmed", names)
                .withStyle(ChatFormatting.GOLD), true);
        return chosen.size();
    }

    public static int cancel(CommandSourceStack source) {
        State state = state(source.getServer());
        if (state == null || state.pending.isEmpty()) {
            source.sendFailure(Component.translatable("message.antarchy.dimension_reset.nothing_queued"));
            return 0;
        }
        state.pending.clear();
        write(source.getServer(), state);
        Antarchy.LOGGER.info("[Antarchy] {} cancelled the reset", source.getTextName());
        source.sendSuccess(() -> Component.translatable("message.antarchy.dimension_reset.cancelled"), true);
        return 1;
    }

    public static int dismiss(CommandSourceStack source) {
        State state = state(source.getServer());
        if (state == null) {
            return 0;
        }
        state.dismissed = true;
        write(source.getServer(), state);
        source.sendSuccess(() -> Component.translatable("message.antarchy.dimension_reset.dismissed", "/" + COMMAND), false);
        return 1;
    }

    private static void sendPrompt(CommandSourceStack source, State state) {
        source.sendSystemMessage(Component.translatable("message.antarchy.dimension_reset.header").withStyle(ChatFormatting.GOLD));

        MutableComponent toggles = Component.empty();
        boolean first = true;
        for (String dimension : DIMENSIONS) {
            if (!state.legacy.contains(dimension)) {
                continue;
            }
            if (!first) {
                toggles.append(Component.literal(" "));
            }
            first = false;
            boolean selected = state.selected.contains(dimension);
            toggles.append(Component.translatable(
                            selected ? "message.antarchy.dimension_reset.toggle_on" : "message.antarchy.dimension_reset.toggle_off",
                            dimensionName(dimension))
                    .withStyle(style -> style
                            .withColor(selected ? ChatFormatting.GREEN : ChatFormatting.RED)
                            .withClickEvent(new ClickEvent.RunCommand("/" + COMMAND + " toggle " + dimension))
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.translatable("message.antarchy.dimension_reset.toggle_hover", dimensionName(dimension))))));
        }
        source.sendSystemMessage(toggles);

        MutableComponent actions = Component.empty()
                .append(button("message.antarchy.dimension_reset.confirm", "message.antarchy.dimension_reset.confirm_hover",
                        "confirm", ChatFormatting.DARK_RED))
                .append(Component.literal(" "));
        if (!state.pending.isEmpty()) {
            actions.append(button("message.antarchy.dimension_reset.cancel", "message.antarchy.dimension_reset.cancel_hover",
                    "cancel", ChatFormatting.YELLOW)).append(Component.literal(" "));
        }
        actions.append(button("message.antarchy.dimension_reset.dismiss", "message.antarchy.dimension_reset.dismiss_hover",
                "dismiss", ChatFormatting.GRAY));
        source.sendSystemMessage(actions);

        if (!state.pending.isEmpty()) {
            source.sendSystemMessage(Component.translatable("message.antarchy.dimension_reset.queued", joinNames(state.pending))
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    private static Component button(String labelKey, String hoverKey, String subcommand, ChatFormatting color) {
        return Component.translatable(labelKey).withStyle(style -> style
                .withColor(color)
                .withClickEvent(new ClickEvent.RunCommand("/" + COMMAND + " " + subcommand))
                .withHoverEvent(new HoverEvent.ShowText(
                        Component.translatable(hoverKey, "/" + COMMAND))));
    }

    private static Component dimensionName(String dimension) {
        return Component.translatable("message.antarchy.dimension_reset.dimension." + dimension);
    }

    private static Component joinNames(Set<String> dimensions) {
        MutableComponent names = Component.empty();
        boolean first = true;
        for (String dimension : DIMENSIONS) {
            if (!dimensions.contains(dimension)) {
                continue;
            }
            if (!first) {
                names.append(Component.literal(", "));
            }
            first = false;
            names.append(dimensionName(dimension));
        }
        return names;
    }

    private static void relocate(ServerPlayer player, String dimension) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos target = findSafeSpot(level, player.getBlockX(), player.getBlockZ());
        ServerLevel targetLevel = level;
        if (target == null) {
            targetLevel = level.getServer().overworld();
            BlockPos spawn = targetLevel.getRespawnData().pos();
            targetLevel.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
            target = targetLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
        }
        player.fallDistance = 0.0F;
        player.teleportTo(targetLevel, target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, Set.of(), player.getYRot(), player.getXRot(), true);
        player.sendSystemMessage(Component.translatable("message.antarchy.dimension_reset.relocated", dimensionName(dimension))
                .withStyle(ChatFormatting.GOLD));
        Antarchy.LOGGER.info("[Antarchy] Moved {} out of reset dimension {} to {}", player.getName().getString(), dimension, target);
    }

    private static BlockPos findSafeSpot(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int minY = level.getMinY() + 1;
        int top = level.dimensionType().hasCeiling()
                ? level.getMinY() + level.dimensionType().logicalHeight() - 2
                : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        top = Math.min(top, level.getMaxY() - 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top; y >= minY; y--) {
            BlockState floor = level.getBlockState(pos.set(x, y - 1, z));
            if (!floor.isSolid() || !floor.getFluidState().isEmpty()) {
                continue;
            }
            if (isOpen(level, pos.set(x, y, z)) && isOpen(level, pos.set(x, y + 1, z))) {
                return new BlockPos(x, y, z);
            }
        }
        return null;
    }

    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isSolid() && state.getFluidState().isEmpty();
    }

    private static State state(MinecraftServer server) {
        return server == currentServer ? currentState : null;
    }

    private static State detect(Path root) {
        State state = new State();
        for (String dimension : DIMENSIONS) {
            if (hasChunks(dimensionFolder(root, dimension).resolve("region"))) {
                state.legacy.add(dimension);
            }
        }
        if (!state.legacy.isEmpty()) {
            Antarchy.LOGGER.info("[Antarchy] Pre-2.3.0 world, old terrain in: {}", String.join(", ", state.legacy));
        }
        return state;
    }

    private static boolean hasChunks(Path regionFolder) {
        if (!Files.isDirectory(regionFolder)) {
            return false;
        }
        try (Stream<Path> files = Files.list(regionFolder)) {
            return files.anyMatch(file -> file.getFileName().toString().endsWith(".mca"));
        } catch (IOException e) {
            return false;
        }
    }

    private static Path dimensionFolder(Path root, String dimension) {
        return root.resolve("dimensions").resolve(Antarchy.MODID).resolve(dimension);
    }

    private static boolean deleteRecursively(Path folder) {
        if (!Files.exists(folder)) {
            return true;
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            List<Path> ordered = paths.sorted(Comparator.reverseOrder()).toList();
            boolean success = true;
            for (Path path : ordered) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    Antarchy.LOGGER.error("[Antarchy] Could not delete {}", path, e);
                    success = false;
                }
            }
            return success;
        } catch (IOException e) {
            Antarchy.LOGGER.error("[Antarchy] Could not walk {}", folder, e);
            return false;
        }
    }

    private static State read(Path file) {
        State state = new State();
        try {
            JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            state.terrainVersion = json.has("terrainVersion") ? json.get("terrainVersion").getAsInt() : TERRAIN_VERSION;
            state.dismissed = json.has("dismissed") && json.get("dismissed").getAsBoolean();
            state.epoch = json.has("epoch") ? json.get("epoch").getAsInt() : 0;
            readSet(json, "legacy", state.legacy);
            readSet(json, "selected", state.selected);
            readSet(json, "pending", state.pending);
            readSet(json, "portalCleanup", state.portalCleanup);
            readMap(json, "resetEpochs", state.resetEpochs);
            readMap(json, "playerEpochs", state.playerEpochs);
        } catch (IOException | RuntimeException e) {
            Antarchy.LOGGER.error("[Antarchy] Could not read {}; dimension reset state starts empty", file, e);
        }
        return state;
    }

    private static void write(MinecraftServer server, State state) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve(STATE_FILE);
        JsonObject json = new JsonObject();
        json.addProperty("terrainVersion", state.terrainVersion);
        json.addProperty("dismissed", state.dismissed);
        json.addProperty("epoch", state.epoch);
        json.add("legacy", toArray(state.legacy));
        json.add("selected", toArray(state.selected));
        json.add("pending", toArray(state.pending));
        json.add("portalCleanup", toArray(state.portalCleanup));
        json.add("resetEpochs", toObject(state.resetEpochs));
        json.add("playerEpochs", toObject(state.playerEpochs));
        try {
            Path temp = file.resolveSibling(STATE_FILE + ".tmp");
            Files.writeString(temp, GSON.toJson(json), StandardCharsets.UTF_8);
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Antarchy.LOGGER.error("[Antarchy] Could not write {}", file, e);
        }
    }

    private static void readSet(JsonObject json, String key, Set<String> target) {
        if (json.has(key) && json.get(key).isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray(key)) {
                target.add(element.getAsString());
            }
        }
    }

    private static void readMap(JsonObject json, String key, Map<String, Integer> target) {
        if (json.has(key) && json.get(key).isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(key).entrySet()) {
                target.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }
    }

    private static JsonArray toArray(Set<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private static JsonObject toObject(Map<String, Integer> values) {
        JsonObject object = new JsonObject();
        values.forEach(object::addProperty);
        return object;
    }

    private static final class State {
        private int terrainVersion = TERRAIN_VERSION;
        private boolean dismissed;
        private int epoch;
        private final Set<String> legacy = new LinkedHashSet<>();
        private final Set<String> selected = new LinkedHashSet<>();
        private final Set<String> pending = new LinkedHashSet<>();
        private final Set<String> portalCleanup = new LinkedHashSet<>();
        private final Map<String, Integer> resetEpochs = new HashMap<>();
        private final Map<String, Integer> playerEpochs = new HashMap<>();
    }
}
