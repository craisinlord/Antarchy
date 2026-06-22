package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

public class OuranwoodLogBlock extends RotatedPillarBlock {
    public static final MapCodec<OuranwoodLogBlock> CODEC = Block.simpleCodec(OuranwoodLogBlock::new);

    public OuranwoodLogBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public OuranwoodLogBlock(Supplier<Block> strippedBlock, BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<OuranwoodLogBlock> codec() {
        return CODEC;
    }
}
