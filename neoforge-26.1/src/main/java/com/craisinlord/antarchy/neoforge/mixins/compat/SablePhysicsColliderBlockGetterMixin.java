package com.craisinlord.antarchy.neoforge.mixins.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ryanhcode.sable.physics.impl.rapier.collider.PhysicsColliderBlockGetter", remap = false)
public class SablePhysicsColliderBlockGetterMixin {
    @Shadow
    private BlockState state;

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true, remap = false)
    private void antarchy$nullSafeFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        if (this.state == null) {
            cir.setReturnValue(Fluids.EMPTY.defaultFluidState());
        }
    }
}
