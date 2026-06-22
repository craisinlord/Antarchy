package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class AntiwaterFogMixin {
//    @Inject(method = "setupColor", at = @At("TAIL"))
//    private static void antarchy$overrideAntiwaterColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, CallbackInfo ci) {
//        if (!antarchy$isInAntiwater(camera)) {
//            return;
//        }
//
//        RenderSystem.clearColor(0.52F, 0.06F, 0.06F, 0.0F);
//    }
//
//    @Inject(method = "setupFog", at = @At("TAIL"))
//    private static void antarchy$overrideAntiwaterFog(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
//        if (!antarchy$isInAntiwater(camera)) {
//            return;
//        }
//
//        RenderSystem.setShaderFogStart(0.5F);
//        RenderSystem.setShaderFogEnd(Math.min(renderDistance, 12.0F));
//        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
//    }
//
//    private static boolean antarchy$isInAntiwater(Camera camera) {
//        Entity entity = camera.getEntity();
//        if (entity == null || entity.level() == null) {
//            return false;
//        }
//
//        BlockPos pos = BlockPos.containing(camera.getPosition());
//        FluidState fluidState = entity.level().getFluidState(pos);
//        return PotentNyxiteBlock.isAntiwater(fluidState);
//    }
}
