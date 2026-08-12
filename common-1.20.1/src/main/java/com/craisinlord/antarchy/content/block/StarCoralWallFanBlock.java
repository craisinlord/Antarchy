package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluids;

public class StarCoralWallFanBlock extends BaseCoralWallFanBlock {
    private final Block deadBlock;

    public StarCoralWallFanBlock(Block deadBlock, Properties properties) {
        super(properties);
        this.deadBlock = deadBlock;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        this.tryScheduleDieTick(state, level, pos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!scanForWater(state, level, pos)) {
            level.setBlock(
                    pos,
                    this.deadBlock.defaultBlockState().setValue(WATERLOGGED, false).setValue(FACING, state.getValue(FACING)),
                    2
            );
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (state.getValue(WATERLOGGED)) {
                level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }

            this.tryScheduleDieTick(state, level, pos);
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
    }
}
