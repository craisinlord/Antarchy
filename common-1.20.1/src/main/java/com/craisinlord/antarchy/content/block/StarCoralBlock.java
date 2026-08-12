package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FluidState;

public class StarCoralBlock extends Block {
    private final Block deadBlock;

    public StarCoralBlock(Block deadBlock, Properties properties) {
        super(properties);
        this.deadBlock = deadBlock;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.scanForWater(level, pos)) {
            level.setBlock(pos, this.deadBlock.defaultBlockState(), 2);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!this.scanForWater(level, pos)) {
            level.scheduleTick(pos, this, 60 + level.getRandom().nextInt(40));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    protected boolean scanForWater(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            FluidState fluidState = level.getFluidState(pos.relative(direction));
            if (fluidState.is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!this.scanForWater(context.getLevel(), context.getClickedPos())) {
            context.getLevel().scheduleTick(context.getClickedPos(), this, 60 + context.getLevel().getRandom().nextInt(40));
        }

        return this.defaultBlockState();
    }
}
