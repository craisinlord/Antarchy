package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.world.LoadedChunkGuard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.redstone.CollectingNeighborUpdater$MultiNeighborUpdate")
public abstract class NeighborUpdateMultiUnloadedMixin {
    @WrapOperation(method = "runNext", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState antarchy$skipUnloadedNeighbor(Level level, BlockPos pos, Operation<BlockState> original) {
        if (LoadedChunkGuard.isNeighborhoodUnloaded(level, pos)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        return original.call(level, pos);
    }
}
