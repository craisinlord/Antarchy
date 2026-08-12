package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.HushweedBlock;
import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HushweedBlockEntity extends BlockEntity {
    private static final int COOLDOWN_TICKS = 30;
    private static final double ATTACK_RANGE = 8.0D;
    private static final double PROJECTILE_CLEARANCE = 1.1D;
    private int cooldownTicks;

    public HushweedBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.HUSHWEED_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, HushweedBlockEntity blockEntity) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        if (blockEntity.cooldownTicks > 0) {
            blockEntity.cooldownTicks--;
            return;
        }

        Vec3 center = Vec3.atCenterOf(pos);
        LivingEntity target = level.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(pos).inflate(ATTACK_RANGE),
                        entity -> entity.isAlive()
                                && entity.getType().is(AntarchyTags.Entities.HUSHWEED_TARGETS)
                                && (!(entity instanceof Player player) || (!player.isCreative() && !player.isSpectator())))
                .stream()
                .min(java.util.Comparator.comparingDouble(entity -> entity.distanceToSqr(center)))
                .orElse(null);
        if (target == null) {
            return;
        }

        Direction facing = state.getValue(HushweedBlock.FACING);
        Vec3 spawnPos = facing == Direction.UP
                ? new Vec3(pos.getX() + 0.5D, pos.getY() + PROJECTILE_CLEARANCE, pos.getZ() + 0.5D)
                : new Vec3(pos.getX() + 0.5D, pos.getY() + (1.0D - PROJECTILE_CLEARANCE), pos.getZ() + 0.5D);
        Vec3 targetPos = target.getEyePosition().subtract(spawnPos);
        if (targetPos.lengthSqr() < 1.0E-6D) {
            return;
        }

        HushProjectileEntity projectile = new HushProjectileEntity(AntarchyObjects.HUSH_PROJECTILE.get(), level);
        projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        projectile.setTargetUuid(target.getUUID());
        projectile.setDeltaMovement(0.0D, 0.27D, 0.0D);
        projectile.setNoGravity(true);
        level.addFreshEntity(projectile);
        blockEntity.cooldownTicks = COOLDOWN_TICKS;
    }
}
