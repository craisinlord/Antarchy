package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.GlimmeringReedBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class GlimmeringReedFeature implements Feature {
    public static final MapCodec<GlimmeringReedFeature> CODEC = MapCodec.unit(GlimmeringReedFeature::new);

    @Override
    public MapCodec<GlimmeringReedFeature> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockState reed = AntarchyObjects.GLIMMERING_REED.get().defaultBlockState();
        boolean placedAny = false;

        for (int i = 0; i < 18; i++) {
            int x = origin.getX() + random.nextInt(17) - 8;
            int z = origin.getZ() + random.nextInt(17) - 8;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos groundPos = new BlockPos(x, surfaceY, z);
            BlockPos reedPos = groundPos.above();
            if (!level.getBlockState(groundPos).isSolid() || !level.isEmptyBlock(reedPos)
                    || !level.isEmptyBlock(reedPos.above()) || !reed.canSurvive(level, reedPos)) {
                continue;
            }

            boolean besideLumen = false;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (level.getFluidState(groundPos.relative(direction)).is(AntarchyTags.Fluids.LUMEN)
                        && level.getFluidState(groundPos.relative(direction)).isSource()) {
                    besideLumen = true;
                    break;
                }
            }
            if (!besideLumen) {
                continue;
            }

            DoublePlantBlock.placeAt(level, reed, reedPos, 2);
            placedAny = true;
        }
        return placedAny;
    }
}
