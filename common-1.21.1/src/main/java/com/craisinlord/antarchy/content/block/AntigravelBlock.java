package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AntigravelBlock extends Block {
    public static final MapCodec<AntigravelBlock> CODEC = simpleCodec(AntigravelBlock::new);

    private static final int SETTLE_DELAY = 2;

    public AntigravelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AntigravelBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, SETTLE_DELAY);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, this, SETTLE_DELAY);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);
        boolean passable = above.isAir() || above.getCollisionShape(level, abovePos).isEmpty();
        if (!passable) {
            return;
        }
        UpwardFallingBlockEntity.fallUp(level, pos, state, false);
    }
}
