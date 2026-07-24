package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class AntimetalRailBlock extends AbstractAntimetalRailBlock {
    public static final MapCodec<AntimetalRailBlock> CODEC = simpleCodec(AntimetalRailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    public AntimetalRailBlock(BlockBehaviour.Properties properties) {
        super(false, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, false));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }
}
