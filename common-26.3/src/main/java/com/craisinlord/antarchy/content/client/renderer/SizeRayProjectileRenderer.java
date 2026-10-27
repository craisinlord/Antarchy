package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class SizeRayProjectileRenderer extends EntityRenderer<SizeRayProjectileEntity, SizeRayProjectileRenderer.SizeRayRenderState> {
    private static final Identifier BEAM_TEXTURE = Identifier.withDefaultNamespace("textures/entity/beacon/beacon_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderTypes.entityTranslucent(BEAM_TEXTURE);
    private static final float[] SHRINK_COLOR = new float[] { 1.0F, 0.92F, 0.15F };
    private static final float[] GROW_COLOR = new float[] { 0.24F, 0.62F, 1.0F };
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D);
    private static final Vec3 WORLD_FORWARD = new Vec3(0.0D, 0.0D, 1.0D);

    public SizeRayProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(SizeRayProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public SizeRayRenderState createRenderState() {
        return new SizeRayRenderState();
    }

    @Override
    public void extractRenderState(SizeRayProjectileEntity entity, SizeRayRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.forward = getRenderDirection(entity, partialTick);
        state.chargeLevel = Math.max(1, entity.getEffectiveChargeLevel());
        state.shrink = entity.getSizeRayType() == SizeRayProjectileEntity.SizeRayType.SHRINK;
        state.speed = entity.getDeltaMovement().length();
    }

    @Override
    public void submit(SizeRayRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();

        Vec3 forward = state.forward;
        Vec3 right = createPerpendicular(forward, WORLD_UP);
        Vec3 crossed = forward.cross(right);
        Vec3 up = crossed.lengthSqr() < 1.0E-6D ? WORLD_UP : crossed.normalize();

        float chargeScale = 1.0F + 0.35F * (state.chargeLevel - 1);
        float pulse = 0.84F + 0.16F * Mth.sin(state.ageInTicks * 0.35F);
        float alpha = 0.95F * pulse;
        float[] color = state.shrink ? SHRINK_COLOR : GROW_COLOR;

        float length = (float) Mth.clamp(state.speed * 2.8D + 1.2D, 1.2D, 3.2D);
        float width = 0.15F * chargeScale;
        float outerWidth = 0.28F * chargeScale;
        int packedLight = state.lightCoords;

        submitNodeCollector.submitCustomGeometry(poseStack, BEAM_RENDER_TYPE, (pose, vertexConsumer) -> {
            renderBeamRibbon(pose, vertexConsumer, packedLight, color[0], color[1], color[2], alpha, width, forward, right, length);
            renderBeamRibbon(pose, vertexConsumer, packedLight, color[0], color[1], color[2], alpha * 0.55F, outerWidth, forward, up, length);
        });

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static Vec3 getRenderDirection(SizeRayProjectileEntity entity, float partialTick) {
        Vec3 delta = entity.getDeltaMovement();
        if (delta.lengthSqr() > 1.0E-6D) {
            return delta.normalize();
        }

        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()) * ((float) Math.PI / 180.0F);
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) * ((float) Math.PI / 180.0F);
        float cosPitch = Mth.cos(pitch);
        Vec3 direction = new Vec3(
                -Mth.sin(yaw) * cosPitch,
                -Mth.sin(pitch),
                Mth.cos(yaw) * cosPitch
        );
        return direction.lengthSqr() > 1.0E-6D ? direction.normalize() : WORLD_FORWARD;
    }

    private static Vec3 createPerpendicular(Vec3 forward, Vec3 preferredAxis) {
        Vec3 perpendicular = forward.cross(preferredAxis);
        if (perpendicular.lengthSqr() < 1.0E-6D) {
            perpendicular = forward.cross(WORLD_RIGHT);
        }
        if (perpendicular.lengthSqr() < 1.0E-6D) {
            perpendicular = WORLD_UP;
        }
        return perpendicular.normalize();
    }

    private static void renderBeamRibbon(
            PoseStack.Pose pose,
            VertexConsumer vertexConsumer,
            int packedLight,
            float red,
            float green,
            float blue,
            float alpha,
            float halfWidth,
            Vec3 forward,
            Vec3 sideways,
            float length
    ) {
        Matrix4f matrix4f = pose.pose();
        Vec3 offset = sideways.scale(halfWidth);
        Vec3 end = forward.scale(length);
        Vec3 normal = sideways.cross(forward);
        if (normal.lengthSqr() < 1.0E-6D) {
            normal = WORLD_UP;
        } else {
            normal = normal.normalize();
        }

        emitVertex(vertexConsumer, pose, matrix4f, offset.scale(-1.0D), red, green, blue, alpha, 0.0F, 0.0F, normal, packedLight);
        emitVertex(vertexConsumer, pose, matrix4f, offset, red, green, blue, alpha, 1.0F, 0.0F, normal, packedLight);
        emitVertex(vertexConsumer, pose, matrix4f, end.add(offset), red, green, blue, alpha, 1.0F, 1.0F, normal, packedLight);
        emitVertex(vertexConsumer, pose, matrix4f, end.add(offset.scale(-1.0D)), red, green, blue, alpha, 0.0F, 1.0F, normal, packedLight);
    }

    private static void emitVertex(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            Matrix4f matrix4f,
            Vec3 position,
            float red,
            float green,
            float blue,
            float alpha,
            float u,
            float v,
            Vec3 normal,
            int packedLight
    ) {
        vertexConsumer
                .addVertex(matrix4f, (float) position.x, (float) position.y, (float) position.z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    public static class SizeRayRenderState extends EntityRenderState {
        public Vec3 forward = WORLD_FORWARD;
        public int chargeLevel = 1;
        public boolean shrink;
        public double speed;
    }
}
