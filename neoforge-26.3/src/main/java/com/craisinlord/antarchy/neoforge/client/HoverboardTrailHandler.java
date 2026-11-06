package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.HoverboardSoundSystem;
import com.craisinlord.antarchy.content.client.HoverboardTrailSystem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class HoverboardTrailHandler {
    private HoverboardTrailHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        HoverboardTrailSystem.tick();
        HoverboardSoundSystem.tick();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(SubmitCustomGeometryEvent event) {
        HoverboardTrailSystem.render(event.getPoseStack(), event.getSubmitNodeCollector(),
                event.getLevelRenderState().cameraRenderState.pos);
    }
}
