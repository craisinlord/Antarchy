package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.royal.RoyalBoltEntity;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class RoyalBoltRenderer extends EntityRenderer<RoyalBoltEntity, RoyalBoltRenderer.BoltRenderState> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/particle/generic_0.png");
    private static final float GLIMMER_SPRITE_SIZE = 0.7F;
    private static final float GLIMMER_SPRITE_ALPHA = 0.85F;

    public RoyalBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BoltRenderState createRenderState() {
        return new BoltRenderState();
    }

    @Override
    public void extractRenderState(RoyalBoltEntity entity, BoltRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.glimmer = GlimmerEchoes.isGlimmerEcho(entity);
        state.halfHeight = entity.getBbHeight() * 0.5F;
    }

    @Override
    public void submit(BoltRenderState state, PoseStack poseStack, SubmitNodeCollector renderTasks, CameraRenderState cameraState) {
        if (state.glimmer) {
            poseStack.pushPose();
            poseStack.translate(0.0D, state.halfHeight, 0.0D);
            poseStack.rotate(cameraState.orientation);
            poseStack.scale(GLIMMER_SPRITE_SIZE, GLIMMER_SPRITE_SIZE, GLIMMER_SPRITE_SIZE);
            renderTasks.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentEmissive(TEXTURE), (pose, consumer) -> {
                glimmerVertex(consumer, pose, -0.5F, -0.5F, 0.0F, 1.0F);
                glimmerVertex(consumer, pose, 0.5F, -0.5F, 1.0F, 1.0F);
                glimmerVertex(consumer, pose, 0.5F, 0.5F, 1.0F, 0.0F);
                glimmerVertex(consumer, pose, -0.5F, 0.5F, 0.0F, 0.0F);
            });
            poseStack.popPose();
        }
        super.submit(state, poseStack, renderTasks, cameraState);
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

    public static final class BoltRenderState extends EntityRenderState {
        boolean glimmer;
        float halfHeight;
    }
}
