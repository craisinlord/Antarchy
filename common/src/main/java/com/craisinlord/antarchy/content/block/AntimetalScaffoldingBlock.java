package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AntimetalScaffoldingBlock extends ScaffoldingBlock {

    public static final MapCodec<AntimetalScaffoldingBlock> CODEC = simpleCodec(AntimetalScaffoldingBlock::new);

//    @Override
//    public MapCodec<AntimetalScaffoldingBlock> codec() { return CODEC; }

    public AntimetalScaffoldingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
