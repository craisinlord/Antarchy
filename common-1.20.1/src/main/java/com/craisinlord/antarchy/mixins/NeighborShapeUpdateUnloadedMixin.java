package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.world.LoadedChunkGuard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.redstone.CollectingNeighborUpdater$ShapeUpdate")
public abstract class NeighborShapeUpdateUnloadedMixin {
    @WrapOperation(method = "runNext", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/redstone/NeighborUpdater;executeShapeUpdate(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;II)V"))
    private void antarchy$skipUnloadedShape(LevelAccessor level, Direction direction, BlockState neighborState, BlockPos pos,
                                           BlockPos neighborPos, int flags, int limit, Operation<Void> original) {
        if (!LoadedChunkGuard.isNeighborhoodUnloaded(level, pos)) {
            original.call(level, direction, neighborState, pos, neighborPos, flags, limit);
        }
    }
}
