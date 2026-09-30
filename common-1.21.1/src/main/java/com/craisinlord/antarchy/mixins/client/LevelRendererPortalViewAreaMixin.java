package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalViewAreaManager;
import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererPortalViewAreaMixin {
    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"), require = 0)
    private void antarchy$suppressPortalWeatherSound(ClientLevel level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay) {
        if (PortalGunPortalRenderState.getContext() == null) {
            level.playLocalSound(pos, sound, source, volume, pitch, distanceDelay);
        }
    }

    @ModifyExpressionValue(method = "setupRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getX()D"), require = 0)
    private double antarchy$usePortalCameraX(double original) {
        PortalGunPortalRenderState.PortalRenderContext context = PortalGunPortalRenderState.getContext();
        return context == null ? original : context.cameraPos().x;
    }

    @ModifyExpressionValue(method = "setupRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getY()D"), require = 0)
    private double antarchy$usePortalCameraY(double original) {
        PortalGunPortalRenderState.PortalRenderContext context = PortalGunPortalRenderState.getContext();
        return context == null ? original : context.cameraPos().y;
    }

    @ModifyExpressionValue(method = "setupRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D"), require = 0)
    private double antarchy$usePortalCameraZ(double original) {
        PortalGunPortalRenderState.PortalRenderContext context = PortalGunPortalRenderState.getContext();
        return context == null ? original : context.cameraPos().z;
    }

    @Inject(method = "addRecentlyCompiledSection", at = @At("HEAD"), cancellable = true)
    private void antarchy$routePortalSectionCompile(SectionRenderDispatcher.RenderSection section, CallbackInfo ci) {
        if (PortalGunPortalViewAreaManager.routeSectionCompiled(section)) {
            ci.cancel();
        }
    }

    @Inject(method = "setSectionDirty(IIIZ)V", at = @At("TAIL"))
    private void antarchy$dirtyPortalSection(int sectionX, int sectionY, int sectionZ, boolean playerChanged, CallbackInfo ci) {
        PortalGunPortalViewAreaManager.onSectionDirty(sectionX, sectionY, sectionZ, playerChanged);
    }

    @Inject(method = "onChunkLoaded", at = @At("TAIL"))
    private void antarchy$updatePortalChunkGraph(ChunkPos pos, CallbackInfo ci) {
        PortalGunPortalViewAreaManager.onChunkLoaded(pos);
    }

}
