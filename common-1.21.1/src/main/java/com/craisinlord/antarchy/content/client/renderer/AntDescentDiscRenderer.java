package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.AntDescentClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class AntDescentDiscRenderer {
    private static final int SEGMENTS = 48;
    private static final float RADIUS = 2.5F;
    private static final float CORE = 0.78F;
    private static final float GROW_TICKS = 20.0F;
    private static final float CLOSE_TICKS = 15.0F;
    private static final float[] VOID_CORE = {0.0F, 0.0F, 0.0F, 0.96F};
    private static final float[] VOID_RIM = {0.42F, 0.1F, 0.68F, 0.9F};
    private static final float[] LIGHT_CORE = {1.0F, 0.96F, 0.84F, 0.8F};
    private static final float[] LIGHT_RIM = {1.0F, 0.84F, 0.48F, 0.85F};

    private AntDescentDiscRenderer() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Camera camera, float partialTick) {
        if (AntDescentClientState.discs().isEmpty()) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        RenderType renderType = RenderType.debugQuads();
        VertexConsumer consumer = buffers.getBuffer(renderType);
        Matrix4f pose = poseStack.last().pose();
        for (AntDescentClientState.Disc disc : AntDescentClientState.discs()) {
            float age = disc.age() + partialTick;
            float grow = Mth.clamp(age / GROW_TICKS, 0.0F, 1.0F);
            float close = Mth.clamp((AntDescentClientState.DISC_LIFETIME_TICKS - age) / CLOSE_TICKS, 0.0F, 1.0F);
            float radius = RADIUS * (1.0F - (1.0F - grow) * (1.0F - grow)) * close;
            if (radius <= 0.01F) {
                continue;
            }
            float x = (float) (disc.center().x - cameraPos.x);
            float y = (float) (disc.center().y - cameraPos.y) + 0.02F;
            float z = (float) (disc.center().z - cameraPos.z);
            drawDisc(consumer, pose, x, y, z, radius, age / 20.0F, disc.home());
        }
        buffers.endBatch(renderType);
    }

    private static void drawDisc(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, float radius, float time, boolean home) {
        float[] core = home ? LIGHT_CORE : VOID_CORE;
        float[] rim = home ? LIGHT_RIM : VOID_RIM;
        float spin = home ? -time : time;
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (i * Math.PI * 2.0D / SEGMENTS);
            float a1 = (float) ((i + 1) * Math.PI * 2.0D / SEGMENTS);
            float inner = radius * CORE;
            float outer0 = radius * (1.0F + 0.08F * Mth.sin(a0 * 5.0F + spin * 2.0F));
            float outer1 = radius * (1.0F + 0.08F * Mth.sin(a1 * 5.0F + spin * 2.0F));
            float glow0 = 0.7F + 0.3F * Mth.sin(a0 * 3.0F - spin * 3.0F);
            float glow1 = 0.7F + 0.3F * Mth.sin(a1 * 3.0F - spin * 3.0F);
            float c0 = Mth.cos(a0);
            float s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1);
            float s1 = Mth.sin(a1);

            vertex(consumer, pose, x, y, z, core, 1.0F, 1.0F);
            vertex(consumer, pose, x, y, z, core, 1.0F, 1.0F);
            vertex(consumer, pose, x + c1 * inner, y, z + s1 * inner, core, 1.0F, 1.0F);
            vertex(consumer, pose, x + c0 * inner, y, z + s0 * inner, core, 1.0F, 1.0F);

            vertex(consumer, pose, x + c0 * inner, y, z + s0 * inner, rim, glow0, 1.0F);
            vertex(consumer, pose, x + c1 * inner, y, z + s1 * inner, rim, glow1, 1.0F);
            vertex(consumer, pose, x + c1 * outer1, y, z + s1 * outer1, rim, glow1, 0.0F);
            vertex(consumer, pose, x + c0 * outer0, y, z + s0 * outer0, rim, glow0, 0.0F);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, float[] color, float glow, float alpha) {
        consumer.addVertex(pose, x, y, z).setColor(
                Math.min(1.0F, color[0] * glow), Math.min(1.0F, color[1] * glow), Math.min(1.0F, color[2] * glow), color[3] * alpha);
    }
}
