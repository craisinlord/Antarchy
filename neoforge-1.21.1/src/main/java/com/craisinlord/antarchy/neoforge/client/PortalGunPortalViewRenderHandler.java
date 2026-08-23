package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalViewRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class PortalGunPortalViewRenderHandler {
    private PortalGunPortalViewRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        PortalGunPortalViewRenderer.render(event.getCamera(), event.getPoseStack().last().pose(), event.getPartialTick());
    }
}
