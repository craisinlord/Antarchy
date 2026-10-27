package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class BiowartBlock extends Block implements BonemealableBlock {
    public static final MapCodec<BiowartBlock> CODEC = Block.simpleCodec(BiowartBlock::new);
    private static Supplier<? extends Block> biowartTendrils;

    public BiowartBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static void bindTendrils(Supplier<? extends Block> tendrilsSupplier) {
        biowartTendrils = tendrilsSupplier;
    }

    @Override
    public MapCodec<BiowartBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir() || hasSpreadTarget(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        placeTendrils(level, pos);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 28; i++) {
            cursor.setWithOffset(pos, random.nextInt(9) - 4, random.nextInt(3) - 1, random.nextInt(9) - 4);
            BlockState target = level.getBlockState(cursor);
            if (isBiomite(target)) {
                level.setBlock(cursor, this.defaultBlockState(), 3);
                placeTendrils(level, cursor);
            }
        }
    }

    private static boolean hasSpreadTarget(LevelReader level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -3; z <= 3; z++) {
                    cursor.setWithOffset(pos, x, y, z);
                    if (isBiomite(level.getBlockState(cursor))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isBiomite(BlockState state) {
        return state.is(AntarchyTags.Blocks.BIOWART_REPLACEABLE);
    }

    private static void placeTendrils(ServerLevel level, BlockPos pos) {
        if (biowartTendrils != null && level.isEmptyBlock(pos.above())) {
            level.setBlock(pos.above(), biowartTendrils.get().defaultBlockState(), 3);
        }
    }
}
