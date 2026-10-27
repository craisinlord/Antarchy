package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.transit.AntTransit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class AntTransitPlayerMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void antarchy$descentProtection(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (AntTransit.isProtected((ServerPlayer) (Object) this, source)) {
            cir.setReturnValue(false);
        }
    }
}
