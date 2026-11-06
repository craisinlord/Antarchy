package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.royal.RoyalBoltEntity;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RoyalBoltRenderer extends EntityRenderer<RoyalBoltEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/particle/generic_0.png");
    private static final float GLIMMER_SPRITE_SIZE = 0.7F;
    private static final float GLIMMER_SPRITE_ALPHA = 0.85F;

    public RoyalBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RoyalBoltEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (GlimmerEchoes.isGlimmerEcho(entity)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(GLIMMER_SPRITE_SIZE, GLIMMER_SPRITE_SIZE, GLIMMER_SPRITE_SIZE);
            PoseStack.Pose pose = poseStack.last();
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
            glimmerVertex(consumer, pose, -0.5F, -0.5F, 0.0F, 1.0F);
            glimmerVertex(consumer, pose, 0.5F, -0.5F, 1.0F, 1.0F);
            glimmerVertex(consumer, pose, 0.5F, 0.5F, 1.0F, 0.0F);
            glimmerVertex(consumer, pose, -0.5F, 0.5F, 0.0F, 0.0F);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void glimmerVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(GlimmerEchoRendering.GLIMMER_RED, GlimmerEchoRendering.GLIMMER_GREEN,
                        GlimmerEchoRendering.GLIMMER_BLUE, GLIMMER_SPRITE_ALPHA)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(RoyalBoltEntity entity) {
        return TEXTURE;
    }
}
