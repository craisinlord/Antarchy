package com.craisinlord.antarchy.content.entity.royal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class RoyalBlockDestruction {
    private RoyalBlockDestruction() {
    }

    public static int destroySphere(
            ServerLevel level,
            Entity source,
            Vec3 center,
            double radius,
            int maxBlocks,
            double maxResistance,
            float dropChance
    ) {
        if (maxBlocks <= 0 || radius <= 0.0D || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return 0;
        }

        int minX = net.minecraft.util.Mth.floor(center.x - radius);
        int minY = net.minecraft.util.Mth.floor(center.y - radius);
        int minZ = net.minecraft.util.Mth.floor(center.z - radius);
        int maxX = net.minecraft.util.Mth.floor(center.x + radius);
        int maxY = net.minecraft.util.Mth.floor(center.y + radius);
        int maxZ = net.minecraft.util.Mth.floor(center.z + radius);
        double radiusSqr = radius * radius;

        int destroyed = 0;
        for (BlockPos cursor : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (destroyed >= maxBlocks) {
                break;
            }
            if (cursor.distToCenterSqr(center.x, center.y, center.z) > radiusSqr
                    || cursor.getY() < level.getMinBuildHeight()
                    || cursor.getY() >= level.getMaxBuildHeight()
                    || !level.getWorldBorder().isWithinBounds(cursor)) {
                continue;
            }

            BlockPos pos = cursor.immutable();
            BlockState existing = level.getBlockState(pos);
            if (existing.isAir()
                    || level.getBlockEntity(pos) != null
                    || existing.getDestroySpeed(level, pos) < 0.0F
                    || existing.getBlock().getExplosionResistance() > maxResistance
                    || existing.getCollisionShape(level, pos).isEmpty()) {
                continue;
            }

            if (source.getRandom().nextFloat() <= dropChance) {
                level.destroyBlock(pos, true, source);
            } else {
                level.levelEvent(2001, pos, Block.getId(existing));
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            destroyed++;
        }
        return destroyed;
    }
}
