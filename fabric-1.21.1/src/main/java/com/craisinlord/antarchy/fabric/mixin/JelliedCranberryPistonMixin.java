package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.block.JelliedCranberryBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonStructureResolver.class)
public abstract class JelliedCranberryPistonMixin {
    @Inject(method = "isSticky", at = @At("HEAD"), cancellable = true)
    private static void antarchy$jelliedCranberryIsSticky(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof JelliedCranberryBlock) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canStickToEachOther", at = @At("HEAD"), cancellable = true)
    private static void antarchy$jelliedCranberryStickRules(BlockState first, BlockState second, CallbackInfoReturnable<Boolean> cir) {
        if (first.getBlock() instanceof JelliedCranberryBlock jelly && !jelly.canStickTo(first, second)) {
            cir.setReturnValue(false);
        } else if (second.getBlock() instanceof JelliedCranberryBlock jelly && !jelly.canStickTo(second, first)) {
            cir.setReturnValue(false);
        }
    }
}
