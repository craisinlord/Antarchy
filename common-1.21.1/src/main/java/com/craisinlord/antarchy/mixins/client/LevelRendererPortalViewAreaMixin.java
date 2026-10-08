package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalSectionViews;
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

    @Inject(method = "addRecentlyCompiledSection", at = @At("TAIL"))
    private void antarchy$notifyPortalGraphs(SectionRenderDispatcher.RenderSection section, CallbackInfo ci) {
        PortalGunPortalSectionViews.onSectionCompiled(section);
    }

    @Inject(method = "onChunkLoaded", at = @At("TAIL"))
    private void antarchy$updatePortalChunkGraph(ChunkPos pos, CallbackInfo ci) {
        PortalGunPortalSectionViews.onChunkLoaded(pos);
    }
}
