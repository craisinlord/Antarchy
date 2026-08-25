package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalViewRenderer;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class PortalGunPortalViewRenderHandler {
    private static long lastStageLogNanos;

    private PortalGunPortalViewRenderHandler() {
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!PortalGunPortalViewRenderer.isEnabled()) {
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastStageLogNanos > 5_000_000_000L) {
            lastStageLogNanos = now;
            Antarchy.LOGGER.info("Portal gun NeoForge render stage fired stage={} partial={}", event.getStage(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
        PortalGunPortalViewRenderer.render(event.getCamera(), event.getPoseStack().last().pose(), event.getPartialTick());
    }
}
