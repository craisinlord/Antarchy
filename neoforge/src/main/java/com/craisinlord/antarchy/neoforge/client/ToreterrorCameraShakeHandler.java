package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class ToreterrorCameraShakeHandler {
    private static final double MAX_SHAKE_RANGE = 48.0D;
    private static final int TORETERROR_SHAKE_TICKS = 25;

    private ToreterrorCameraShakeHandler() {
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 cameraPos = event.getCamera().getPosition();
        float shakeStrength = 0.0F;

        for (ToreterrorEntity toreterror : mc.level.getEntitiesOfClass(
                ToreterrorEntity.class,
                mc.player.getBoundingBox().inflate(MAX_SHAKE_RANGE),
                t -> t.isAlive() && t.isJumpShaking()
        )) {
            double distance = Math.sqrt(cameraPos.distanceToSqr(toreterror.position().add(0, toreterror.getBbHeight() * 0.5, 0)));
            if (distance > MAX_SHAKE_RANGE) continue;
            shakeStrength += (float) ((1.0D - distance / MAX_SHAKE_RANGE) * 2.0D);
        }

        if (BigBerthaItem.clientShakeTicks > 0) {
            shakeStrength += (float) BigBerthaItem.clientShakeTicks / TORETERROR_SHAKE_TICKS * 2.0F;
        }

        if (shakeStrength <= 0.0F) return;

        float time = (float) (mc.player.tickCount + event.getPartialTick());
        event.setYaw(event.getYaw() + Mth.sin(time * 1.5F) * shakeStrength * 2.5F);
        event.setPitch(event.getPitch() + Mth.cos(time * 1.8F) * shakeStrength * 2.0F);
        event.setRoll(event.getRoll() + Mth.sin(time * 2.2F) * shakeStrength * 1.5F);
    }
}
