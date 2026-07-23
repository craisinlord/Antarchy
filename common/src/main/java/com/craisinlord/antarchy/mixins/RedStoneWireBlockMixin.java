package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public class RedStoneWireBlockMixin {
    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private static void antarchy$rejectBluestone(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(AntarchyTags.Blocks.BLUESTONE_COMPONENTS)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void antarchy$rejectBluestone(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(AntarchyTags.Blocks.BLUESTONE_COMPONENTS)) {
            cir.setReturnValue(false);
        }
    }
}
