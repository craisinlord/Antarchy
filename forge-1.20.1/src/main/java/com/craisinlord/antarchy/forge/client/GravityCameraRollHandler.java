package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class GravityCameraRollHandler {
    private GravityCameraRollHandler() {
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();
        Entity entity = camera.getEntity();
        if (entity == null) {
            return;
        }

        float flipProgress = AntarchyGravityApi.getGravityFlipProgress(entity, (float) event.getPartialTick());
        if (flipProgress <= 0.0F) {
            return;
        }

        float gravityRoll = 180.0F * flipProgress;
        event.setRoll(event.getRoll() + gravityRoll);

        Vector3f look = camera.getLookVector();
        float radians = (float) Math.toRadians(gravityRoll);
        camera.getUpVector().rotateAxis(radians, look.x(), look.y(), look.z());
        camera.getLeftVector().rotateAxis(radians, look.x(), look.y(), look.z());
    }
}
