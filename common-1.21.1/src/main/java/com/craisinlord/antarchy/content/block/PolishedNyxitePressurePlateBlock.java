package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PolishedNyxitePressurePlateBlock extends PressurePlateBlock {
    public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");

    private static final VoxelShape CEILING_SHAPE = Block.box(1.0D, 15.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final VoxelShape CEILING_PRESSED_SHAPE = Block.box(1.0D, 15.5D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final AABB CEILING_TOUCH_AABB = new AABB(0.0625D, 0.75D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

    public PolishedNyxitePressurePlateBlock(BlockSetType type, BlockBehaviour.Properties properties) {
        super(type, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(CEILING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CEILING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean ceiling = context.getClickedFace() == Direction.DOWN;
        return this.defaultBlockState().setValue(CEILING, ceiling);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(CEILING)
                ? Block.canSupportCenter(level, pos.above(), Direction.DOWN)
                : super.canSurvive(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(CEILING)) {
            return super.getShape(state, level, pos, context);
        }
        return state.getValue(POWERED) ? CEILING_PRESSED_SHAPE : CEILING_SHAPE;
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getValue(CEILING)) {
            return super.getSignalStrength(level, pos);
        }
        return getEntityCount(level, CEILING_TOUCH_AABB.move(pos), Entity.class) > 0 ? 15 : 0;
    }
}
