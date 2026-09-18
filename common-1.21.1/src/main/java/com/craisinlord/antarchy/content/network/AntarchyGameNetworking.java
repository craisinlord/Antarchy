package com.craisinlord.antarchy.content.network;

import net.minecraft.core.BlockPos;
import java.util.function.Consumer;

/** Small Antarchy game-progress bridge over the AntOS computer session. */
public final class AntarchyGameNetworking {
    private static Consumer<AntarchyGamePayload> sender = payload -> { };
    private AntarchyGameNetworking() { }

    public static void setSender(Consumer<AntarchyGamePayload> sender) { AntarchyGameNetworking.sender = sender; }
    public static void requestBasiliskState(BlockPos pos) { send(pos, AntarchyGamePayload.BASILISK_STATE, ""); }
    public static void saveBasiliskScore(BlockPos pos, int score) { send(pos, AntarchyGamePayload.BASILISK_STATE, Integer.toString(Math.max(0, score))); }
    public static void requestAntmanState(BlockPos pos) { send(pos, AntarchyGamePayload.ANTMAN_STATE, ""); }
    public static void saveAntmanScore(BlockPos pos, int score) { send(pos, AntarchyGamePayload.ANTMAN_STATE, Integer.toString(Math.max(0, score))); }

    private static void send(BlockPos pos, int action, String value) { sender.accept(new AntarchyGamePayload(pos, action, value)); }
}
