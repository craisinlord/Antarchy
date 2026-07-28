package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SignalSavingBulbBlock extends Block {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty BRIGHTNESS = IntegerProperty.create("brightness", 0, 15);

    public SignalSavingBulbBlock(BlockBehaviour.Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIT) ? state.getValue(BRIGHTNESS) : 0));
        this.registerDefaultState(this.defaultBlockState()
                .setValue(LIT, false)
                .setValue(POWERED, false)
                .setValue(BRIGHTNESS, 0));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (oldState.getBlock() != state.getBlock() && level instanceof ServerLevel serverLevel) {
            this.checkAndFlip(state, serverLevel, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (level instanceof ServerLevel serverLevel) {
            this.checkAndFlip(state, serverLevel, pos);
        }
    }

    public void checkAndFlip(BlockState state, ServerLevel level, BlockPos pos) {
        boolean receiving = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (receiving == wasPowered) return;

        BlockState newState = state;

        if (!wasPowered) {
            boolean isLit = state.getValue(LIT);
            if (!isLit) {
                int strength = level.getBestNeighborSignal(pos);
                newState = state.setValue(LIT, true).setValue(BRIGHTNESS, Math.max(strength, 1));
                level.playSound((Player) null, pos, SoundEvents.COPPER_BULB_TURN_ON, SoundSource.BLOCKS);
            } else {
                newState = state.setValue(LIT, false).setValue(BRIGHTNESS, 0);
                level.playSound((Player) null, pos, SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
            }
        }

        level.setBlock(pos, newState.setValue(POWERED, receiving), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POWERED, BRIGHTNESS);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LIT) ? state.getValue(BRIGHTNESS) : 0;
    }
}
