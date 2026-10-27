package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jetbrains.annotations.Nullable;

public record DimensionalTearFeature() implements Feature {
    public static final MapCodec<DimensionalTearFeature> CODEC = MapCodec.unit(DimensionalTearFeature::new);
    private static final int LOCAL_SEARCH_ATTEMPTS = 18;

    @Override
    public MapCodec<DimensionalTearFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!AntarchySettings.dimensionalTearsSpawnNaturally()) {
            return false;
        }

        if (!(level instanceof ServerLevelAccessor)) {
            return false;
        }

        BlockPos anchor = findOpenPocket(level, origin, random, LOCAL_SEARCH_ATTEMPTS);
        if (anchor == null || !canAccess(level, anchor)) {
            return false;
        }

        level.setBlock(anchor, AntarchyObjects.DIMENSIONAL_TEAR_MARKER.get().defaultBlockState(), 2);
        return true;
    }

    @Nullable
    private static BlockPos findOpenPocket(WorldGenLevel level, BlockPos origin, RandomSource random, int attempts) {
        int minY = level.getMinY() + 8;
        int maxY = level.getMaxY() - 7;
        for (int i = 0; i < attempts; i++) {
            BlockPos candidate = origin.offset(
                    Mth.nextInt(random, -16, 16),
                    Mth.nextInt(random, -24, 24),
                    Mth.nextInt(random, -16, 16)
            );
            candidate = new BlockPos(candidate.getX(), Mth.clamp(candidate.getY(), minY, maxY), candidate.getZ());
            if (isUsablePocket(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isUsablePocket(WorldGenLevel level, BlockPos pos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (!canAccess(level, checkPos) || !level.getBlockState(checkPos).getCollisionShape(level, checkPos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean canAccess(WorldGenLevel level, BlockPos pos) {
        return !(level instanceof WorldGenRegion region) || region.ensureCanWrite(pos);
    }
}
