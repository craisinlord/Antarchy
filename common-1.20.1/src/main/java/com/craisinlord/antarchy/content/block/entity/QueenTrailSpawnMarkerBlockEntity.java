package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class QueenTrailSpawnMarkerBlockEntity extends BlockEntity {
    private static final double ACTIVATION_DISTANCE_SQUARED = 96.0D * 96.0D;
    private long siteId;
    private BlockPos spawnPos = BlockPos.ZERO;
    private BlockPos homePos = BlockPos.ZERO;
    private float yaw;
    private boolean configured;
    private boolean consumed;
    private UUID queenUuid;

    public QueenTrailSpawnMarkerBlockEntity(BlockPos pos, BlockState state,
                                            Supplier<? extends BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> type) {
        super(type.get(), pos, state);
    }

    public void configure(long siteId, BlockPos spawnPos, BlockPos homePos, float yaw) {
        this.siteId = siteId;
        this.spawnPos = spawnPos.immutable();
        this.homePos = homePos.immutable();
        this.yaw = yaw;
        this.configured = true;
        setChanged();
    }

    public long getSiteId() { return siteId; }
    public BlockPos getSpawnPos() { return spawnPos; }
    public boolean isConfigured() { return configured; }

    public static void serverTick(ServerLevel level, QueenTrailSpawnMarkerBlockEntity marker) {
        if (!marker.configured || marker.consumed || level.getDifficulty() == Difficulty.PEACEFUL) return;
        if (Math.floorMod(level.getGameTime(), 20L) != Math.floorMod(marker.siteId, 20L)) return;
        boolean nearby = level.players().stream().anyMatch(player -> !player.isSpectator()
                && player.distanceToSqr(marker.spawnPos.getX() + .5D, marker.spawnPos.getY() + .5D,
                marker.spawnPos.getZ() + .5D) <= ACTIVATION_DISTANCE_SQUARED);
        if (!nearby) return;
        for (int chunkX = (marker.spawnPos.getX() - 16) >> 4; chunkX <= (marker.spawnPos.getX() + 16) >> 4; chunkX++)
            for (int chunkZ = (marker.spawnPos.getZ() - 16) >> 4; chunkZ <= (marker.spawnPos.getZ() + 16) >> 4; chunkZ++)
                level.getChunk(chunkX, chunkZ);
        QueenEntity queen = QueenEntity.spawnFromUndersideTrail(level, marker.spawnPos, marker.homePos, marker.siteId, marker.yaw);
        if (queen != null) {
            marker.consumed = true;
            marker.queenUuid = queen.getUUID();
            marker.setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        siteId = tag.getLong("site_id");
        spawnPos = BlockPos.of(tag.getLong("spawn_pos"));
        homePos = BlockPos.of(tag.getLong("home_pos"));
        yaw = tag.getFloat("yaw");
        configured = tag.getBoolean("configured");
        consumed = tag.getBoolean("consumed");
        queenUuid = tag.hasUUID("queen_uuid") ? tag.getUUID("queen_uuid") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("site_id", siteId);
        tag.putLong("spawn_pos", spawnPos.asLong());
        tag.putLong("home_pos", homePos.asLong());
        tag.putFloat("yaw", yaw);
        tag.putBoolean("configured", configured);
        tag.putBoolean("consumed", consumed);
        if (queenUuid != null) tag.putUUID("queen_uuid", queenUuid);
    }
}
