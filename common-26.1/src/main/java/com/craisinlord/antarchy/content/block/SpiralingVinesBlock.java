package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SpiralingVinesBlock extends SimpleDirectionalVineBlock {
    public static final MapCodec<SpiralingVinesBlock> CODEC = simpleCodec(SpiralingVinesBlock::new);

    public SpiralingVinesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }
}
