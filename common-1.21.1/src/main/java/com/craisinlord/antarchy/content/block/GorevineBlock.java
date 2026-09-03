package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class GorevineBlock extends SimpleDirectionalVineBlock {
    public static final MapCodec<GorevineBlock> CODEC = simpleCodec(GorevineBlock::new);
    private static final int MAX_GROWTH_DISTANCE = 7;

    public GorevineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected int maxDistance() {
        return MAX_GROWTH_DISTANCE;
    }

    @Override
    public MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
}
