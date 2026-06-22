package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockBehaviour.BlockStateBase.class)
/*
 * Makes torchflowers glow when the config is on.
 */
public abstract class TorchflowerBlockStateMixin {
    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void antarchy$makeTorchflowerGlow(CallbackInfoReturnable<Integer> cir) {
        BlockState state = (BlockState) (Object) this;
        if (AntarchySettings.glowingTorchflowers() && state.is(Blocks.TORCHFLOWER)) {
            cir.setReturnValue(10);
        }
    }
}
