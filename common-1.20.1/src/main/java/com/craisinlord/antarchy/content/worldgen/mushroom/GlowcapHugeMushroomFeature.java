package com.craisinlord.antarchy.content.worldgen.mushroom;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class GlowcapHugeMushroomFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation GLOWCAP_MUSHROOM_BLOCK_ID = new ResourceLocation(Antarchy.MODID, "glowcap_mushroom_block");
    private static final int MIN_STEM_HEIGHT = 5;
    private static final int MAX_STEM_HEIGHT = 8;
    private static final int CAP_RADIUS = 2;
    private static final int TOP_RADIUS = 1;
    public GlowcapHugeMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Block glowcapBlock = getBlock(GLOWCAP_MUSHROOM_BLOCK_ID);
        if (glowcapBlock == null || !(glowcapBlock.defaultBlockState().getBlock() instanceof HugeMushroomBlock)) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int stemHeight = MIN_STEM_HEIGHT + random.nextInt(MAX_STEM_HEIGHT - MIN_STEM_HEIGHT + 1);
        BlockPos origin = context.origin();
        if (!isValidPlacementOrigin(level, origin, stemHeight + 2)) {
            return false;
        }
        BlockState capBase = glowcapBlock.defaultBlockState();
        BlockState stemState = Blocks.MUSHROOM_STEM.defaultBlockState();

        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        BlockPos.MutableBlockPos cursor = origin.mutable();
        boolean placedAny = false;

        for (int i = 0; i < stemHeight; i++) {
            if (i > 1 && i % 2 == 0) {
                cursor.move(lean);
            }
            cursor.setY(origin.getY() + i);
            if (!canReplace(level, cursor)) {
                continue;
            }
            level.setBlock(cursor, stemState, 3);
            placedAny = true;
        }

        BlockPos capCenter = cursor.above().immutable();
        placedAny |= placeUmbrellaCap(level, random, capCenter, capBase);

        return placedAny;
    }

    private static boolean isValidPlacementOrigin(WorldGenLevel level, BlockPos origin, int clearanceHeight) {
        if (!level.getBlockState(origin).isAir()) {
            return false;
        }

        BlockPos belowPos = origin.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (!belowState.is(BlockTags.MUSHROOM_GROW_BLOCK) && !belowState.isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }

        return hasVerticalClearance(level, origin, clearanceHeight);
    }

    private static boolean hasVerticalClearance(WorldGenLevel level, BlockPos origin, int clearanceHeight) {
        for (int offset = 0; offset <= clearanceHeight; offset++) {
            if (!canReplace(level, origin.above(offset))) {
                return false;
            }
        }
        return true;
    }

    private boolean placeUmbrellaCap(WorldGenLevel level, RandomSource random, BlockPos capCenter, BlockState capBase) {
        boolean placedAny = false;
        BlockState stemState = Blocks.MUSHROOM_STEM.defaultBlockState();

        for (int dx = -CAP_RADIUS; dx <= CAP_RADIUS; dx++) {
            for (int dz = -CAP_RADIUS; dz <= CAP_RADIUS; dz++) {
                boolean isCenter = dx == 0 && dz == 0;
                boolean coveredByTopTier = Math.abs(dx) <= TOP_RADIUS && Math.abs(dz) <= TOP_RADIUS;

                BlockPos pos = new BlockPos(capCenter.getX() + dx, capCenter.getY(), capCenter.getZ() + dz);
                if (!canReplace(level, pos)) {
                    continue;
                }

                if (isCenter) {
                    level.setBlock(pos, stemState, 3);
                    placedAny = true;
                    continue;
                }

                BlockState state = capBase
                        .setValue(HugeMushroomBlock.NORTH, dz == -CAP_RADIUS)
                        .setValue(HugeMushroomBlock.SOUTH, dz == CAP_RADIUS)
                        .setValue(HugeMushroomBlock.EAST, dx == CAP_RADIUS)
                        .setValue(HugeMushroomBlock.WEST, dx == -CAP_RADIUS)
                        .setValue(HugeMushroomBlock.UP, !coveredByTopTier)
                        .setValue(HugeMushroomBlock.DOWN, true);

                level.setBlock(pos, state, 3);
                placedAny = true;
            }
        }

        int topY = capCenter.getY() + 1;
        for (int dx = -TOP_RADIUS; dx <= TOP_RADIUS; dx++) {
            for (int dz = -TOP_RADIUS; dz <= TOP_RADIUS; dz++) {
                BlockPos pos = new BlockPos(capCenter.getX() + dx, topY, capCenter.getZ() + dz);
                if (!canReplace(level, pos)) {
                    continue;
                }

                BlockState state = capBase
                        .setValue(HugeMushroomBlock.NORTH, dz == -TOP_RADIUS)
                        .setValue(HugeMushroomBlock.SOUTH, dz == TOP_RADIUS)
                        .setValue(HugeMushroomBlock.EAST, dx == TOP_RADIUS)
                        .setValue(HugeMushroomBlock.WEST, dx == -TOP_RADIUS)
                        .setValue(HugeMushroomBlock.UP, true)
                        .setValue(HugeMushroomBlock.DOWN, false);

                level.setBlock(pos, state, 3);
                placedAny = true;
            }
        }

        return placedAny;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        return existing.isAir() || existing.canBeReplaced();
    }

    private static Block getBlock(ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
    }
}
