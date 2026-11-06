package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.CavarynSkyRenderer;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.content.client.renderer.ThoraxisUndersideSkyRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class ElythiaSkyMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void antarchy$extractAntarchySky(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
        ThoraxisUndersideSkyRenderer.extract(level, camera, partialTicks, state);
        if (ThoraxisUndersideSkyRenderer.shouldRender(state)) {
            state.skybox = DimensionType.Skybox.OVERWORLD;
        }
        ElythiaSkyRenderer.extract(level, camera, partialTicks, state);
        CavarynSkyRenderer.extract(level, camera, partialTicks, state);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void antarchy$renderAntarchySky(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
        if (state == null) {
            return;
        }
        if (ThoraxisUndersideSkyRenderer.shouldRender(state)) {
            ci.cancel();
            RenderSystem.setShaderFog(skyFog);
            ThoraxisUndersideSkyRenderer.render(state);
        } else if (ElythiaSkyRenderer.shouldRender(state)) {
            ci.cancel();
            RenderSystem.setShaderFog(skyFog);
            ElythiaSkyRenderer.render(state);
        } else if (CavarynSkyRenderer.shouldRender(state)) {
            ci.cancel();
            RenderSystem.setShaderFog(skyFog);
            CavarynSkyRenderer.render(state);
        }
    }
}
