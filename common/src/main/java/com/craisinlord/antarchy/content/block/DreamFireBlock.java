package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelReader;

public class DreamFireBlock extends SoulFireBlock {
    @SuppressWarnings("rawtypes")
    public static final MapCodec<SoulFireBlock> CODEC = (MapCodec) simpleCodec(DreamFireBlock::new);

    public DreamFireBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SoulFireBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS);
    }
}
