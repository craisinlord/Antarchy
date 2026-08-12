package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.HangingCreeprootsBlock;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class HangingCreeprootsFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation HANGING_CREEPROOTS_ID = new ResourceLocation(Antarchy.MODID, "hanging_creeproots");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int VERTICAL_SCAN = 12;

    public HangingCreeprootsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Block block = getBlock(HANGING_CREEPROOTS_ID);
        if (!(block instanceof HangingCreeprootsBlock rootsBlock)) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos anchor = findAnchor(level, context.origin(), random);
        if (anchor == null) {
            return false;
        }

        return placeColumn(level, rootsBlock, anchor, random);
    }

    private static BlockPos findAnchor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            startY = Math.max(level.getMinBuildHeight() + 3, Math.min(level.getMaxBuildHeight() - 3, startY));

            for (int y = startY; y <= Math.min(level.getMaxBuildHeight() - 2, startY + VERTICAL_SCAN); y++) {
                mutable.set(x, y, z);
                if (!level.getBlockState(mutable).canBeReplaced()) {
                    continue;
                }
                if (AntarchyFluidChecks.hasBileNearby(level, mutable, 1)) {
                    continue;
                }

                BlockPos abovePos = mutable.above();
                BlockState aboveState = level.getBlockState(abovePos);
                if (aboveState.isFaceSturdy(level, abovePos, net.minecraft.core.Direction.DOWN)) {
                    return mutable.immutable();
                }
            }
        }

        return null;
    }

    private static boolean placeColumn(WorldGenLevel level, HangingCreeprootsBlock rootsBlock, BlockPos startPos, RandomSource random) {
        boolean placedAny = false;
        int length = 1 + random.nextInt(5);

        for (int offset = 0; offset < length; offset++) {
            BlockPos rootsPos = startPos.below(offset);
            if (rootsPos.getY() <= level.getMinBuildHeight()) {
                break;
            }

            BlockState existing = level.getBlockState(rootsPos);
            if (!existing.canBeReplaced() && !existing.is(rootsBlock)) {
                break;
            }
            if (AntarchyFluidChecks.hasBileNearby(level, rootsPos, 1)) {
                break;
            }

            int distance = Math.min(offset, 4);
            boolean bottom = offset == length - 1 || !level.getBlockState(rootsPos.below()).canBeReplaced();
            BlockState state = rootsBlock.defaultBlockState()
                    .setValue(HangingCreeprootsBlock.DISTANCE, distance)
                    .setValue(HangingCreeprootsBlock.BOTTOM, bottom);
            if (!state.canSurvive(level, rootsPos)) {
                break;
            }

            level.setBlock(rootsPos, state, 3);
            placedAny = true;
            if (!bottom && !level.getBlockState(rootsPos.below()).canBeReplaced()) {
                level.setBlock(rootsPos, state.setValue(HangingCreeprootsBlock.BOTTOM, true), 3);
                break;
            }
        }

        return placedAny;
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
