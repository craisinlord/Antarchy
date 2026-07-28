package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ViewportEvent;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class CameraShakeHandler {
    private static final int TORETERROR_SHAKE_TICKS = 25;

    private CameraShakeHandler() {
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            CameraShakeClientState.clear();
            return;
        }

        float shakeStrength = CameraShakeClientState.getStrength(event.getCamera().getPosition());

        int beetleShakeTicks = HerculesBeetleImpactShakeClientState.getTicks();
        if (beetleShakeTicks > 0) {
            shakeStrength += (float) beetleShakeTicks / TORETERROR_SHAKE_TICKS * 2.0F;
        }

        int bigBerthaShakeTicks = BigBerthaItem.clientShakeTicks;
        if (bigBerthaShakeTicks > 0) {
            shakeStrength += (float) bigBerthaShakeTicks / TORETERROR_SHAKE_TICKS * 2.0F;
        }

        if (shakeStrength <= 0.0F) return;

        float time = (float) (mc.player.tickCount + event.getPartialTick());
        event.setYaw(event.getYaw() + Mth.sin(time * 1.5F) * shakeStrength * 2.5F);
        event.setPitch(event.getPitch() + Mth.cos(time * 1.8F) * shakeStrength * 2.0F);
        event.setRoll(event.getRoll() + Mth.sin(time * 2.2F) * shakeStrength * 1.5F);
    }
}
