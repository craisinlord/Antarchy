package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.vortex.WindVortexEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class WindVortexRenderer extends EntityRenderer<WindVortexEntity, WindVortexRenderer.WindVortexRenderState> {
    private static final int RINGS = 11;
    private static final float BASE_RADIUS = 0.35F;
    private static final RenderType RENDER_TYPE = RenderTypes.lightning();

    public WindVortexRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public WindVortexRenderState createRenderState() {
        return new WindVortexRenderState();
    }

    @Override
    public void extractRenderState(WindVortexEntity entity, WindVortexRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.height = entity.getVortexHeight();
        state.topRadius = entity.getTopRadius();
        state.age = entity.tickCount + partialTick;
        state.fadeOutProgress = entity.getFadeOutProgress(partialTick);
        state.basis = entity.basis();
        state.mode = entity.getMode();
    }

    @Override
    public void submit(WindVortexRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        WindVortexEntity.Basis basis = state.basis;
        if (basis != null) {
            float height = state.height;
            float topRadius = state.topRadius;
            float age = state.age;
            float fadeIn = Mth.clamp(age / 8.0F, 0.0F, 1.0F);
            float alphaMultiplier = fadeIn * state.fadeOutProgress;
            boolean pull = state.mode == WindVortexEntity.VortexMode.LENS_PULL;
            boolean push = state.mode == WindVortexEntity.VortexMode.LENS_PUSH;
            float spinDirection = pull ? -1.0F : 1.0F;
            collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> {
                Matrix4f matrix = pose.pose();
                for (int ring = 0; ring < RINGS; ring++) {
                    float progress = (ring + 0.25F) / RINGS;
                    float axisDistance = progress * height;
                    float shapeProgress = pull ? 1.0F - progress : progress;
                    float radius = Mth.lerp(shapeProgress, BASE_RADIUS, topRadius);
                    float ringSpin = spinDirection * age * (0.13F + progress * 0.045F) + ring * 0.47F;
                    float alpha = alphaMultiplier;
                    float halfSide = radius * 0.92F;
                    float lineThickness = 0.055F + progress * 0.018F;
                    int red = push ? 70 : 64;
                    int green = push ? 220 : 255;
                    int blue = push ? 255 : 56;
                    drawSquareRing(consumer, matrix, basis, halfSide, axisDistance, lineThickness, ringSpin,
                            alpha, red, green, blue);
                }
            });
        }

        super.submit(state, poseStack, collector, camera);
    }

    private static void drawSquareRing(VertexConsumer consumer, Matrix4f pose, WindVortexEntity.Basis basis,
            float halfSide, float axisDistance, float lineThickness, float rotation, float alpha,
            int red, int green, int blue) {
        for (int side = 0; side < 4; side++) {
            float sideAngle = rotation + side * (Mth.TWO_PI / 4.0F);
            addSquareSide(consumer, pose, basis, halfSide, axisDistance, lineThickness,
                    sideAngle, alpha, red, green, blue);
        }
    }

    private static void addSquareSide(VertexConsumer consumer, Matrix4f pose, WindVortexEntity.Basis basis,
            float halfSide, float axisDistance, float lineThickness, float sideAngle,
            float alpha, int red, int green, int blue) {
        org.joml.Vector3f start = squarePoint(basis, halfSide, axisDistance, sideAngle, -halfSide);
        org.joml.Vector3f end = squarePoint(basis, halfSide, axisDistance, sideAngle, halfSide);
        org.joml.Vector3f axisOffset = new org.joml.Vector3f(
                (float) basis.axis.x, (float) basis.axis.y, (float) basis.axis.z).mul(lineThickness);
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        addQuad(consumer, pose, start, end, axisOffset, red, green, blue, a);
    }

    private static org.joml.Vector3f squarePoint(WindVortexEntity.Basis basis, float halfSide,
            float axisDistance, float sideAngle, float along) {
        float x = Mth.cos(sideAngle) * halfSide;
        float z = Mth.sin(sideAngle) * halfSide;
        float tangentX = -Mth.sin(sideAngle) * along;
        float tangentZ = Mth.cos(sideAngle) * along;
        return new org.joml.Vector3f(
                (float) (basis.axis.x * axisDistance + basis.sideA.x * (x + tangentX) + basis.sideB.x * (z + tangentZ)),
                (float) (basis.axis.y * axisDistance + basis.sideA.y * (x + tangentX) + basis.sideB.y * (z + tangentZ)),
                (float) (basis.axis.z * axisDistance + basis.sideA.z * (x + tangentX) + basis.sideB.z * (z + tangentZ)));
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f pose, org.joml.Vector3f start,
            org.joml.Vector3f end, org.joml.Vector3f offset, int red, int green, int blue, int alpha) {
        addVertex(consumer, pose, start.x - offset.x, start.y - offset.y, start.z - offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, end.x - offset.x, end.y - offset.y, end.z - offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, end.x + offset.x, end.y + offset.y, end.z + offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, start.x + offset.x, start.y + offset.y, start.z + offset.z, red, green, blue, alpha);

        addVertex(consumer, pose, start.x + offset.x, start.y + offset.y, start.z + offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, end.x + offset.x, end.y + offset.y, end.z + offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, end.x - offset.x, end.y - offset.y, end.z - offset.z, red, green, blue, alpha);
        addVertex(consumer, pose, start.x - offset.x, start.y - offset.y, start.z - offset.z, red, green, blue, alpha);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z,
            int red, int green, int blue, int alpha) {
        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
    }

    public static class WindVortexRenderState extends EntityRenderState {
        public float height;
        public float topRadius;
        public float age;
        public float fadeOutProgress;
        public WindVortexEntity.Basis basis;
        public WindVortexEntity.VortexMode mode;
    }
}
