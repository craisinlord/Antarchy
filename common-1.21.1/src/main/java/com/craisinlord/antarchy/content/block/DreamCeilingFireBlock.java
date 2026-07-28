package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DreamCeilingFireBlock extends SoulFireBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    @SuppressWarnings("rawtypes")
    public static final MapCodec<SoulFireBlock> CODEC = (MapCodec) simpleCodec(DreamCeilingFireBlock::new);

    public DreamCeilingFireBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SoulFireBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(AntarchyTags.Blocks.DREAM_FIRE_BASE_BLOCKS);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
