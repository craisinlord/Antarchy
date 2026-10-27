package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ElythiaUndergroundFeature extends Feature<NoneFeatureConfiguration> {
    public ElythiaUndergroundFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos cavity = this.findCavity(level, context.origin(), random);
        if (cavity == null) {
            return false;
        }

        BlockPos floorPos = cavity.below();
        BlockPos ceilingPos = this.findCeiling(level, cavity, 8);
        if (ceilingPos == null) {
            return false;
        }

        return switch (random.nextInt(8)) {
            case 0, 1, 2 -> this.placeFloorPatch(level, floorPos, random, Blocks.DIRT.defaultBlockState(), 3 + random.nextInt(4));
            case 3, 4, 5 -> this.placeFloorPatch(level, floorPos, random, Blocks.COARSE_DIRT.defaultBlockState(), 4 + random.nextInt(5));
            case 6 -> this.placeFloorPatch(level, floorPos, random, Blocks.GRANITE.defaultBlockState(), 3 + random.nextInt(4));
            default -> this.placeDripstonePatch(level, floorPos, ceilingPos, random, 2 + random.nextInt(3));
        };
    }

    private BlockPos findCavity(WorldGenLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos candidate = origin.offset(random.nextInt(17) - 8, random.nextInt(25) - 12, random.nextInt(17) - 8);
            for (int dy = -6; dy <= 6; dy++) {
                BlockPos airPos = candidate.above(dy);
                if (this.isUsableCavity(level, airPos)) {
                    return airPos;
                }
            }
        }
        return null;
    }

    private boolean isUsableCavity(WorldGenLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && isSolidCaveBlock(level.getBlockState(pos.below()))
                && !level.isEmptyBlock(pos.above(3));
    }

    private BlockPos findCeiling(WorldGenLevel level, BlockPos start, int range) {
        for (int step = 1; step <= range; step++) {
            BlockPos pos = start.above(step);
            if (!level.isEmptyBlock(pos)) {
                return pos;
            }
        }
        return null;
    }

    private boolean placeFloorPatch(WorldGenLevel level, BlockPos center, RandomSource random, BlockState replacement, int radius) {
        boolean placedAny = false;
        int radiusSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radiusSq + random.nextInt(2)) {
                    continue;
                }

                BlockPos targetPos = center.offset(x, 0, z);
                if (!isSolidCaveBlock(level.getBlockState(targetPos)) || !level.isEmptyBlock(targetPos.above())) {
                    continue;
                }

                level.setBlock(targetPos, replacement, 2);
                placedAny = true;
            }
        }
        return placedAny;
    }

    private boolean placeDripstonePatch(WorldGenLevel level, BlockPos floorCenter, BlockPos ceilingCenter, RandomSource random, int radius) {
        boolean placedAny = false;
        int radiusSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radiusSq + random.nextInt(2)) {
                    continue;
                }

                BlockPos floorPos = floorCenter.offset(x, 0, z);
                if (isSolidCaveBlock(level.getBlockState(floorPos)) && level.isEmptyBlock(floorPos.above())) {
                    level.setBlock(floorPos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
                    if (random.nextFloat() < 0.45F && level.isEmptyBlock(floorPos.above())) {
                        level.setBlock(
                                floorPos.above(),
                                Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP),
                                2
                        );
                    }
                    placedAny = true;
                }

                BlockPos ceilingPos = ceilingCenter.offset(x, 0, z);
                if (isSolidCaveBlock(level.getBlockState(ceilingPos)) && level.isEmptyBlock(ceilingPos.below())) {
                    level.setBlock(ceilingPos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
                    if (random.nextFloat() < 0.55F && level.isEmptyBlock(ceilingPos.below())) {
                        level.setBlock(
                                ceilingPos.below(),
                                Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN),
                                2
                        );
                    }
                    placedAny = true;
                }
            }
        }
        return placedAny;
    }

    private static boolean isSolidCaveBlock(BlockState state) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PACKED_MUD)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.DRIPSTONE_BLOCK);
    }
}
