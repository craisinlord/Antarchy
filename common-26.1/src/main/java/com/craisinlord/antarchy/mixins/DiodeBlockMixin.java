package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.block.BluestoneComparatorBlock;
import com.craisinlord.antarchy.content.block.BluestoneRepeaterBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiodeBlock.class)
public class DiodeBlockMixin {
    @Inject(method = "isDiode", at = @At("HEAD"), cancellable = true)
    private static void antarchy$ignoreBluestoneDiodes(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof BluestoneRepeaterBlock || state.getBlock() instanceof BluestoneComparatorBlock) {
            cir.setReturnValue(false);
        }
    }
}
