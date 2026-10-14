package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonMath;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class MoonSkyTracker {
    private static final long MAX_AGE_MILLIS = 1500L;
    private static final double AIM_COSINE = Math.cos(Math.toRadians(PortalGunMoonMath.AIM_TOLERANCE_DEGREES));
    private static Vec3 direction;
    private static ClientLevel level;
    private static long recordedAt;
    private static Consumer<PortalGunMoonAimPayload> sender = payload -> {};

    private MoonSkyTracker() {
    }

    public static void setSender(Consumer<PortalGunMoonAimPayload> payloadSender) {
        sender = payloadSender;
    }

    public static void recordSphericalMoon(ClientLevel renderedLevel, Camera camera, Matrix4f skyMatrix, double theta, double phi) {
        Vector3d local = new Quaterniond().rotateY(theta).mul(new Quaterniond().rotateX(phi)).transform(new Vector3d(0.0D, 1.0D, 0.0D));
        Vector3f view = skyMatrix.transformDirection(new Vector3f((float) local.x, (float) local.y, (float) local.z));
        if (view.lengthSquared() < 1.0E-8F) {
            return;
        }
        Vector3f world = camera.rotation().transform(view.normalize());
        direction = new Vec3(world.x, world.y, world.z).normalize();
        level = renderedLevel;
        recordedAt = System.currentTimeMillis();
    }

    public static void report() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        boolean tracked = direction != null && level == minecraft.level && System.currentTimeMillis() - recordedAt <= MAX_AGE_MILLIS;
        boolean aiming = tracked && minecraft.gameRenderer.getMainCamera().getLookVector().dot(
                new Vector3f((float) direction.x, (float) direction.y, (float) direction.z)) >= AIM_COSINE;
        sender.accept(new PortalGunMoonAimPayload(tracked, aiming));
    }
}
