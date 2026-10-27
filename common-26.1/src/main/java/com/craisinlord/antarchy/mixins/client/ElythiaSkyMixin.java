package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.CavarynSkyRenderer;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.craisinlord.antarchy.content.client.renderer.ThoraxisUndersideSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class ElythiaSkyMixin {
    @Unique
    private SkyRenderState antarchy$skyState;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void antarchy$extractAntarchySky(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
        this.antarchy$skyState = state;
        ThoraxisUndersideSkyRenderer.extract(level, camera, partialTicks, state);
        if (ThoraxisUndersideSkyRenderer.shouldRender(state)) {
            state.skybox = DimensionType.Skybox.OVERWORLD;
        }
        ElythiaSkyRenderer.extract(level, camera, partialTicks, state);
        CavarynSkyRenderer.extract(level, camera, partialTicks, state);
    }

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void antarchy$renderAntarchySky(int skyColor, CallbackInfo ci) {
        SkyRenderState state = this.antarchy$skyState;
        if (state == null) {
            return;
        }
        if (ThoraxisUndersideSkyRenderer.shouldRender(state)) {
            ci.cancel();
            ThoraxisUndersideSkyRenderer.render(state);
        } else if (ElythiaSkyRenderer.shouldRender(state)) {
            ci.cancel();
            ElythiaSkyRenderer.render(state);
        } else if (CavarynSkyRenderer.shouldRender(state)) {
            ci.cancel();
            CavarynSkyRenderer.render(state);
        }
    }

    @Inject(method = {"renderSunriseAndSunset", "renderSunMoonAndStars", "renderDarkDisc"}, at = @At("HEAD"), cancellable = true)
    private void antarchy$skipVanillaSkyLayers(CallbackInfo ci) {
        if (this.antarchy$isAntarchySky()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean antarchy$isAntarchySky() {
        SkyRenderState state = this.antarchy$skyState;
        return state != null && (ThoraxisUndersideSkyRenderer.shouldRender(state)
                || ElythiaSkyRenderer.shouldRender(state)
                || CavarynSkyRenderer.shouldRender(state));
    }
}
