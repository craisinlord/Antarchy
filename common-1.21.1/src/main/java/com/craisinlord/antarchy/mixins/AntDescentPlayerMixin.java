package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.elythia.AntDescent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class AntDescentPlayerMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void antarchy$descentProtection(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (AntDescent.isProtected((ServerPlayer) (Object) this, source)) {
            cir.setReturnValue(false);
        }
    }
}
