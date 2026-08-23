package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TruffaloLogBlock extends RotatedPillarBlock {
    public static final MapCodec<TruffaloLogBlock> CODEC = Block.simpleCodec(TruffaloLogBlock::new);

    private static final VoxelShape SHAPE_Y = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    private static final VoxelShape SHAPE_X = Block.box(0.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    private static final VoxelShape SHAPE_Z = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 16.0D);

    public TruffaloLogBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TruffaloLogBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    private static VoxelShape shapeFor(BlockState state) {
        return switch (state.getValue(AXIS)) {
            case X -> SHAPE_X;
            case Z -> SHAPE_Z;
            default -> SHAPE_Y;
        };
    }
}
