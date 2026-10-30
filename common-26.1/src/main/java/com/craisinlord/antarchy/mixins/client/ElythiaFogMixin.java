package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ElythiaFog;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class ElythiaFogMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void antarchy$elythiaFogDistance(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker,
                                            float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        if (!AntarchySettings.elythiaFogEnabled() || camera.getFluidInCamera() != FogType.NONE
                || !ElythiaSkyRenderer.shouldRender(level)) {
            return;
        }
        Entity entity = camera.entity();
        if (entity == null || entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        float distance = renderDistanceInChunks * 16.0F;
        FogData fog = cir.getReturnValue();
        fog.environmentalStart = ElythiaFog.start(level, distance, partialTick);
        fog.environmentalEnd = ElythiaFog.end(level, distance, partialTick);
    }
}
