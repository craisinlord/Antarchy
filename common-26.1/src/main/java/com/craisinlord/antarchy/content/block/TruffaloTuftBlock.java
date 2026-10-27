package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TruffaloTuftBlock extends Block {
    public static final MapCodec<TruffaloTuftBlock> CODEC = Block.simpleCodec(TruffaloTuftBlock::new);
    private static final float SHEARS_DESTROY_SPEED = 10.0F;

    public TruffaloTuftBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TruffaloTuftBlock> codec() {
        return CODEC;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.getMainHandItem().getItem() instanceof ShearsItem) {
            return SHEARS_DESTROY_SPEED;
        }

        return super.getDestroyProgress(state, player, level, pos);
    }
}
