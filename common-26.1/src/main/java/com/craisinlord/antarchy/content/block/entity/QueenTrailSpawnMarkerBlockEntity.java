package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

    public boolean isConsumed() {
        return this.consumed;
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
        if (queen == null) {
            for (int radius = 2; radius <= 10 && queen == null; radius += 2) {
                for (int dx = -radius; dx <= radius && queen == null; dx += radius) {
                    for (int dz = -radius; dz <= radius && queen == null; dz += radius) {
                        BlockPos candidate = this.spawnPos.offset(dx, 0, dz);
                        if (candidate.equals(this.spawnPos)) {
                            continue;
                        }
                        queen = QueenEntity.spawnFromUndersideTrail(level, candidate, this.homePos, this.siteId, this.yaw);
                        if (queen != null) {
                            this.spawnPos = candidate.immutable();
                        }
                    }
                }
            }
        }
        if (queen != null) {
            this.consumed = true;
            this.queenUuid = queen.getUUID();
            this.setChanged();
        }
        return queen;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("site_id", this.siteId);
        output.putLong("spawn_pos", this.spawnPos.asLong());
        output.putLong("home_pos", this.homePos.asLong());
        output.putFloat("yaw", this.yaw);
        output.putBoolean("configured", this.configured);
        output.putBoolean("consumed", this.consumed);
        output.storeNullable("queen_uuid", UUIDUtil.CODEC, this.queenUuid);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.siteId = input.getLongOr("site_id", 0L);
        this.spawnPos = BlockPos.of(input.getLongOr("spawn_pos", 0L));
        this.homePos = BlockPos.of(input.getLongOr("home_pos", 0L));
        this.yaw = input.getFloatOr("yaw", 0.0F);
        this.configured = input.getBooleanOr("configured", false);
        this.consumed = input.getBooleanOr("consumed", false);
        this.queenUuid = input.read("queen_uuid", UUIDUtil.CODEC).orElse(null);
    }
}
