package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.WaterBombEntity;
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

public class WaterBombRenderer extends EntityRenderer<WaterBombEntity> {

    private static final ResourceLocation WATER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/water_still.png");

    public WaterBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(WaterBombEntity entity, BlockPos pos) {
        return 12;
    }

    @Override
    public void render(WaterBombEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.5F, 0.5F, 0.5F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(WATER_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        addVertex(vertexConsumer, pose, matrix4f, -0.5F, -0.5F, 0.0F, 1.0F, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, -0.5F, 1.0F, 1.0F, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, 0.5F, 1.0F, 0.0F, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, -0.5F, 0.5F, 0.0F, 0.0F, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void addVertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, Matrix4f matrix4f,
                                   float x, float y, float u, float v, int packedLight) {
        vertexConsumer.addVertex(matrix4f, x, y, 0.0F)
                .setColor(0.05F, 0.15F, 0.6F, 0.9F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterBombEntity entity) {
        return WATER_TEXTURE;
    }
}
