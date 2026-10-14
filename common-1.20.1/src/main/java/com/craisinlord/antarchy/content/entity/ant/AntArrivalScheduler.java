package com.craisinlord.antarchy.content.entity.ant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public final class AntArrivalScheduler {
    private static final TicketType<ChunkPos> ARRIVAL_TICKET = TicketType.create("antarchy_ant_arrival", Comparator.comparingLong(ChunkPos::toLong), 300);
    private static final int MAX_WAIT_TICKS = 200;
    private static final Map<UUID, PendingArrival> PENDING = new LinkedHashMap<>();

    private AntArrivalScheduler() {
    }

    static void request(ServerPlayer player, ServerLevel destination, BlockPos center, int searchRadius, Runnable onLoaded) {
        ChunkPos centerChunk = new ChunkPos(center);
        int chunkRadius = (searchRadius >> 4) + 1;
        destination.getChunkSource().addRegionTicket(ARRIVAL_TICKET, centerChunk, chunkRadius + 1, centerChunk);
        PendingArrival arrival = new PendingArrival(player, destination, centerChunk, chunkRadius, onLoaded);
        if (arrival.isLoaded()) {
            PENDING.remove(player.getUUID());
            onLoaded.run();
            return;
        }
        PENDING.put(player.getUUID(), arrival);
    }

    public static void tick(MinecraftServer server) {
        if (PENDING.isEmpty()) {
            return;
        }
        List<PendingArrival> ready = new ArrayList<>();
        Iterator<PendingArrival> iterator = PENDING.values().iterator();
        while (iterator.hasNext()) {
            PendingArrival arrival = iterator.next();
            if (arrival.destination.getServer() != server || arrival.player.hasDisconnected() || arrival.player.isRemoved()) {
                iterator.remove();
                continue;
            }
            arrival.waitedTicks++;
            if (arrival.waitedTicks >= MAX_WAIT_TICKS || arrival.isLoaded()) {
                iterator.remove();
                ready.add(arrival);
            }
        }
        for (PendingArrival arrival : ready) {
            arrival.onLoaded.run();
        }
    }

    public static void clear() {
        PENDING.clear();
    }

    private static final class PendingArrival {
        private final ServerPlayer player;
        private final ServerLevel destination;
        private final ChunkPos center;
        private final int chunkRadius;
        private final Runnable onLoaded;
        private int waitedTicks;

        private PendingArrival(ServerPlayer player, ServerLevel destination, ChunkPos center, int chunkRadius, Runnable onLoaded) {
            this.player = player;
            this.destination = destination;
            this.center = center;
            this.chunkRadius = chunkRadius;
            this.onLoaded = onLoaded;
        }

        private boolean isLoaded() {
            ServerChunkCache chunks = this.destination.getChunkSource();
            for (int chunkX = this.center.x - this.chunkRadius; chunkX <= this.center.x + this.chunkRadius; chunkX++) {
                for (int chunkZ = this.center.z - this.chunkRadius; chunkZ <= this.center.z + this.chunkRadius; chunkZ++) {
                    if (chunks.getChunkNow(chunkX, chunkZ) == null) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
