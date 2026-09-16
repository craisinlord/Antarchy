package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class QueenTrailSpawnMarkerBlockEntity extends BlockEntity {
    private static final double ACTIVATION_DISTANCE_SQUARED = 96.0D * 96.0D;
    private long siteId;
    private BlockPos spawnPos = BlockPos.ZERO;
    private BlockPos homePos = BlockPos.ZERO;
    private float yaw;
    private boolean configured;
    private boolean consumed;
    @Nullable
    private UUID queenUuid;

    public QueenTrailSpawnMarkerBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> typeSupplier) {
        super(typeSupplier.get(), pos, state);
    }

    public void configure(long siteId, BlockPos spawnPos, BlockPos homePos, float yaw) {
        this.siteId = siteId;
        this.spawnPos = spawnPos.immutable();
        this.homePos = homePos.immutable();
        this.yaw = yaw;
        this.configured = true;
        this.setChanged();
    }

    public long getSiteId() {
        return this.siteId;
    }

    public BlockPos getSpawnPos() {
        return this.spawnPos;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public static void serverTick(ServerLevel level, QueenTrailSpawnMarkerBlockEntity marker) {
        if (!marker.configured || marker.consumed || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (Math.floorMod(level.getGameTime(), 20L) != Math.floorMod(marker.siteId, 20L)) {
            return;
        }
        boolean nearbyPlayer = level.players().stream().anyMatch(player -> !player.isSpectator()
                && player.distanceToSqr(marker.spawnPos.getX() + 0.5D, marker.spawnPos.getY() + 0.5D, marker.spawnPos.getZ() + 0.5D) <= ACTIVATION_DISTANCE_SQUARED);
        if (!nearbyPlayer) {
            return;
        }
        // The Queen is large enough to span several chunks. Ensure the area around
        // the arrival point is available before checking its collision box.
        for (int chunkX = (marker.spawnPos.getX() - 16) >> 4; chunkX <= (marker.spawnPos.getX() + 16) >> 4; chunkX++) {
            for (int chunkZ = (marker.spawnPos.getZ() - 16) >> 4; chunkZ <= (marker.spawnPos.getZ() + 16) >> 4; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
        marker.spawnNow(level);
    }

    @Nullable
    public QueenEntity spawnNow(ServerLevel level) {
        if (!this.configured) {
            return null;
        }
        if (this.consumed) {
            if (this.queenUuid != null && level.getEntity(this.queenUuid) instanceof QueenEntity queen) {
                return queen;
            }
            return null;
        }
        for (int chunkX = (this.spawnPos.getX() - 16) >> 4; chunkX <= (this.spawnPos.getX() + 16) >> 4; chunkX++) {
            for (int chunkZ = (this.spawnPos.getZ() - 16) >> 4; chunkZ <= (this.spawnPos.getZ() + 16) >> 4; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
        QueenEntity queen = QueenEntity.spawnFromUndersideTrail(level, this.spawnPos, this.homePos, this.siteId, this.yaw);
        if (queen != null) {
            this.consumed = true;
            this.queenUuid = queen.getUUID();
            this.setChanged();
        }
        return queen;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("site_id", this.siteId);
        tag.putLong("spawn_pos", this.spawnPos.asLong());
        tag.putLong("home_pos", this.homePos.asLong());
        tag.putFloat("yaw", this.yaw);
        tag.putBoolean("configured", this.configured);
        tag.putBoolean("consumed", this.consumed);
        if (this.queenUuid != null) {
            tag.putUUID("queen_uuid", this.queenUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.siteId = tag.getLong("site_id");
        this.spawnPos = BlockPos.of(tag.getLong("spawn_pos"));
        this.homePos = BlockPos.of(tag.getLong("home_pos"));
        this.yaw = tag.getFloat("yaw");
        this.configured = tag.getBoolean("configured");
        this.consumed = tag.getBoolean("consumed");
        this.queenUuid = tag.hasUUID("queen_uuid") ? tag.getUUID("queen_uuid") : null;
    }
}
