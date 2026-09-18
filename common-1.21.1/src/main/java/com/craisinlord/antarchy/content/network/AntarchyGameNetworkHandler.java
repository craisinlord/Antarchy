package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.computer.AntarchyGameSavedData;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Handles Antarchy-specific game progress sent by AntOS game extensions. */
public final class AntarchyGameNetworkHandler {
    private static BiConsumer<ServerPlayer, AntarchyGameResultPayload> resultSender = (player, result) -> { };

    private AntarchyGameNetworkHandler() { }

    public static void setResultSender(BiConsumer<ServerPlayer, AntarchyGameResultPayload> sender) {
        resultSender = sender;
    }

    public static void handle(ServerPlayer player, AntarchyGamePayload payload) {
        BlockPos pos = payload.pos();
        if (payload.value().length() > 65536 || player.level().isClientSide || !player.serverLevel().hasChunkAt(pos)
                || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) return;

        var blockEntity = player.serverLevel().getBlockEntity(pos);
        if (!(blockEntity instanceof com.craisinlord.antos.content.block.entity.ComputerBlockEntity computer)
                || !computer.canUseFileSystem(player)) {
            send(player, payload, false, "", "unauthorized");
            return;
        }
        String key = AntarchyGameSavedData.key(player.serverLevel().dimension().location(), pos);
        AntarchyGameSavedData.GameState state = AntarchyGameSavedData.get(player.server, key);
        switch (payload.action()) {
            case AntarchyGamePayload.BASILISK_STATE -> basilisk(player, payload, state);
            case AntarchyGamePayload.ANTMAN_STATE -> antman(player, payload, state);
            default -> send(player, payload, false, "", "invalid");
        }
    }

    private static void basilisk(ServerPlayer player, AntarchyGamePayload payload, AntarchyGameSavedData.GameState state) {
        if (!payload.value().isBlank()) {
            Integer score = parseScore(payload.value());
            if (score == null) { send(player, payload, false, "", "invalid_score"); return; }
            state.basiliskScore = score;
            state.basiliskHighScore = Math.max(state.basiliskHighScore, score);
            AntarchyGameSavedData.changed(player.server);
        }
        send(player, payload, true, state.basiliskScore + "\0" + state.basiliskHighScore, "");
    }

    private static void antman(ServerPlayer player, AntarchyGamePayload payload, AntarchyGameSavedData.GameState state) {
        if (!payload.value().isBlank()) {
            Integer score = parseScore(payload.value());
            if (score == null) { send(player, payload, false, "", "invalid_score"); return; }
            state.antmanScore = score;
            state.antmanHighScore = Math.max(state.antmanHighScore, score);
            AntarchyGameSavedData.changed(player.server);
        }
        send(player, payload, true, state.antmanScore + "\0" + state.antmanHighScore, "");
    }

    private static Integer parseScore(String value) {
        try { return Math.min(1_000_000, Math.max(0, Integer.parseInt(value))); }
        catch (NumberFormatException ignored) { return null; }
    }

    private static void send(ServerPlayer player, AntarchyGamePayload payload, boolean success, String data, String error) {
        resultSender.accept(player, new AntarchyGameResultPayload(payload.pos(), success,
                payload.action() + "\0" + error + "\0" + data));
    }
}
