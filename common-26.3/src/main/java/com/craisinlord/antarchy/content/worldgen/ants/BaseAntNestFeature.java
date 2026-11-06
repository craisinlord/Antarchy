package com.craisinlord.antarchy.content.worldgen.ants;

import com.craisinlord.antarchy.content.block.AntNestBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class BaseAntNestFeature implements Feature {
    private final Holder<BlockStateProvider> toPlace;
    private final boolean scheduleTick;

    protected BaseAntNestFeature(Holder<BlockStateProvider> toPlace, boolean scheduleTick) {
        this.toPlace = toPlace;
        this.scheduleTick = scheduleTick;
    }

    protected static <T extends BaseAntNestFeature> MapCodec<T> createCodec(BiFunction<Holder<BlockStateProvider>, Boolean, T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockStateProvider.CODEC.fieldOf("to_place").forGetter(BaseAntNestFeature::toPlace),
                Codec.BOOL.optionalFieldOf("schedule_tick", false).forGetter(BaseAntNestFeature::scheduleTick)
        ).apply(instance, factory::apply));
    }

    public Holder<BlockStateProvider> toPlace() {
        return this.toPlace;
    }

    public boolean scheduleTick() {
        return this.scheduleTick;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos nestPos = this.resolveNestPos(level, origin);
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

        BlockState nestState = this.toPlace.value().getState(level, random, nestPos);
        if (nestState.hasProperty(AntNestBlock.FACING)) {
            nestState = nestState.setValue(AntNestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
        }

        if (!nestState.canSurvive(level, nestPos)) {
            return false;
        }

        level.setBlock(nestPos, nestState, 2);
        this.decorateAroundNest(level, nestPos, random);
        return true;
    }

    protected BlockPos resolveNestPos(WorldGenLevel level, BlockPos origin) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        return new BlockPos(origin.getX(), y, origin.getZ());
    }

    protected abstract boolean isValidNestGround(BlockState nestGroundState);

    protected abstract void decorateAroundNest(WorldGenLevel level, BlockPos center, RandomSource random);
}
