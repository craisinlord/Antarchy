package com.craisinlord.antarchy.content.worldgen.ants;

import com.craisinlord.antarchy.content.block.AntNestBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public abstract class BaseAntNestFeature extends Feature<SimpleBlockConfiguration> {
    protected BaseAntNestFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos nestPos = this.resolveNestPos(level, context.origin());
        BlockPos aboveNestPos = nestPos.above();
        BlockPos supportPos = nestPos.below();
        BlockState nestGroundState = level.getBlockState(nestPos);
        BlockState supportState = level.getBlockState(supportPos);

        if (!level.getBlockState(aboveNestPos).canBeReplaced()) {
            return false;
        }

        if (!this.isValidNestGround(nestGroundState) || !nestGroundState.getFluidState().isEmpty()) {
            return false;
        }

        if (!supportState.isFaceSturdy(level, supportPos, Direction.UP) || !supportState.getFluidState().isEmpty()) {
            return false;
        }

        BlockState nestState = context.config().toPlace().getState(context.random(), nestPos);
        if (nestState.hasProperty(AntNestBlock.FACING)) {
            nestState = nestState.setValue(AntNestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(context.random()));
        }

        if (!nestState.canSurvive(level, nestPos)) {
            return false;
        }

        level.setBlock(nestPos, nestState, 2);
        this.decorateAroundNest(level, nestPos, context.random());
        return true;
    }

    protected BlockPos resolveNestPos(WorldGenLevel level, BlockPos origin) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        return new BlockPos(origin.getX(), y, origin.getZ());
    }

    protected abstract boolean isValidNestGround(BlockState nestGroundState);

    protected abstract void decorateAroundNest(WorldGenLevel level, BlockPos center, RandomSource random);
}
