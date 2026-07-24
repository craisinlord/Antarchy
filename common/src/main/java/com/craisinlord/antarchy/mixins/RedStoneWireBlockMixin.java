package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.BluestoneBlock;
import com.craisinlord.antarchy.content.block.BluestoneComparatorBlock;
import com.craisinlord.antarchy.content.block.BluestoneRepeaterBlock;
import com.craisinlord.antarchy.content.block.BluestoneTorchBlock;
import com.craisinlord.antarchy.content.block.BluestoneWireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public class RedStoneWireBlockMixin {
    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private static void antarchy$rejectBluestone(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (antarchy$isBluestoneBlock(state)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void antarchy$rejectBluestone(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (antarchy$isBluestoneBlock(state)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getConnectingSide", at = @At("HEAD"), cancellable = true)
    private void antarchy$blockBluestoneSide(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<RedstoneSide> cir) {
        BlockPos sidePos = pos.relative(direction);
        if (antarchy$isBluestoneConnection(level, sidePos)) {
            cir.setReturnValue(RedstoneSide.NONE);
        }
    }

    @Inject(method = "calculateTargetStrength", at = @At("HEAD"), cancellable = true)
    private void antarchy$ignoreBluestonePower(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.antarchy$getReceivedRedstonePower(level, pos));
    }

    private int antarchy$getReceivedRedstonePower(Level level, BlockPos pos) {
        int signal = 0;
        for (Direction direction : Direction.values()) {
            BlockPos sidePos = pos.relative(direction);
            BlockState sideState = level.getBlockState(sidePos);
            signal = Math.max(signal, this.antarchy$getSignalFromState(level, sidePos, sideState, direction));
            if (signal >= 15) {
                return 15;
            }
            BlockPos abovePos = sidePos.above();
            BlockPos belowPos = sidePos.below();
            BlockState aboveState = level.getBlockState(abovePos);
            BlockState belowState = level.getBlockState(belowPos);
            if (sideState.isRedstoneConductor(level, sidePos)) {
                if (!aboveState.isRedstoneConductor(level, abovePos)) {
                    signal = Math.max(signal, this.antarchy$getSignalFromState(level, abovePos, aboveState, direction));
                }
            } else {
                signal = Math.max(signal, this.antarchy$getSignalFromState(level, belowPos, belowState, direction));
            }
            if (signal >= 15) {
                return 15;
            }
        }
        return signal;
    }

    private int antarchy$getSignalFromState(Level level, BlockPos pos, BlockState state, Direction direction) {
        if (antarchy$isBluestoneBlock(state)) {
            return 0;
        }
        return state.getSignal(level, pos, direction);
    }

    private static boolean antarchy$isBluestoneBlock(BlockState state) {
        return state.is(AntarchyTags.Blocks.BLUESTONE_COMPONENTS)
                || state.getBlock() instanceof BluestoneWireBlock
                || state.getBlock() instanceof BluestoneRepeaterBlock
                || state.getBlock() instanceof BluestoneComparatorBlock
                || state.getBlock() instanceof BluestoneTorchBlock
                || state.getBlock() instanceof BluestoneBlock;
    }

    private static boolean antarchy$isBluestoneConnection(BlockGetter level, BlockPos pos) {
        return antarchy$isBluestoneBlock(level.getBlockState(pos))
                || antarchy$isBluestoneBlock(level.getBlockState(pos.above()))
                || antarchy$isBluestoneBlock(level.getBlockState(pos.below()));
    }
}
