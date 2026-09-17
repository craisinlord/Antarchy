package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

public final class RoyalIndicatorRenderer {
    private RoyalIndicatorRenderer() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffers, LivingEntity entity,
            boolean commanded, boolean judged, float ageInTicks) {
        if ((!commanded && !judged) || entity.isInvisible()) {
            return;
        }
        float radius = Math.max(0.28F, Math.min(1.15F, entity.getBbWidth() * 0.58F));
        float baseY = entity.getBbHeight() + 0.18F;
        poseStack.pushPose();
        poseStack.translate(0.0D, baseY, 0.0D);
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        float spin = ageInTicks * (judged ? 0.055F : 0.025F);

        if (commanded) {
            ring(lines, pose, radius, spin, 0.95F, 0.63F, 0.10F, 0.95F, 36);
            crownPoints(lines, pose, radius, spin, 1.0F, 0.80F, 0.22F, 0.95F);
        }
        if (judged) {
            float markY = commanded ? -0.13F : 0.0F;
            poseStack.translate(0.0D, markY, 0.0D);
            pose = poseStack.last();
            brackets(lines, pose, radius * 1.18F, -spin * 1.7F);
            ring(lines, pose, radius * 0.73F, -spin * 0.7F, 1.0F, 0.91F, 0.48F, 0.85F, 24);
        }
        poseStack.popPose();
    }

    private static void ring(VertexConsumer out, PoseStack.Pose pose, float radius, float rotation,
            float r, float g, float b, float a, int segments) {
        for (int i = 0; i < segments; i++) {
            double angleA = (Math.PI * 2.0D * i) / segments + rotation;
            double angleB = (Math.PI * 2.0D * (i + 1)) / segments + rotation;
            line(out, pose, (float) Math.cos(angleA) * radius, 0.0F, (float) Math.sin(angleA) * radius,
                    (float) Math.cos(angleB) * radius, 0.0F, (float) Math.sin(angleB) * radius, r, g, b, a);
        }
    }

    private static void crownPoints(VertexConsumer out, PoseStack.Pose pose, float radius, float rotation,
            float r, float g, float b, float a) {
        for (int i = 0; i < 3; i++) {
            double angle = rotation + Math.PI * 2.0D * i / 3.0D;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            line(out, pose, x, 0.0F, z, x * 0.68F, 0.19F, z * 0.68F, r, g, b, a);
        }
    }

    private static void brackets(VertexConsumer out, PoseStack.Pose pose, float radius, float rotation) {
        for (int i = 0; i < 3; i++) {
            double angle = rotation + Math.PI * 2.0D * i / 3.0D;
            float cx = (float) Math.cos(angle) * radius;
            float cz = (float) Math.sin(angle) * radius;
            float tx = (float) -Math.sin(angle);
            float tz = (float) Math.cos(angle);
            line(out, pose, cx - tx * 0.13F, 0.0F, cz - tz * 0.13F,
                    cx + tx * 0.13F, 0.0F, cz + tz * 0.13F, 1.0F, 0.81F, 0.20F, 1.0F);
            line(out, pose, cx, 0.0F, cz, cx * 0.82F, 0.0F, cz * 0.82F, 1.0F, 0.91F, 0.52F, 0.9F);
        }
    }

    private static void line(VertexConsumer out, PoseStack.Pose pose,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float r, float g, float b, float a) {
        out.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
        out.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
