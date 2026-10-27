package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.JudgmentMarkAccess;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.LivingEntity;

public final class RoyalIndicatorRenderer {
    public static final DataTicket<Boolean> COMMANDED =
            DataTicket.create("antarchy_royal_indicator_commanded", Boolean.class);
    public static final DataTicket<Boolean> JUDGED =
            DataTicket.create("antarchy_royal_indicator_judged", Boolean.class);
    private static final Identifier COMMANDED_CROWN_TEXTURE = Identifier.fromNamespaceAndPath(
            com.craisinlord.antarchy.Antarchy.MODID, "textures/vfx/commanded_vfx_side.png");
    private static final RenderType CROWN_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(COMMANDED_CROWN_TEXTURE);

    private RoyalIndicatorRenderer() {
    }

    public static void extractIndicatorState(LivingEntity entity, GeoRenderState state) {
        state.addGeckolibData(COMMANDED,
                RoyalEffectHooks.commandedHolder() != null && entity.hasEffect(RoyalEffectHooks.commandedHolder()));
        state.addGeckolibData(JUDGED,
                entity instanceof JudgmentMarkAccess access && access.antarchy$isJudgmentMarked());
    }

    public static void clearIndicatorState(GeoRenderState state) {
        state.addGeckolibData(COMMANDED, false);
        state.addGeckolibData(JUDGED, false);
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, EntityRenderState state) {
        submit(poseStack, collector, state.boundingBoxWidth, state.boundingBoxHeight, state.isInvisible,
                state.getOrDefaultGeckolibData(COMMANDED, false), state.getOrDefaultGeckolibData(JUDGED, false),
                state.ageInTicks);
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, float bbWidth, float bbHeight,
            boolean invisible, boolean commanded, boolean judged, float ageInTicks) {
        if ((!commanded && !judged) || invisible) {
            return;
        }
        float radius = Math.max(0.28F, Math.min(1.15F, bbWidth * 0.58F));
        float baseY = bbHeight + 0.18F;
        float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        poseStack.pushPose();
        poseStack.translate(0.0D, baseY, 0.0D);
        float spin = ageInTicks * (judged ? 0.055F : 0.025F);

        if (commanded) {
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, lines) ->
                    ring(lines, pose, radius, spin, 0.95F, 0.63F, 0.10F, 0.95F, 36, lineWidth));
            crownTexture(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, radius, spin);
        }
        if (judged) {
            float markY = commanded ? -0.13F : 0.08F;
            poseStack.translate(0.0D, markY, 0.0D);
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, lines) -> {
                brackets(lines, pose, radius * 1.18F, -spin * 1.7F, lineWidth);
                ring(lines, pose, radius * 0.73F, -spin * 0.7F, 1.0F, 0.91F, 0.48F, 0.85F, 24, lineWidth);
                judgmentFlare(lines, pose, radius * 0.82F, spin, lineWidth);
            });
        }
        poseStack.popPose();
    }

    private static void ring(VertexConsumer out, PoseStack.Pose pose, float radius, float rotation,
            float r, float g, float b, float a, int segments, float lineWidth) {
        for (int i = 0; i < segments; i++) {
            double angleA = (Math.PI * 2.0D * i) / segments + rotation;
            double angleB = (Math.PI * 2.0D * (i + 1)) / segments + rotation;
            line(out, pose, (float) Math.cos(angleA) * radius, 0.0F, (float) Math.sin(angleA) * radius,
                    (float) Math.cos(angleB) * radius, 0.0F, (float) Math.sin(angleB) * radius, r, g, b, a, lineWidth);
        }
    }

    private static void crownTexture(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
            float radius, float rotation) {
        float halfWidth = radius * 0.78F;
        float bottom = 0.02F;
        float top = 0.40F;

        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotation(rotation));
        for (int side = 0; side < 4; side++) {
            poseStack.pushPose();
            poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(side * 90.0F));
            collector.submitCustomGeometry(poseStack, CROWN_RENDER_TYPE, (pose, crown) ->
                    quad(crown, pose, -halfWidth, bottom, halfWidth, top, halfWidth, packedLight));
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

        out.addVertex(pose, left, top, depth).setUv(0.0F, 0.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
        out.addVertex(pose, right, top, depth).setUv(1.0F, 0.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
        out.addVertex(pose, right, bottom, depth).setUv(1.0F, 1.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
        out.addVertex(pose, left, bottom, depth).setUv(0.0F, 1.0F).setOverlay(0).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
    }

    private static void brackets(VertexConsumer out, PoseStack.Pose pose, float radius, float rotation, float lineWidth) {
        for (int i = 0; i < 3; i++) {
            double angle = rotation + Math.PI * 2.0D * i / 3.0D;
            float cx = (float) Math.cos(angle) * radius;
            float cz = (float) Math.sin(angle) * radius;
            float tx = (float) -Math.sin(angle);
            float tz = (float) Math.cos(angle);
            line(out, pose, cx - tx * 0.13F, 0.0F, cz - tz * 0.13F,
                    cx + tx * 0.13F, 0.0F, cz + tz * 0.13F, 1.0F, 0.81F, 0.20F, 1.0F, lineWidth);
            line(out, pose, cx, 0.0F, cz, cx * 0.82F, 0.0F, cz * 0.82F, 1.0F, 0.91F, 0.52F, 0.9F, lineWidth);
        }
    }

    private static void judgmentFlare(VertexConsumer out, PoseStack.Pose pose, float radius, float rotation, float lineWidth) {
        for (int i = 0; i < 4; i++) {
            double angle = rotation + Math.PI * 0.5D * i;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            line(out, pose, x, -0.16F, z, 0.0F, 0.22F, 0.0F, 1.0F, 0.68F, 0.08F, 1.0F, lineWidth);
            line(out, pose, 0.0F, 0.22F, 0.0F, -x, -0.16F, -z, 1.0F, 0.91F, 0.35F, 0.95F, lineWidth);
        }
    }

    private static void line(VertexConsumer out, PoseStack.Pose pose,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float r, float g, float b, float a, float lineWidth) {
        out.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F).setLineWidth(lineWidth);
        out.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, 0.0F, 1.0F, 0.0F).setLineWidth(lineWidth);
    }
}
