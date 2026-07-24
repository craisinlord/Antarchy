package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class AntimetalActivatorRailBlock extends AbstractAntimetalRailBlock {
    public static final MapCodec<AntimetalActivatorRailBlock> CODEC = simpleCodec(AntimetalActivatorRailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public AntimetalActivatorRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends AbstractAntimetalRailBlock> codec() {
        return CODEC;
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, POWERED, WATERLOGGED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        updatePoweredState(level, pos, level.getBlockState(pos));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            updatePoweredState(level, pos, level.getBlockState(pos));
        }
    }

    private void updatePoweredState(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide() || !(state.getBlock() instanceof AntimetalActivatorRailBlock)) {
            return;
        }
        boolean powered = AntimetalPowerHelper.isPoweredWithChain(level, pos, state, SHAPE, AntimetalActivatorRailBlock.class);
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
            AntimetalPowerHelper.notifyAllSignalNeighbors(level, pos);
        }
    }
}
