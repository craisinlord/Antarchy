package com.craisinlord.antarchy.content.entity.royal;

import java.util.function.Predicate;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public final class RoyalSectionScan {
    private static final double COLLISION_EPSILON = 1.0E-7D;
    private static final Predicate<BlockState> NON_AIR = state -> !state.isAir();

    private RoyalSectionScan() {
    }

    public static boolean chunksPresent(Level level, AABB area) {
        int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(area.minX));
        int maxChunkX = SectionPos.blockToSectionCoord(Mth.floor(area.maxX));
        int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(area.minZ));
        int maxChunkZ = SectionPos.blockToSectionCoord(Mth.floor(area.maxZ));
        ChunkSource chunks = level.getChunkSource();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (chunks.getChunkNow(chunkX, chunkZ) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean mayContain(Level level, AABB area, Predicate<BlockState> states) {
        int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(area.minX - COLLISION_EPSILON) - 1);
        int maxChunkX = SectionPos.blockToSectionCoord(Mth.floor(area.maxX + COLLISION_EPSILON) + 1);
        int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(area.minZ - COLLISION_EPSILON) - 1);
        int maxChunkZ = SectionPos.blockToSectionCoord(Mth.floor(area.maxZ + COLLISION_EPSILON) + 1);
        int minSectionY = Math.max(level.getMinSection(),
                SectionPos.blockToSectionCoord(Mth.floor(area.minY - COLLISION_EPSILON) - 1));
        int maxSectionY = Math.min(level.getMaxSection() - 1,
                SectionPos.blockToSectionCoord(Mth.floor(area.maxY + COLLISION_EPSILON) + 1));
        ChunkSource chunks = level.getChunkSource();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = chunks.getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    return true;
                }
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
                    if (!section.hasOnlyAir() && section.maybeHas(states)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAllAir(Level level, AABB area) {
        return !mayContain(level, area, NON_AIR);
    }

    public static boolean noEntityOrBorderCollision(Level level, Entity entity, AABB box) {
        if (!level.getEntityCollisions(entity, box).isEmpty()) {
            return false;
        }
        WorldBorder border = level.getWorldBorder();
        return !border.isInsideCloseToBorder(entity, box)
                || !Shapes.joinIsNotEmpty(border.getCollisionShape(), Shapes.create(box), BooleanOp.AND);
    }
}
