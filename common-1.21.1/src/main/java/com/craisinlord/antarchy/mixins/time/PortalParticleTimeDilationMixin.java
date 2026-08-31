package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.client.ClientTimeDilationTicker;
import net.minecraft.client.particle.PortalParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PortalParticle.class)
public abstract class PortalParticleTimeDilationMixin {
    @Unique
    private double antarchy$timeProgress;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowPortalParticle(CallbackInfo ci) {
        ParticlePositionAccessor position = (ParticlePositionAccessor) (Object) this;
        double rate = ClientTimeDilationTicker.rateAt(position.antarchy$getX(), position.antarchy$getY(), position.antarchy$getZ());
        if (rate >= 1.0D) {
            return;
        }
        this.antarchy$timeProgress += rate;
        if (this.antarchy$timeProgress < 1.0D) {
            ci.cancel();
        } else {
            this.antarchy$timeProgress -= 1.0D;
        }
    }
}
