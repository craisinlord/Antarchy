package com.craisinlord.antarchy.content.worldgen.ants;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class TermiteNestFeature extends SurfaceAntNestFeature {
    private static final ResourceLocation MYRMITE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "myrmite");
    private static final ResourceLocation BROODSTONE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "broodstone");

    public TermiteNestFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean isValidNestGround(BlockState nestGroundState) {
        return super.isValidNestGround(nestGroundState)
                || nestGroundState.is(getBlock(MYRMITE_ID))
                || nestGroundState.is(getBlock(BROODSTONE_ID));
    }

    @Override
    protected void decorateAroundNest(WorldGenLevel level, BlockPos center, RandomSource random) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (xOffset == 0 && zOffset == 0) {
                    continue;
                }

                if (Math.abs(xOffset) == 1 && Math.abs(zOffset) == 1 && random.nextBoolean()) {
                    continue;
                }

                BlockPos targetPos = center.offset(xOffset, 0, zOffset);
                BlockState targetState = level.getBlockState(targetPos);
                if (this.isValidNestGround(targetState)) {
                    level.setBlock(targetPos, Blocks.ROOTED_DIRT.defaultBlockState(), 2);
                }
            }
        }
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
