package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ElythiaFog;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class ElythiaFogMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void antarchy$elythiaFogDistance(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
        if (mode != FogRenderer.FogMode.FOG_TERRAIN || thickFog || !AntarchySettings.elythiaFogEnabled()
                || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }
        Entity entity = camera.getEntity();
        if (entity == null || !(entity.level() instanceof ClientLevel level) || !ElythiaSkyRenderer.shouldRender(level)) {
            return;
        }
        if (entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
            return;
        }

        RenderSystem.setShaderFogStart(ElythiaFog.start(level, renderDistance, partialTick));
        RenderSystem.setShaderFogEnd(ElythiaFog.end(level, renderDistance, partialTick));
    }
}
