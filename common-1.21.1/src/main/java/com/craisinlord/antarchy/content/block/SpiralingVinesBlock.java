package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpiralingVinesBlock extends GrowingPlantHeadBlock {
    public static final MapCodec<SpiralingVinesBlock> CODEC = simpleCodec(SpiralingVinesBlock::new);
    public static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    public static Supplier<Block> BODY_BLOCK;

    public SpiralingVinesBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.UP, SHAPE, false, 0.1D);
    }

    @Override
    public MapCodec<SpiralingVinesBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return 1;
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }

    @Override
    protected Block getBodyBlock() {
        return BODY_BLOCK.get();
    }
}
