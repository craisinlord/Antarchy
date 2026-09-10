package com.craisinlord.antarchy.content.block;

import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class GorevineBlock extends SimpleDirectionalVineBlock {
    private static final int MAX_GROWTH_DISTANCE = 7;

    public GorevineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public int maxDistance() {
        return MAX_GROWTH_DISTANCE;
    }
}
