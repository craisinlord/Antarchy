package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AmberMossBlock extends Block implements BonemealableBlock {
    public static final MapCodec<AmberMossBlock> CODEC = Block.simpleCodec(AmberMossBlock::new);

    public AmberMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AmberMossBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void performBonemeal(net.minecraft.server.level.ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
    }
}
