package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SpiralingVinesBlock extends SimpleDirectionalVineBlock {
    public static final MapCodec<SpiralingVinesBlock> CODEC = simpleCodec(SpiralingVinesBlock::new);

    public SpiralingVinesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
}
