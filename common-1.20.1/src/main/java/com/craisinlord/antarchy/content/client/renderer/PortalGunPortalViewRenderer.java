package com.craisinlord.antarchy.content.client.renderer;

import net.minecraft.client.Camera;
import org.joml.Matrix4f;

/** 1.20 hook for the optional recursive portal framebuffer path. */
public final class PortalGunPortalViewRenderer {
    private PortalGunPortalViewRenderer() {
    }

    public static boolean isEnabled() {
        return false;
    }

    public static void render(Camera camera, Matrix4f poseMatrix, float tickCounter) {
        // Disabled on 1.20 until its LevelRenderer camera contract is ported.
    }
}
