package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class HushProjectileRenderer extends EntityRenderer<HushProjectileEntity> {
    private static final ResourceLocation ORB_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");

    public HushProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(HushProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(HushProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.55F, 0.55F, 0.55F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(ORB_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        addVertex(vertexConsumer, pose, matrix4f, -0.5F, -0.5F, 0.0F, 1.0F, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, -0.5F, 1.0F, 1.0F, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, 0.5F, 1.0F, 0.0F, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, -0.5F, 0.5F, 0.0F, 0.0F, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void addVertex(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            Matrix4f matrix4f,
            float x,
            float y,
            float u,
            float v,
            int packedLight
    ) {
        vertexConsumer.addVertex(matrix4f, x, y, 0.0F)
                .setColor(1.0F, 0.08F, 0.08F, 0.95F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(HushProjectileEntity entity) {
        return ORB_TEXTURE;
    }
}
