package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import software.bernie.geckolib.event.GeoRenderEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class ParalyzedStoneRenderHandler {

    private ParalyzedStoneRenderHandler() {
    }
    @SubscribeEvent
    public static void onCompileGeoEntityLayers(GeoRenderEvent.Entity.CompileRenderLayers event) {
    }
}
