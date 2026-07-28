package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class LucidBoltRenderer extends EntityRenderer<LucidBoltEntity> {
    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static final Vec3 WORLD_RIGHT = new Vec3(1.0D, 0.0D, 0.0D);
    private static final Vec3 WORLD_FORWARD = new Vec3(0.0D, 0.0D, 1.0D);

    public LucidBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(LucidBoltEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(LucidBoltEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        Vec3 forward = getRenderDirection(entity, partialTick);
        Vec3 right = createPerpendicular(forward, WORLD_UP);
        Vec3 up = forward.cross(right);
        if (up.lengthSqr() < 1.0E-6D) {
            up = WORLD_UP;
        } else {
            up = up.normalize();
        }

        float pulse = 0.85F + 0.15F * Mth.sin((entity.tickCount + partialTick) * 0.55F);
        float alpha = 0.92F * pulse;
        Vec3 delta = entity.getDeltaMovement();
        float length = (float) Mth.clamp(delta.length() * 2.3D + 0.95D, 0.9D, 2.5D);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(BEAM_TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        renderBeamRibbon(pose, vertexConsumer, packedLight, 1.0F, 0.18F, 0.08F, alpha, 0.13F, forward, right, length);
        renderBeamRibbon(pose, vertexConsumer, packedLight, 1.0F, 0.48F, 0.18F, alpha * 0.58F, 0.24F, forward, up, length);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static Vec3 getRenderDirection(LucidBoltEntity entity, float partialTick) {
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

    @Override
    public ResourceLocation getTextureLocation(LucidBoltEntity entity) {
        return BEAM_TEXTURE;
    }
}
