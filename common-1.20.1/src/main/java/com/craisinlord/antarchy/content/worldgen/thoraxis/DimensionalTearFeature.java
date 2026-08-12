package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DimensionalTearFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_LINK_RANGE = 50;
    private static final int MAX_LINK_RANGE = 500;
    private static final int LOCAL_SEARCH_ATTEMPTS = 18;
    private static final int PARTNER_ORIGIN_ATTEMPTS = 12;
    private static final int PARTNER_SEARCH_ATTEMPTS = 48;

    public DimensionalTearFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ServerLevelAccessor serverLevelAccessor = level;

        RandomSource random = context.random();
        BlockPos first = findOpenPocket(level, context.origin(), random, LOCAL_SEARCH_ATTEMPTS);
        if (first == null) {
            return false;
        }

        BlockPos second = null;
        for (int i = 0; i < PARTNER_ORIGIN_ATTEMPTS && second == null; i++) {
            second = findOpenPocket(level, linkedOrigin(first, random), random, PARTNER_SEARCH_ATTEMPTS);
        }
        if (second == null) {
            return false;
        }

        ServerLevel serverLevel = serverLevelAccessor.getLevel();
        int lifetime = Math.max(1200, AntarchySettings.dimensionalTearLifetimeTicks());
        float yawA = random.nextFloat() * 360.0F;
        float yawB = Mth.wrapDegrees(yawA + 140.0F + random.nextFloat() * 80.0F);
        DimensionalTearEntity tearA = DimensionalTearEntity.create(serverLevel, center(first), yawA, lifetime);
        DimensionalTearEntity tearB = DimensionalTearEntity.create(serverLevel, center(second), yawB, lifetime);
        tearA.linkTo(tearB);
        tearB.linkTo(tearA);
        serverLevel.addFreshEntity(tearA);
        serverLevel.addFreshEntity(tearB);
        return true;
    }

    private static BlockPos linkedOrigin(BlockPos first, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        int distance = Mth.nextInt(random, MIN_LINK_RANGE, MAX_LINK_RANGE);
        int dx = Mth.floor(Math.cos(angle) * distance);
        int dz = Mth.floor(Math.sin(angle) * distance);
        int dy = Mth.nextInt(random, -36, 36);
        return first.offset(dx, dy, dz);
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

    private static Vec3 center(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }
}
