package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseCoralFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluids;

public class StarCoralFanBlock extends BaseCoralFanBlock {
    public static final MapCodec<StarCoralFanBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(StarCoralBlock.DEAD_CORAL_FIELD.forGetter(block -> block.deadBlock), propertiesCodec())
                    .apply(instance, StarCoralFanBlock::new)
    );
    private final Block deadBlock;

    public StarCoralFanBlock(Block deadBlock, Properties properties) {
        super(properties);
        this.deadBlock = deadBlock;
    }

    @Override
    public MapCodec<StarCoralFanBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        this.tryScheduleDieTick(state, level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!scanForWater(state, level, pos)) {
            level.setBlock(pos, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, false), 2);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            this.tryScheduleDieTick(state, level, pos);
            if (state.getValue(WATERLOGGED)) {
                level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }

            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
    }
}
