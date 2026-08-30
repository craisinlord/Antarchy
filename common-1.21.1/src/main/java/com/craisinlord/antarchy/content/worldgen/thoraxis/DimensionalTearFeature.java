package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

public final class DimensionalTearFeature extends Feature<NoneFeatureConfiguration> {
    private static final int LOCAL_SEARCH_ATTEMPTS = 18;

    public DimensionalTearFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!(level instanceof ServerLevelAccessor)) {
            return false;
        }

        RandomSource random = context.random();
        BlockPos anchor = findOpenPocket(level, context.origin(), random, LOCAL_SEARCH_ATTEMPTS);
        if (anchor == null || !canAccess(level, anchor)) {
            return false;
        }

        level.setBlock(anchor, AntarchyObjects.DIMENSIONAL_TEAR_MARKER.get().defaultBlockState(), 2);
        return true;
    }

    @Nullable
    private static BlockPos findOpenPocket(WorldGenLevel level, BlockPos origin, RandomSource random, int attempts) {
        int minY = level.getMinBuildHeight() + 8;
        int maxY = level.getMaxBuildHeight() - 8;
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
