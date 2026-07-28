package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyOrbEntity;
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

public class BrutalflyOrbRenderer extends EntityRenderer<BrutalflyOrbEntity> {
    private static final ResourceLocation ORB_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");

    public BrutalflyOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(BrutalflyOrbEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(BrutalflyOrbEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        float scale = entity.isLingeringOrb() ? entity.getOrbRadius(partialTick) * 0.38F : 0.8F;
        poseStack.scale(scale, scale, scale);

        float red;
        float green;
        float blue;
        if (entity.getOrbVariant() == BrutalflyOrbEntity.OrbVariant.POISON) {
            red = 0.32F;
            green = 0.92F;
            blue = 0.26F;
        } else {
            red = 1.0F;
            green = 0.62F;
            blue = 0.08F;
        }

        float alpha = entity.isLingeringOrb() ? 0.72F : 0.95F;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(ORB_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();

        addVertex(vertexConsumer, pose, matrix4f, -0.5F, -0.5F, 0.0F, 1.0F, red, green, blue, alpha, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, -0.5F, 1.0F, 1.0F, red, green, blue, alpha, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, 0.5F, 1.0F, 0.0F, red, green, blue, alpha, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, -0.5F, 0.5F, 0.0F, 0.0F, red, green, blue, alpha, packedLight);

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
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight
    ) {
        vertexConsumer.addVertex(matrix4f, x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(BrutalflyOrbEntity entity) {
        return ORB_TEXTURE;
    }
}
