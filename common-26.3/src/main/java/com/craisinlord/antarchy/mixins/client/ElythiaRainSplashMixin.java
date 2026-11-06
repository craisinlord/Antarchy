package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class ElythiaRainSplashMixin {
    @Unique
    private static final float[] ANTARCHY$SPLASH_TINT = {0.8F, 0.4F, 1.0F};

    @WrapOperation(
            method = "tickWeatherEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    )
    private void antarchy$purpleSplash(ClientLevel level, ParticleOptions particle, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, Operation<Void> original) {
        if (particle != ParticleTypes.RAIN || !antarchy$inElythia()) {
            original.call(level, particle, x, y, z, xSpeed, ySpeed, zSpeed);
            return;
        }
        Particle created = Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.GLOW, x, y, z, xSpeed, ySpeed, zSpeed);
        if (created instanceof SingleQuadParticle splash) {
            splash.setColor(ANTARCHY$SPLASH_TINT[0], ANTARCHY$SPLASH_TINT[1], ANTARCHY$SPLASH_TINT[2]);
        }
    }

    @Unique
    private static boolean antarchy$inElythia() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA);
    }
}
