package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.MoltingVinesBlock;
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

public final class MoltingVinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation MOLTING_VINES_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "molting_vines");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int VERTICAL_SCAN = 12;
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 6;
    private static final int BROODFRUIT_CHANCE = 3;

    public MoltingVinesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Block block = getBlock(MOLTING_VINES_ID);
        if (!(block instanceof MoltingVinesBlock moltingVinesBlock)) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos anchorPos = findAnchor(level, context.origin(), random);
        if (anchorPos == null) {
            return false;
        }

        return placeColumn(level, moltingVinesBlock, anchorPos, random);
    }

    private static BlockPos findAnchor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minY = level.getMinBuildHeight() + 2;
        int maxY = level.getMaxBuildHeight() - 2;
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            startY = Math.max(minY, Math.min(maxY, startY));

            for (int y = startY; y >= Math.max(minY, startY - VERTICAL_SCAN); y--) {
                mutable.set(x, y, z);
                if (!level.getBlockState(mutable).canBeReplaced()) {
                    continue;
                }
                if (AntarchyFluidChecks.hasBileNearby(level, mutable, 1)) {
                    continue;
                }
                BlockPos abovePos = mutable.above();
                BlockState aboveState = level.getBlockState(abovePos);
                if (!aboveState.isFaceSturdy(level, abovePos, net.minecraft.core.Direction.DOWN)) {
                    continue;
                }
                if (!level.getBlockState(mutable.below()).canBeReplaced()) {
                    continue;
                }
                return mutable.immutable();
            }
        }
        return null;
    }

    private static boolean placeColumn(WorldGenLevel level, MoltingVinesBlock block, BlockPos anchorPos, RandomSource random) {
        int maxLength = MIN_LENGTH + random.nextInt(MAX_LENGTH - MIN_LENGTH + 1);
        int length = 0;
        BlockPos.MutableBlockPos mutable = anchorPos.mutable();
        while (length < maxLength && mutable.getY() > level.getMinBuildHeight()) {
            BlockState existing = level.getBlockState(mutable);
            if (!existing.canBeReplaced()) {
                break;
            }
            if (AntarchyFluidChecks.hasBileNearby(level, mutable, 1)) {
                break;
            }
            length++;
            mutable.move(0, -1, 0);
        }

        if (length < MIN_LENGTH) {
            return false;
        }

        int fruitIndex = -1;
        if (length >= 3 && random.nextInt(BROODFRUIT_CHANCE) == 0) {
            fruitIndex = 1 + random.nextInt(length - 2);
        }

        for (int offset = 0; offset < length; offset++) {
            BlockPos pos = anchorPos.below(offset);
            BlockState state = block.defaultBlockState()
                    .setValue(MoltingVinesBlock.DISTANCE, Math.min(offset, MoltingVinesBlock.DISTANCE.getPossibleValues().stream().max(Integer::compareTo).orElse(6)))
                    .setValue(MoltingVinesBlock.BOTTOM_CAP, offset == length - 1)
                    .setValue(MoltingVinesBlock.BROODFRUIT, offset == fruitIndex);
            if (!state.canSurvive(level, pos)) {
                return false;
            }
            level.setBlock(pos, state, 3);
        }

        return true;
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
