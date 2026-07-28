package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.JumpyBugEggBlock;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class JumpyBugEggCeilingFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation JUMPY_BUG_EGG_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_bug_egg");
    private static final int SEARCH_RADIUS = 8;
    private static final int VERTICAL_SCAN = 24;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int MAX_PLACEMENTS = 3;

    public JumpyBugEggCeilingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Block eggBlock = getBlock(JUMPY_BUG_EGG_ID);
        if (eggBlock == null) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        boolean placedAny = false;
        int placements = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS && placements < MAX_PLACEMENTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(VERTICAL_SCAN * 2 + 1) - VERTICAL_SCAN;
            int minY = Math.max(level.getMinBuildHeight() + 1, startY - VERTICAL_SCAN);
            int maxY = Math.min(level.getMaxBuildHeight() - 2, startY + VERTICAL_SCAN);

            BlockPos spot = null;
            for (int y = maxY; y >= minY; y--) {
                mutable.set(x, y, z);
                if (isValidCeilingSpot(level, mutable)) {
                    spot = mutable.immutable();
                    break;
                }
            }

            if (spot == null) {
                continue;
            }
            BlockState eggState = eggBlock.defaultBlockState()
                    .setValue(JumpyBugEggBlock.HANGING, true)
                    .setValue(JumpyBugEggBlock.ROTATED, random.nextBoolean());
            level.setBlock(spot, eggState, 3);
            placedAny = true;
            placements++;
        }

        return placedAny;
    }

    private static boolean isValidCeilingSpot(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            return false;
        }

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        return aboveState.isFaceSturdy(level, abovePos, Direction.DOWN);
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
