package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PrimordialLoamBlock extends Block implements BonemealableBlock {

    public PrimordialLoamBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canStayGrassy(level, pos)) {
            Block loam = AntarchyObjects.LOAM.get();
            if (loam != null) {
                level.setBlockAndUpdate(pos, loam.defaultBlockState());
            }
            return;
        }
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
            return;
        }
        Block loam = AntarchyObjects.LOAM.get();
        if (loam == null) {
            return;
        }
        BlockState spread = this.defaultBlockState();
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (!level.isLoaded(target)) {
                continue;
            }
            if (level.getBlockState(target).is(loam) && canStayGrassy(level, target)
                    && level.getFluidState(target.above()).isEmpty()) {
                level.setBlockAndUpdate(target, spread);
            }
        }
    }

    private static boolean canStayGrassy(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getBlock() instanceof SnowLayerBlock && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }
        if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        }
        return !aboveState.isSolidRender(level, above);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        Block grassBlock = AntarchyObjects.PRIMORDIAL_GRASS.get();
        if (grassBlock == null) {
            return;
        }
        BlockState grass = grassBlock.defaultBlockState();
        BlockPos start = pos.above();
        attempts:
        for (int i = 0; i < 128; i++) {
            BlockPos target = start;
            for (int j = 0; j < i / 16; j++) {
                target = target.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                BlockState here = level.getBlockState(target);
                if (!level.getBlockState(target.below()).is(this) || !(here.isAir() || here.is(grassBlock))) {
                    continue attempts;
                }
            }
            if (level.getBlockState(target).isAir() && grass.canSurvive(level, target)) {
                level.setBlock(target, grass, Block.UPDATE_ALL);
            }
        }
    }
}
