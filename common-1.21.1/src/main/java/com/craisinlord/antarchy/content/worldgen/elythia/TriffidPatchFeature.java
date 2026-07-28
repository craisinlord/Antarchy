package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.TriffidEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class TriffidPatchFeature extends Feature<NoneFeatureConfiguration> {
    private static final int PATCH_RADIUS = 5;
    private static final double PATCH_RADIUS_SQR = 30.25D;

    public TriffidPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        if (surfaceY <= level.getMinBuildHeight()) {
            return false;
        }

        BlockPos center = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        if (!canUseSurface(level, center)) {
            return false;
        }

        if (!(level instanceof ServerLevelAccessor serverLevel)) {
            return false;
        }

        TriffidEntity triffid = AntarchyObjects.TRIFFID.get().create(serverLevel.getLevel());
        if (triffid == null) {
            return false;
        }

        DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(center);
        triffid.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        triffid.finalizeSpawn(serverLevel, difficulty, MobSpawnType.CHUNK_GENERATION, null);
        triffid.setPersistenceRequired();

        layPatch(level, center, random);
        serverLevel.addFreshEntity(triffid);
        return true;
    }

    private static boolean canUseSurface(WorldGenLevel level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos surfacePos = center.offset(dx, 0, dz);
                BlockState surfaceState = level.getBlockState(surfacePos);
                BlockPos belowPos = surfacePos.below();
                BlockState belowState = level.getBlockState(belowPos);

                if (!isSurfaceCandidate(surfaceState)) {
                    return false;
                }

                if (!belowState.isFaceSturdy(level, belowPos, Direction.UP)) {
                    return false;
                }

                for (int y = 1; y <= 7; y++) {
                    BlockState aboveState = level.getBlockState(surfacePos.above(y));
                    if (!aboveState.isAir() && !aboveState.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static void layPatch(WorldGenLevel level, BlockPos center, RandomSource random) {
        for (int dx = -PATCH_RADIUS; dx <= PATCH_RADIUS; dx++) {
            for (int dz = -PATCH_RADIUS; dz <= PATCH_RADIUS; dz++) {
                double distanceSqr = dx * dx + dz * dz;
                if (distanceSqr > PATCH_RADIUS_SQR) {
                    continue;
                }

                BlockPos surfacePos = center.offset(dx, 0, dz);
                BlockState surfaceState = level.getBlockState(surfacePos);
                if (!isPatchSurfaceCandidate(surfaceState)) {
                    continue;
                }

                clearReplaceableColumn(level, surfacePos, 8);

                BlockState patchState = patchState(random, dx, dz, distanceSqr);
                if (patchState != null) {
                    level.setBlock(surfacePos, patchState, 2);
                }
            }
        }
    }

    private static void clearReplaceableColumn(WorldGenLevel level, BlockPos surfacePos, int maxHeight) {
        for (int y = 1; y <= maxHeight; y++) {
            BlockPos abovePos = surfacePos.above(y);
            BlockState aboveState = level.getBlockState(abovePos);
            if (aboveState.canBeReplaced()) {
                level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private static BlockState patchState(RandomSource random, int dx, int dz, double distanceSqr) {
        if (dx == 0 && dz == 0) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }

        double normalizedDistance = Math.sqrt(distanceSqr) / PATCH_RADIUS;
        double mudChance = 1.0D - normalizedDistance * 0.82D;
        if (mudChance <= 0.0D || random.nextDouble() > mudChance) {
            return null;
        }

        BlockState goo = AntarchyObjects.TRIFFID_GOO_BLOCK.get().defaultBlockState();
        BlockState mud = Blocks.MUD.defaultBlockState();

        if (normalizedDistance < 0.32D) {
            float roll = random.nextFloat();
            if (roll < 0.45F) {
                return Blocks.MOSS_BLOCK.defaultBlockState();
            }
            return roll < 0.72F ? goo : mud;
        }

        if (normalizedDistance < 0.58D) {
            float roll = random.nextFloat();
            if (roll < 0.18F) {
                return Blocks.MOSS_BLOCK.defaultBlockState();
            }
            return roll < 0.44F ? goo : mud;
        }

        return random.nextFloat() < 0.18F ? goo : mud;
    }

    private static boolean isSurfaceCandidate(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.canBeReplaced();
    }

    private static boolean isPatchSurfaceCandidate(BlockState state) {
        return isSurfaceCandidate(state)
                || state.is(Blocks.MUD);
    }
}
