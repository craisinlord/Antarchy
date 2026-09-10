package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Fills the low, open main-side Thoraxis basin with source ichor during chunk generation.
 * The feature works one chunk at a time so it does not depend on runtime fluid spreading.
 */
public final class ThoraxisIchorLakeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int LAKE_FLOOR_Y = 1;
    private static final int LAKE_SURFACE_Y = 8;
    private static final ResourceLocation ICHOR_BLOCK_ID =
            new ResourceLocation(Antarchy.MODID, "ichor");

    public ThoraxisIchorLakeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockState ichorState = BuiltInRegistries.BLOCK.get(ICHOR_BLOCK_ID).defaultBlockState();
        if (ichorState.isAir() || ichorState.getFluidState().isEmpty()) {
            return false;
        }

        int chunkMinX = origin.getX() >> 4 << 4;
        int chunkMinZ = origin.getZ() >> 4 << 4;
        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                for (int y = LAKE_FLOOR_Y; y <= LAKE_SURFACE_Y; y++) {
                    mutable.set(x, y, z);
                    BlockState current = level.getBlockState(mutable);
                    if (!isOpenForLake(current)) {
                        continue;
                    }

                    level.setBlock(mutable, ichorState, 2);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private static boolean isOpenForLake(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.blocksMotion();
    }
}
