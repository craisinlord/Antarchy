package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.CreepingHorrorEggBlock;
import com.craisinlord.antarchy.content.block.LurkingTerrorEggBlock;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CavarynEggPatchFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 20;
    private static final int FLOOR_SCAN_RANGE = 10;

    public CavarynEggPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        BlockPos surfacePos = findSurfacePos(level, origin, random);
        if (surfacePos == null) {
            return false;
        }

        EggType eggType = random.nextBoolean() ? EggType.CREEPING_HORROR : EggType.LURKING_TERROR;
        return placePatch(level, surfacePos, random, eggType);
    }

    private static BlockPos findSurfacePos(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(FLOOR_SCAN_RANGE * 2 + 1) - FLOOR_SCAN_RANGE;
            startY = Math.max(level.getMinBuildHeight() + 2, Math.min(level.getMaxBuildHeight() - 3, startY));

            for (int y = startY; y >= Math.max(level.getMinBuildHeight() + 1, startY - FLOOR_SCAN_RANGE); y--) {
                mutable.set(x, y, z);
                BlockState floorState = level.getBlockState(mutable);
                BlockState airState = level.getBlockState(mutable.above());
                if (floorState.getFluidState().isEmpty()
                        && floorState.blocksMotion()
                        && floorState.isFaceSturdy(level, mutable, Direction.UP)
                        && airState.canBeReplaced()) {
                    return mutable.immutable();
                }
            }
        }

        return null;
    }

    private boolean placePatch(WorldGenLevel level, BlockPos center, RandomSource random, EggType eggType) {
        int radius = 2 + random.nextInt(3);
        boolean placedAny = false;
        List<BlockPos> eggSpots = new java.util.ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = dx * dx + dz * dz;
                if (distance > radius * radius + random.nextInt(2)) {
                    continue;
                }

                BlockPos floorPos = center.offset(dx, 0, dz);
                BlockState floorState = level.getBlockState(floorPos);
                BlockPos eggPos = floorPos.above();
                BlockState aboveState = level.getBlockState(eggPos);
                if (!isValidEggFloor(level, floorPos, floorState, aboveState)) {
                    continue;
                }
                eggSpots.add(eggPos);
            }
        }

        if (eggSpots.isEmpty()) {
            return placedAny;
        }

        int eggCount = Math.min(eggSpots.size(), 2 + random.nextInt(3));
        for (int i = 0; i < eggCount; i++) {
            BlockPos eggPos = eggSpots.get(random.nextInt(eggSpots.size()));
            if (!level.getBlockState(eggPos).canBeReplaced()) {
                continue;
            }

            BlockState eggState = eggType == EggType.CREEPING_HORROR
                    ? AntarchyObjects.CREEPING_HORROR_EGGS.get().defaultBlockState()
                    : AntarchyObjects.LURKING_TERROR_EGGS.get().defaultBlockState();
            eggState = eggState.setValue(eggType == EggType.CREEPING_HORROR ? CreepingHorrorEggBlock.EGGS : LurkingTerrorEggBlock.EGGS, 1 + random.nextInt(4));
            level.setBlock(eggPos, eggState, 3);
            placedAny = true;
        }

        return placedAny;
    }

    private static boolean isValidEggFloor(WorldGenLevel level, BlockPos floorPos, BlockState floorState, BlockState aboveState) {
        return !floorState.isAir()
                && floorState.getFluidState().isEmpty()
                && floorState.blocksMotion()
                && floorState.isFaceSturdy(level, floorPos, Direction.UP)
                && aboveState.canBeReplaced();
    }

    private enum EggType {
        CREEPING_HORROR,
        LURKING_TERROR
    }
}
