package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class GravityCameraRollHandler {
    private GravityCameraRollHandler() {
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Entity entity = event.getCamera().getEntity();
        if (entity == null) {
            return;
        }

        float flipProgress = AntarchyGravityApi.getGravityFlipProgress(entity, (float) event.getPartialTick());
        if (flipProgress <= 0.0F) {
            return;
        }

        event.setRoll(event.getRoll() + 180.0F * flipProgress);
    }
}
