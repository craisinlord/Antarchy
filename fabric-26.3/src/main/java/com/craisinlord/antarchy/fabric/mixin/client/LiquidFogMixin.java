package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.fluid.BileLiquidBlock;
import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class LiquidFogMixin {
    private static final int FLUID_NONE = 0;
    private static final int FLUID_BILE = 1;
    private static final int FLUID_ANTIWATER = 2;
    private static final int FLUID_ICHOR = 3;
    private static final int FLUID_LUMEN = 4;

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void antarchy$overrideLiquidFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        int fluidKind = antarchy$getFluidKind(camera);
        if (fluidKind == FLUID_NONE) {
            return;
        }

        FogData fog = cir.getReturnValue();
        float renderDistance = renderDistanceInChunks * 16;
        if (fluidKind == FLUID_BILE) {
            fog.color.set(0.52F, 0.58F, 0.08F, 1.0F);
            fog.environmentalStart = 0.2F;
            fog.environmentalEnd = Math.min(renderDistance, 4.5F);
        } else if (fluidKind == FLUID_ANTIWATER) {
            fog.color.set(0.48F, 0.06F, 0.06F, 1.0F);
            fog.environmentalStart = 0.18F;
            fog.environmentalEnd = Math.min(renderDistance, 3.5F);
        } else if (fluidKind == FLUID_ICHOR) {
            fog.color.set(0.18F, 0.01F, 0.02F, 1.0F);
            fog.environmentalStart = 0.08F;
            fog.environmentalEnd = Math.min(renderDistance, 2.25F);
        } else if (fluidKind == FLUID_LUMEN) {
            fog.color.set(0.42F, 0.82F, 1.0F, 1.0F);
            fog.environmentalStart = 0.3F;
            fog.environmentalEnd = Math.min(renderDistance, 6.0F);
        }
        fog.skyEnd = fog.environmentalEnd;
        fog.cloudEnd = fog.environmentalEnd;
    }

    private static int antarchy$getFluidKind(Camera camera) {
        Entity entity = camera.entity();
        if (entity == null || entity.level() == null) {
            return FLUID_NONE;
        }

        BlockPos pos = BlockPos.containing(camera.position());
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
