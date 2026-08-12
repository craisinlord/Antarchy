package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.SpitBugProjectileEntity;
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

public class SpitBugProjectileRenderer extends EntityRenderer<SpitBugProjectileEntity> {
    private static final ResourceLocation PROJECTILE_TEXTURE =
            new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

    public SpitBugProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(SpitBugProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(SpitBugProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        float scale = entity.getRenderScale(partialTick);
        poseStack.scale(scale, scale, scale);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(PROJECTILE_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        float alpha = entity.isLingering() ? 0.78F : 0.95F;

        addVertex(vertexConsumer, pose, matrix4f, -0.5F, -0.5F, 0.0F, 1.0F, alpha, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, -0.5F, 1.0F, 1.0F, alpha, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, 0.5F, 0.5F, 1.0F, 0.0F, alpha, packedLight);
        addVertex(vertexConsumer, pose, matrix4f, -0.5F, 0.5F, 0.0F, 0.0F, alpha, packedLight);

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
            float alpha,
            int packedLight
    ) {
        vertexConsumer.vertex(matrix4f, x, y, 0.0F)
                .color(0.55F, 1.0F, 0.35F, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SpitBugProjectileEntity entity) {
        return PROJECTILE_TEXTURE;
    }
}
