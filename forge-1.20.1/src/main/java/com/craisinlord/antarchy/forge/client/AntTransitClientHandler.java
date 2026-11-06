package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.craisinlord.antarchy.content.client.renderer.AntTransitGroundRenderer;
import com.craisinlord.antarchy.content.client.renderer.AntTransitOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AntTransitClientHandler {
    private AntTransitClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        AntTransitClientState.tick();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        AntTransitGroundRenderer.render(event.getPoseStack(), minecraft.renderBuffers().bufferSource(), event.getCamera(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        AntTransitOverlay.render(event.getGuiGraphics(), event.getPartialTick());
    }
}
