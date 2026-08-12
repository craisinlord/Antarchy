package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LotusBlock extends Block {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    private static final VoxelShape SHAPE_STANDING = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 6.5D, 13.0D);
    private static final VoxelShape SHAPE_HANGING = Block.box(3.0D, 9.5D, 3.0D, 13.0D, 16.0D, 13.0D);

    public LotusBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean canHang = canSurvive(this.defaultBlockState().setValue(HANGING, true), level, pos);
        boolean canStand = canSurvive(this.defaultBlockState().setValue(HANGING, false), level, pos);

        if (context.getClickedFace() == Direction.DOWN && canHang) {
            return this.defaultBlockState().setValue(HANGING, true);
        }

        if (canStand) {
            return this.defaultBlockState().setValue(HANGING, false);
        }

        return canHang ? this.defaultBlockState().setValue(HANGING, true) : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? SHAPE_HANGING : SHAPE_STANDING;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HANGING)) {
            BlockPos abovePos = pos.above();
            return level.getBlockState(abovePos).isFaceSturdy(level, abovePos, Direction.DOWN);
        }

        BlockPos belowPos = pos.below();
        return level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction supportDirection = state.getValue(HANGING) ? Direction.UP : Direction.DOWN;
        if (direction == supportDirection && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) != 0) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
        double y = state.getValue(HANGING) ? pos.getY() + 0.2D : pos.getY() + 0.8D;
        double motionY = state.getValue(HANGING) ? -0.02D : 0.015D;
        level.addParticle(AntarchyObjects.LOTUS_POLLEN.get(), x, y, z, 0.0D, motionY, 0.0D);
    }
}
