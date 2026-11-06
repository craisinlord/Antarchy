package com.craisinlord.antarchy.content.network;

import net.minecraft.core.BlockPos;

public final class AntarchyGameNetworking {
    private AntarchyGameNetworking() { }

    public static void requestBasiliskState(BlockPos pos) { send(pos, AntarchyGamePayload.BASILISK_STATE, ""); }
    public static void saveBasiliskScore(BlockPos pos, int score) { send(pos, AntarchyGamePayload.BASILISK_STATE, Integer.toString(Math.max(0, score))); }
    public static void requestAntmanState(BlockPos pos) { send(pos, AntarchyGamePayload.ANTMAN_STATE, ""); }
    public static void saveAntmanScore(BlockPos pos, int score) { send(pos, AntarchyGamePayload.ANTMAN_STATE, Integer.toString(Math.max(0, score))); }

    private static void send(BlockPos pos, int action, String value) { AntarchyNetwork.sendToServer(new AntarchyGamePayload(pos, action, value)); }
}
