package com.craisinlord.antarchy.content.worldgen.ocean;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class StarCoralPatchFeature extends Feature<NoneFeatureConfiguration> {
    public StarCoralPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Block starCoralBlock = AntarchyObjects.STAR_CORAL_BLOCK.get();
        Block starCoral = AntarchyObjects.STAR_CORAL.get();
        Block starCoralFan = AntarchyObjects.STAR_CORAL_FAN.get();

        int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
        BlockPos floorPos = new BlockPos(origin.getX(), surfaceY - 1, origin.getZ());
        if (!level.getBlockState(floorPos).isSolid() || !level.getFluidState(floorPos.above()).is(FluidTags.WATER)) {
            return false;
        }

        int radius = 3 + random.nextInt(3);
        int targetStructures = 3 + random.nextInt(4);
        int attempts = targetStructures * 4;
        int placed = 0;

        for (int i = 0; i < attempts && placed < targetStructures; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            BlockPos supportPos = new BlockPos(x, y - 1, z);
            BlockPos basePos = supportPos.above();

            if (!level.getBlockState(supportPos).isSolid()) {
                continue;
            }
            if (!level.getFluidState(basePos).is(FluidTags.WATER)) {
                continue;
            }
            if (!level.getBlockState(basePos).canBeReplaced()) {
                continue;
            }

            growCoralStructure(level, random, basePos, starCoralBlock, starCoral, starCoralFan);
            placed++;
        }

        return placed > 0;
    }

    private void growCoralStructure(WorldGenLevel level, RandomSource random, BlockPos basePos, Block solidBlock, Block plantBlock, Block fanBlock) {
        BlockPos pos = basePos;
        int trunkHeight = 1 + random.nextInt(3);
        for (int i = 0; i < trunkHeight; i++) {
            if (!tryPlaceSolid(level, pos, solidBlock)) {
                return;
            }
            pos = pos.above();
        }

        List<Direction> directions = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        Collections.shuffle(directions, new java.util.Random(random.nextLong()));

        int branchCount = Math.min(directions.size(), 2 + random.nextInt(3));
        for (int b = 0; b < branchCount; b++) {
            Direction direction = directions.get(b);
            BlockPos branchPos = pos;
            int branchLength = 2 + random.nextInt(4);
            boolean grew = false;
            for (int step = 0; step < branchLength; step++) {
                branchPos = branchPos.relative(direction);
                if (step > 0 && random.nextInt(3) == 0) {
                    branchPos = branchPos.above();
                }
                if (!tryPlaceSolid(level, branchPos, solidBlock)) {
                    break;
                }
                grew = true;
            }

            if (grew) {
                BlockState tip = random.nextBoolean() ? plantBlock.defaultBlockState() : fanBlock.defaultBlockState();
                tryPlaceState(level, branchPos.above(), tip);
            }
        }
    }

    private boolean tryPlaceSolid(WorldGenLevel level, BlockPos pos, Block block) {
        return tryPlaceState(level, pos, block.defaultBlockState());
    }

    private boolean tryPlaceState(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getBlockState(pos).canBeReplaced()) {
            return false;
        }

        level.setBlock(pos, state, 2);
        return true;
    }
}
