package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Supplier;

public class BloodCrystalGrowthBlock extends Block {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final Supplier<? extends Block> smallBud;
    private final Supplier<? extends Block> mediumBud;
    private final Supplier<? extends Block> largeBud;
    private final Supplier<? extends Block> cluster;

    public BloodCrystalGrowthBlock(
            Properties properties,
            Supplier<? extends Block> smallBud,
            Supplier<? extends Block> mediumBud,
            Supplier<? extends Block> largeBud,
            Supplier<? extends Block> cluster
    ) {
        super(properties);
        this.smallBud = smallBud;
        this.mediumBud = mediumBud;
        this.largeBud = largeBud;
        this.cluster = cluster;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.is(this.cluster.get());
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Block nextBlock = null;

        if (canGrowInto(targetState)) {
            nextBlock = this.smallBud.get();
        } else if (isStage(targetState, this.smallBud.get(), direction)) {
            nextBlock = this.mediumBud.get();
        } else if (isStage(targetState, this.mediumBud.get(), direction)) {
            nextBlock = this.largeBud.get();
        } else if (isStage(targetState, this.largeBud.get(), direction)) {
            nextBlock = this.cluster.get();
        }

        if (nextBlock == null) {
            return;
        }

        FluidState fluidState = targetState.getFluidState();
        BlockState nextState = nextBlock.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER && fluidState.getAmount() == 8);
        level.setBlockAndUpdate(targetPos, nextState);
    }

    private static boolean canGrowInto(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }

    private static boolean isStage(BlockState state, Block block, Direction direction) {
        return state.is(block) && state.hasProperty(AmethystClusterBlock.FACING) && state.getValue(AmethystClusterBlock.FACING) == direction;
    }
}

