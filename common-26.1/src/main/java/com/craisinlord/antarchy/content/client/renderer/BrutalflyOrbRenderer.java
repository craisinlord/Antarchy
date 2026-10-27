package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyOrbEntity;
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

public class BrutalflyOrbRenderer extends EntityRenderer<BrutalflyOrbEntity, BrutalflyOrbRenderer.BrutalflyOrbRenderState> {
    private static final Identifier ORB_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType ORB_RENDER_TYPE = RenderTypes.entityTranslucent(ORB_TEXTURE);

    public BrutalflyOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(BrutalflyOrbEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public BrutalflyOrbRenderState createRenderState() {
        return new BrutalflyOrbRenderState();
    }

    @Override
    public void extractRenderState(BrutalflyOrbEntity entity, BrutalflyOrbRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.lingering = entity.isLingeringOrb();
        state.scale = state.lingering ? entity.getOrbRadius(partialTick) * 0.38F : 0.8F;
        state.poison = entity.getOrbVariant() == BrutalflyOrbEntity.OrbVariant.POISON;
    }

    @Override
    public void submit(BrutalflyOrbRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);

        float scale = state.scale;
        poseStack.scale(scale, scale, scale);

        float red;
        float green;
        float blue;
        if (state.poison) {
            red = 0.32F;
            green = 0.92F;
            blue = 0.26F;
        } else {
            red = 1.0F;
            green = 0.62F;
            blue = 0.08F;
        }

        float alpha = state.lingering ? 0.72F : 0.95F;
        int packedLight = state.lightCoords;
        collector.submitCustomGeometry(poseStack, ORB_RENDER_TYPE, (pose, vertexConsumer) -> {
            addVertex(vertexConsumer, pose, -0.5F, -0.5F, 0.0F, 1.0F, red, green, blue, alpha, packedLight);
            addVertex(vertexConsumer, pose, 0.5F, -0.5F, 1.0F, 1.0F, red, green, blue, alpha, packedLight);
            addVertex(vertexConsumer, pose, 0.5F, 0.5F, 1.0F, 0.0F, red, green, blue, alpha, packedLight);
            addVertex(vertexConsumer, pose, -0.5F, 0.5F, 0.0F, 0.0F, red, green, blue, alpha, packedLight);
        });

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void addVertex(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
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
        vertexConsumer.addVertex(pose.pose(), x, y, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    public static class BrutalflyOrbRenderState extends EntityRenderState {
        public boolean lingering;
        public boolean poison = true;
        public float scale = 0.8F;
    }
}
