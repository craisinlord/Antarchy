package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class NightmareCameraShakeHandler {
    private static final double MAX_SHAKE_RANGE = 32.0D;

    private NightmareCameraShakeHandler() {
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        float shakeStrength = 0.0F;

        for (NightmareEntity nightmare : minecraft.level.getEntitiesOfClass(
                NightmareEntity.class,
                minecraft.player.getBoundingBox().inflate(MAX_SHAKE_RANGE),
                nightmare -> nightmare.isAlive() && nightmare.isRoaring()
        )) {
            double distance = Math.sqrt(cameraPos.distanceToSqr(nightmare.position().add(0.0D, nightmare.getBbHeight() * 0.5D, 0.0D)));
            if (distance > MAX_SHAKE_RANGE) {
                continue;
            }

            shakeStrength += (float) ((1.0D - distance / MAX_SHAKE_RANGE) * 1.35D);
        }

        if (shakeStrength <= 0.0F) {
            return;
        }

        float time = (float) (minecraft.player.tickCount + event.getPartialTick());
        event.setYaw(event.getYaw() + Mth.sin(time * 1.85F) * shakeStrength * 1.8F);
        event.setPitch(event.getPitch() + Mth.cos(time * 2.15F) * shakeStrength * 1.45F);
        event.setRoll(event.getRoll() + Mth.sin(time * 2.65F) * shakeStrength * 1.05F);
    }
}
