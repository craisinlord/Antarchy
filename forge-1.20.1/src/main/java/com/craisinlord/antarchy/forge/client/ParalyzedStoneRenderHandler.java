package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import software.bernie.geckolib.event.GeoRenderEvent;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class ParalyzedStoneRenderHandler {

    private ParalyzedStoneRenderHandler() {
    }
    @SubscribeEvent
    public static void onCompileGeoEntityLayers(GeoRenderEvent.Entity.CompileRenderLayers event) {
    }
}
