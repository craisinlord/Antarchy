package com.craisinlord.antarchy.content.portal;

import com.craisinlord.antarchy.compat.infinity.InfinityCompat;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Converts an unlit obsidian frame near a killed rainbow ant into a real, permanent Infinite Dimensions
 * portal to that ant's assigned dimension. Lights the frame as a normal vanilla Nether portal first, then
 * hands it to Infinity's own portal-creation pipeline (the same one used when a book is thrown into a lit
 * portal), so the result behaves exactly like any other physical Infinity portal from then on.
 */
public final class RainbowPortalIgniter {
    private static final int SEARCH_RADIUS = 2;

    private RainbowPortalIgniter() {
    }

    public static boolean tryIgnite(ServerLevel level, AABB deathBounds, ResourceLocation dimensionId) {
        PermanentPortalShape shape = findInactiveShape(level, deathBounds);
        if (shape == null) {
            return false;
        }

        shape.fill(level);
        BlockPos anyInteriorPos = firstInteriorPos(shape);
        if (anyInteriorPos == null) {
            return false;
        }

        boolean created = InfinityCompat.get().createPhysicalPortal(level, anyInteriorPos, dimensionId);
        if (!created) {
            return false;
        }

        Vec3 center = shape.center();
        level.playSound(null, center.x, center.y, center.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.9F, 0.9F);
        return true;
    }

    @Nullable
    private static PermanentPortalShape findInactiveShape(ServerLevel level, AABB bounds) {
        int minX = (int) Math.floor(bounds.minX + 0.001D) - SEARCH_RADIUS;
        int minY = (int) Math.floor(bounds.minY + 0.001D) - SEARCH_RADIUS;
        int minZ = (int) Math.floor(bounds.minZ + 0.001D) - SEARCH_RADIUS;
        int maxX = (int) Math.floor(bounds.maxX - 0.001D) + SEARCH_RADIUS;
        int maxY = (int) Math.floor(bounds.maxY - 0.001D) + SEARCH_RADIUS;
        int maxZ = (int) Math.floor(bounds.maxZ - 0.001D) + SEARCH_RADIUS;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    PermanentPortalShape xShape = PermanentPortalShape.findInactive(
                            level, candidate, AntarchyTags.Blocks.RAINBOW_PORTAL_FRAMES, Blocks.NETHER_PORTAL, Direction.Axis.X);
                    if (xShape != null && xShape.intersects(bounds)) {
                        return xShape;
                    }
                    PermanentPortalShape zShape = PermanentPortalShape.findInactive(
                            level, candidate, AntarchyTags.Blocks.RAINBOW_PORTAL_FRAMES, Blocks.NETHER_PORTAL, Direction.Axis.Z);
                    if (zShape != null && zShape.intersects(bounds)) {
                        return zShape;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private static BlockPos firstInteriorPos(PermanentPortalShape shape) {
        BlockPos[] holder = new BlockPos[1];
        shape.forEachInterior(pos -> {
            if (holder[0] == null) {
                holder[0] = pos;
            }
        });
        return holder[0];
    }
}
