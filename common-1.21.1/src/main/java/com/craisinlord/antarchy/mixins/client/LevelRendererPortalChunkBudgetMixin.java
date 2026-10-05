package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalRendererPool;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererPortalChunkBudgetMixin {
    @Redirect(
            method = "compileSections",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;uploadAllPendingUploads()V")
    )
    private void antarchy$budgetPortalUploads(SectionRenderDispatcher dispatcher) {
        PortalGunPortalRendererPool.uploadPending((LevelRenderer) (Object) this, dispatcher);
    }

    @Redirect(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;pollLightUpdates()V")
    )
    private void antarchy$skipProxyLightQueuePoll(ClientLevel level) {
        if (!PortalGunPortalRendererPool.isProxy((LevelRenderer) (Object) this)) {
            level.pollLightUpdates();
        }
    }

    @Redirect(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I")
    )
    private int antarchy$skipProxyLightQueueDrain(LevelLightEngine lightEngine) {
        return PortalGunPortalRendererPool.isProxy((LevelRenderer) (Object) this) ? 0 : lightEngine.runLightUpdates();
    }

    @Redirect(
            method = "compileSections",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;rebuildSectionSync(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;Lnet/minecraft/client/renderer/chunk/RenderRegionCache;)V"
            )
    )
    private void antarchy$queuePortalSyncRebuilds(SectionRenderDispatcher dispatcher, SectionRenderDispatcher.RenderSection section, RenderRegionCache cache) {
        PortalGunPortalRendererPool.rebuildSection((LevelRenderer) (Object) this, dispatcher, section, cache);
    }

    @Redirect(
            method = "compileSections",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;rebuildSectionAsync(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher;Lnet/minecraft/client/renderer/chunk/RenderRegionCache;)V"
            )
    )
    private void antarchy$budgetPortalAsyncRebuilds(SectionRenderDispatcher.RenderSection section, SectionRenderDispatcher dispatcher, RenderRegionCache cache) {
        PortalGunPortalRendererPool.rebuildSectionAsync((LevelRenderer) (Object) this, dispatcher, section, cache);
    }

    @Redirect(
            method = "compileSections",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;setNotDirty()V"
            )
    )
    private void antarchy$keepUnscheduledPortalSectionsDirty(SectionRenderDispatcher.RenderSection section) {
        PortalGunPortalRendererPool.setNotDirty(section);
    }
}
