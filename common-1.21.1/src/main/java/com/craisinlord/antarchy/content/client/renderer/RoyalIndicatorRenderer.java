package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class RoyalIndicatorRenderer {
    private static final ResourceLocation COMMANDED_CROWN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            com.craisinlord.antarchy.Antarchy.MODID, "textures/vfx/commanded_vfx_side.png");

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
            crownTexture(poseStack, buffers, LightTexture.FULL_BRIGHT, radius, spin);
        }
        if (judged) {
            float markY = commanded ? -0.13F : 0.08F;
            poseStack.translate(0.0D, markY, 0.0D);
            pose = poseStack.last();
            brackets(lines, pose, radius * 1.18F, -spin * 1.7F);
            ring(lines, pose, radius * 0.73F, -spin * 0.7F, 1.0F, 0.91F, 0.48F, 0.85F, 24);
            judgmentFlare(lines, pose, radius * 0.82F, spin);
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

    private static void crownTexture(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
            float radius, float rotation) {
        VertexConsumer crown = buffers.getBuffer(RenderType.entityTranslucentEmissive(COMMANDED_CROWN_TEXTURE));
        float halfWidth = radius * 0.78F;
        float bottom = 0.02F;
        float top = 0.40F;

        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotation(rotation));
        for (int side = 0; side < 4; side++) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(side * 90.0F));
            PoseStack.Pose pose = poseStack.last();
            quad(crown, pose, -halfWidth, bottom, halfWidth, top, halfWidth, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void quad(VertexConsumer out, PoseStack.Pose pose, float left, float bottom,
            float right, float top, float depth, int packedLight) {
        out.addVertex(pose, left, bottom, depth).setUv(0.0F, 1.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);
        out.addVertex(pose, right, bottom, depth).setUv(1.0F, 1.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);
        out.addVertex(pose, right, top, depth).setUv(1.0F, 0.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);
        out.addVertex(pose, left, top, depth).setUv(0.0F, 0.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, 1.0F);

        // Emit the reverse winding as well. The crown is viewed from every angle, and
        // a single-sided quad can vanish when the renderer's culling state rejects it.
        out.addVertex(pose, left, top, depth).setUv(0.0F, 0.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
        out.addVertex(pose, right, top, depth).setUv(1.0F, 0.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
        out.addVertex(pose, right, bottom, depth).setUv(1.0F, 1.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
        out.addVertex(pose, left, bottom, depth).setUv(0.0F, 1.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
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

    private static void judgmentFlare(VertexConsumer out, PoseStack.Pose pose, float radius, float rotation) {
        // The old mark was almost edge-on from normal gameplay camera angles. Add a vertical,
        // unmistakable judgment sigil while keeping the rotating ring as its core.
        for (int i = 0; i < 4; i++) {
            double angle = rotation + Math.PI * 0.5D * i;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            line(out, pose, x, -0.16F, z, 0.0F, 0.22F, 0.0F, 1.0F, 0.68F, 0.08F, 1.0F);
            line(out, pose, 0.0F, 0.22F, 0.0F, -x, -0.16F, -z, 1.0F, 0.91F, 0.35F, 0.95F);
        }
    }

    private static void line(VertexConsumer out, PoseStack.Pose pose,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float r, float g, float b, float a) {
        out.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
        out.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
