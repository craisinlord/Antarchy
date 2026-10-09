package com.craisinlord.antarchy.content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BlockSuction {
    private static final float MAX_EXPLOSION_RESISTANCE = 30.0F;

    private BlockSuction() {
    }

    public static boolean canLift(ServerLevel level, BlockPos pos, BlockState state) {
        return canLift(level, pos, state, false);
    }

    public static boolean canLift(ServerLevel level, BlockPos pos, BlockState state, boolean allowCollisionless) {
        return !state.isAir()
                && level.getBlockEntity(pos) == null
                && state.getDestroySpeed(level, pos) >= 0.0F
                && state.getBlock().getExplosionResistance() <= MAX_EXPLOSION_RESISTANCE
                && (allowCollisionless ? state.getFluidState().isEmpty() : !state.getCollisionShape(level, pos).isEmpty());
    }

    public static FallingBlockEntity lift(ServerLevel level, BlockPos pos, BlockState state, Vec3 target, double speed) {
        FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
        if (falling == null) {
            return null;
        }
        Vec3 towardTarget = target.subtract(falling.position());
        if (towardTarget.lengthSqr() > 0.001D) {
            falling.setDeltaMovement(towardTarget.normalize().scale(speed));
        }
        falling.hasImpulse = true;
        falling.hurtMarked = true;
        return falling;
    }
}
