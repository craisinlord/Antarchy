package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TorchflowerBushBlock extends MilkweedBlock {
    public static final MapCodec<TorchflowerBushBlock> CODEC = Block.simpleCodec(TorchflowerBushBlock::new);

    public TorchflowerBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MilkweedBlock> codec() {
        return (MapCodec<MilkweedBlock>) (MapCodec<?>) CODEC;
    }
}
