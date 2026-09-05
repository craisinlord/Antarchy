package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.vortex.WindVortexEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class WindVortexRenderer extends EntityRenderer<WindVortexEntity> {
    private static final int RINGS = 10;
    private static final int SEGMENTS_PER_RING = 12;
    private static final int ARC_STEPS = 6;
    private static final float BASE_RADIUS = 0.35F;

    public WindVortexRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(WindVortexEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();
        float height = entity.getVortexHeight();
        float topRadius = entity.getTopRadius();
        float age = entity.tickCount + partialTick;
        float fadeIn = Mth.clamp(age / 8.0F, 0.0F, 1.0F);
        float alphaMultiplier = fadeIn * entity.getFadeOutProgress(partialTick);
        WindVortexEntity.Basis basis = entity.basis();
        boolean pull = entity.getMode() == WindVortexEntity.VortexMode.LENS_PULL;
        boolean push = entity.getMode() == WindVortexEntity.VortexMode.LENS_PUSH;
        float spinDirection = pull ? -1.0F : 1.0F;

        for (int ring = 0; ring < RINGS; ring++) {
            float progress = (ring + 0.25F) / RINGS;
            float axisDistance = progress * height;
            float shapeProgress = pull ? 1.0F - progress : progress;
            float radius = Mth.lerp(shapeProgress, BASE_RADIUS, topRadius);
            float ringSpin = spinDirection * age * (0.13F + progress * 0.045F) + ring * 0.47F;
            float alpha = alphaMultiplier;
            float thickness = 0.025F + radius * 0.012F;
            float arcLength = 0.34F + progress * 0.08F;
            int red = push ? 70 : 64;
            int green = push ? 220 : 255;
            int blue = push ? 255 : 56;

            for (int segment = 0; segment < SEGMENTS_PER_RING; segment++) {
                float baseAngle = ringSpin + segment * Mth.TWO_PI / SEGMENTS_PER_RING;
                float start = baseAngle - arcLength * 0.5F;
                float end = baseAngle + arcLength * 0.5F;
                drawArc(consumer, pose, basis, radius, axisDistance, thickness, start, end, alpha, red, green, blue);
            }
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void drawArc(VertexConsumer consumer, Matrix4f pose, WindVortexEntity.Basis basis, float radius,
            float axisDistance, float thickness, float start, float end, float alpha, int red, int green, int blue) {
        float previousAngle = start;
        for (int step = 1; step <= ARC_STEPS; step++) {
            float angle = Mth.lerp(step / (float) ARC_STEPS, start, end);
            addLineQuad(consumer, pose, basis, radius, axisDistance, thickness, previousAngle, angle, alpha, red, green, blue);
            previousAngle = angle;
        }
    }

    private static void addLineQuad(VertexConsumer consumer, Matrix4f pose, WindVortexEntity.Basis basis, float radius,
            float axisDistance, float thickness, float startAngle, float endAngle, float alpha, int red, int green, int blue) {
        org.joml.Vector3f start = point(basis, radius, axisDistance, startAngle);
        org.joml.Vector3f end = point(basis, radius, axisDistance, endAngle);
        org.joml.Vector3f axisOffset = new org.joml.Vector3f((float) basis.axis.x, (float) basis.axis.y, (float) basis.axis.z).mul(thickness);
        float middleAngle = (startAngle + endAngle) * 0.5F;
        org.joml.Vector3f radialOffset = new org.joml.Vector3f(
                (float) (basis.sideA.x * Mth.cos(middleAngle) + basis.sideB.x * Mth.sin(middleAngle)),
                (float) (basis.sideA.y * Mth.cos(middleAngle) + basis.sideB.y * Mth.sin(middleAngle)),
                (float) (basis.sideA.z * Mth.cos(middleAngle) + basis.sideB.z * Mth.sin(middleAngle))
        ).mul(thickness);
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);

        addQuad(consumer, pose, start, end, axisOffset, red, green, blue, a);
        addQuad(consumer, pose, start, end, radialOffset, red, green, blue, a);
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f pose, org.joml.Vector3f start,
            org.joml.Vector3f end, org.joml.Vector3f offset, int red, int green, int blue, int alpha) {
        addVertex(consumer, pose, start.x - offset.x, start.y - offset.y, start.z - offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, end.x - offset.x, end.y - offset.y, end.z - offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, end.x + offset.x, end.y + offset.y, end.z + offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, start.x + offset.x, start.y + offset.y, start.z + offset.z, red, green, blue, alpha);
    }

    private static org.joml.Vector3f point(WindVortexEntity.Basis basis, float radius, float axisDistance, float angle) {
        double radialA = Mth.cos(angle) * radius;
        double radialB = Mth.sin(angle) * radius;
        double x = basis.axis.x * axisDistance + basis.sideA.x * radialA + basis.sideB.x * radialB;
        double y = basis.axis.y * axisDistance + basis.sideA.y * radialA + basis.sideB.y * radialB;
        double z = basis.axis.z * axisDistance + basis.sideA.z * radialA + basis.sideB.z * radialB;
        return new org.joml.Vector3f((float) x, (float) y, (float) z);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z,
            int red, int green, int blue, int alpha) {
        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
    }

    @Override
    public ResourceLocation getTextureLocation(WindVortexEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/unknown_pack.png");
    }
}
