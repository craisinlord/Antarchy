package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.fluid.BileLiquidBlock;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
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
public abstract class LiquidFogMixin {
    private static final int FLUID_NONE = 0;
    private static final int FLUID_BILE = 1;
    private static final int FLUID_ANTIWATER = 2;
    private static final int FLUID_ICHOR = 3;
    private static final int FLUID_LUMEN = 4;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void antarchy$overrideBileColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, CallbackInfo ci) {
        int fluidKind = antarchy$getFluidKind(camera);
        if (fluidKind == FLUID_NONE) {
            return;
        }

        if (fluidKind == FLUID_BILE) {
            RenderSystem.clearColor(0.52F, 0.58F, 0.08F, 0.0F);
        } else if (fluidKind == FLUID_ANTIWATER) {
            RenderSystem.clearColor(0.48F, 0.06F, 0.06F, 0.0F);
        } else if (fluidKind == FLUID_ICHOR) {
            RenderSystem.clearColor(0.18F, 0.01F, 0.02F, 0.0F);
        } else if (fluidKind == FLUID_LUMEN) {
            RenderSystem.clearColor(0.42F, 0.82F, 1.0F, 0.0F);
        }
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void antarchy$overrideBileFog(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
        int fluidKind = antarchy$getFluidKind(camera);
        if (fluidKind == FLUID_NONE) {
            return;
        }

        if (fluidKind == FLUID_BILE) {
            RenderSystem.setShaderFogStart(0.2F);
            RenderSystem.setShaderFogEnd(Math.min(renderDistance, 4.5F));
        } else if (fluidKind == FLUID_ANTIWATER) {
            RenderSystem.setShaderFogStart(0.18F);
            RenderSystem.setShaderFogEnd(Math.min(renderDistance, 3.5F));
        } else if (fluidKind == FLUID_ICHOR) {
            RenderSystem.setShaderFogStart(0.08F);
            RenderSystem.setShaderFogEnd(Math.min(renderDistance, 2.25F));
        } else if (fluidKind == FLUID_LUMEN) {
            RenderSystem.setShaderFogStart(0.3F);
            RenderSystem.setShaderFogEnd(Math.min(renderDistance, 6.0F));
        }
        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
    }

    private static int antarchy$getFluidKind(Camera camera) {
        Entity entity = camera.getEntity();
        if (entity == null || entity.level() == null) {
            return FLUID_NONE;
        }

        BlockPos pos = BlockPos.containing(camera.getPosition());
        FluidState fluidState = entity.level().getFluidState(pos);
        if (BileLiquidBlock.isBile(fluidState)) {
            return FLUID_BILE;
        }
        if (AntarchyFluidChecks.isAntiwater(fluidState)) {
            return FLUID_ANTIWATER;
        }
        if (AntarchyFluidChecks.isIchor(fluidState)) {
            return FLUID_ICHOR;
        }
        if (AntarchyFluidChecks.isLumen(fluidState)) {
            return FLUID_LUMEN;
        }
        return FLUID_NONE;
    }
}
