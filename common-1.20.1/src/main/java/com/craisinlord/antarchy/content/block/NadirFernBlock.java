package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NadirFernBlock extends DirectionalThoraxisFlowerBlock implements BonemealableBlock {
    private static final ResourceLocation LARGE_NADIR_FERN_ID = new ResourceLocation(Antarchy.MODID, "large_nadir_fern");

    public NadirFernBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClientSide) {
        return level.getBlockState(pos.relative(state.getValue(VERTICAL_DIRECTION))).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(VERTICAL_DIRECTION);
        Block largeFern = BuiltInRegistries.BLOCK.get(LARGE_NADIR_FERN_ID);
        BlockState lowerState = largeFern.defaultBlockState()
                .setValue(LargeNadirFernBlock.VERTICAL_DIRECTION, direction)
                .setValue(LargeNadirFernBlock.HALF, LargeNadirFernBlock.Half.BOTTOM);
        LargeNadirFernBlock.placeAt(level, pos, lowerState, Block.UPDATE_ALL);
    }
}
