package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SpiralingVinesPlantBlock extends GrowingPlantBodyBlock {
    public static final MapCodec<SpiralingVinesPlantBlock> CODEC = simpleCodec(SpiralingVinesPlantBlock::new);
    public static Supplier<GrowingPlantHeadBlock> HEAD_BLOCK;

    public SpiralingVinesPlantBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.UP, SpiralingVinesBlock.SHAPE, false);
    }

    @Override
    public MapCodec<SpiralingVinesPlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return HEAD_BLOCK.get();
    }
}
