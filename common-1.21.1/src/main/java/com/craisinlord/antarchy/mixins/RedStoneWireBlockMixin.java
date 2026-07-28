package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.BluestoneBlock;
import com.craisinlord.antarchy.content.block.BluestoneComparatorBlock;
import com.craisinlord.antarchy.content.block.BluestoneRepeaterBlock;
import com.craisinlord.antarchy.content.block.BluestoneSignalHelper;
import com.craisinlord.antarchy.content.block.BluestoneTorchBlock;
import com.craisinlord.antarchy.content.block.BluestoneWireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(
            method = "calculateTargetStrength",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBestNeighborSignal(Lnet/minecraft/core/BlockPos;)I"
            )
    )
    private int antarchy$ignoreBluestonePowerForRedstone(Level level, BlockPos pos) {
        int signal = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (antarchy$isBluestoneBlock(neighborState)) {
                continue;
            }
            signal = Math.max(signal, neighborState.getSignal(level, neighborPos, direction));
            if (neighborState.isRedstoneConductor(level, neighborPos)) {
                signal = Math.max(signal, antarchy$getDirectSignalToExcludingBluestone(level, neighborPos));
            }
            if (signal >= 15) {
                return 15;
            }
        }
        return signal;
    }

    private int antarchy$getDirectSignalToExcludingBluestone(Level level, BlockPos pos) {
        int signal = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (antarchy$isBluestoneBlock(neighborState)) {
                continue;
            }
            signal = Math.max(signal, neighborState.getDirectSignal(level, neighborPos, direction));
            if (signal >= 15) {
                return 15;
            }
        }
        return signal;
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
