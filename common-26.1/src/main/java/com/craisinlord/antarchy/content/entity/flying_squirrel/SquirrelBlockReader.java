package com.craisinlord.antarchy.content.entity.flying_squirrel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

final class SquirrelBlockReader {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState VOID_AIR = Blocks.VOID_AIR.defaultBlockState();

    private final Level level;
    private final boolean direct;
    private int chunkX;
    private int chunkZ;
    @Nullable
    private LevelChunk chunk;
    private int sectionX;
    private int sectionY;
    private int sectionZ;
    @Nullable
    private LevelChunkSection section;

    SquirrelBlockReader(Level level) {
        this.level = level;
        this.direct = level.isDebug();
    }

    BlockState getBlockState(int x, int y, int z) {
        if (this.level.isOutsideBuildHeight(y)) {
            return VOID_AIR;
        }
        if (this.direct) {
            return this.level.getBlockState(new BlockPos(x, y, z));
        }
        LevelChunkSection levelSection = this.sectionAt(x, y, z);
        return levelSection.hasOnlyAir() ? AIR : levelSection.getBlockState(x & 15, y & 15, z & 15);
    }

    BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    boolean isAir(int x, int y, int z) {
        return this.getBlockState(x, y, z).isAir();
    }

    boolean sectionHasOnlyAir(int x, int y, int z) {
        if (this.level.isOutsideBuildHeight(y)) {
            return true;
        }
        if (this.direct) {
            return false;
        }
        return this.sectionAt(x, y, z).hasOnlyAir();
    }

    boolean sectionMayContain(int x, int y, int z, Predicate<BlockState> predicate) {
        if (this.level.isOutsideBuildHeight(y)) {
            return predicate.test(VOID_AIR);
        }
        if (this.direct) {
            return true;
        }
        LevelChunkSection levelSection = this.sectionAt(x, y, z);
        if (levelSection.hasOnlyAir()) {
            return predicate.test(AIR);
        }
        return levelSection.maybeHas(predicate);
    }

    private LevelChunkSection sectionAt(int x, int y, int z) {
        int sx = x >> 4;
        int sy = y >> 4;
        int sz = z >> 4;
        LevelChunkSection levelSection = this.section;
        if (levelSection != null && sx == this.sectionX && sy == this.sectionY && sz == this.sectionZ) {
            return levelSection;
        }
        LevelChunk levelChunk = this.chunk;
        if (levelChunk == null || sx != this.chunkX || sz != this.chunkZ) {
            levelChunk = this.level.getChunk(sx, sz);
            this.chunk = levelChunk;
            this.chunkX = sx;
            this.chunkZ = sz;
        }
        levelSection = levelChunk.getSection(levelChunk.getSectionIndex(y));
        this.section = levelSection;
        this.sectionX = sx;
        this.sectionY = sy;
        this.sectionZ = sz;
        return levelSection;
    }
}
