package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.SpitBugProjectileEntity;
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

public class SpitBugProjectileRenderer extends EntityRenderer<SpitBugProjectileEntity, SpitBugProjectileRenderer.SpitBugProjectileRenderState> {
    private static final Identifier PROJECTILE_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType PROJECTILE_RENDER_TYPE = RenderTypes.entityTranslucent(PROJECTILE_TEXTURE);

    public SpitBugProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(SpitBugProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public SpitBugProjectileRenderState createRenderState() {
        return new SpitBugProjectileRenderState();
    }

    @Override
    public void extractRenderState(SpitBugProjectileEntity entity, SpitBugProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.scale = entity.getRenderScale(partialTick);
        state.lingering = entity.isLingering();
    }

    @Override
    public void submit(SpitBugProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);

        float scale = state.scale;
        poseStack.scale(scale, scale, scale);

        int packedLight = state.lightCoords;
        float alpha = state.lingering ? 0.78F : 0.95F;
        submitNodeCollector.submitCustomGeometry(poseStack, PROJECTILE_RENDER_TYPE, (pose, vertexConsumer) -> {
            addVertex(vertexConsumer, pose, -0.5F, -0.5F, 0.0F, 1.0F, alpha, packedLight);
            addVertex(vertexConsumer, pose, 0.5F, -0.5F, 1.0F, 1.0F, alpha, packedLight);
            addVertex(vertexConsumer, pose, 0.5F, 0.5F, 1.0F, 0.0F, alpha, packedLight);
            addVertex(vertexConsumer, pose, -0.5F, 0.5F, 0.0F, 0.0F, alpha, packedLight);
        });

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static void addVertex(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            float alpha,
            int packedLight
    ) {
        vertexConsumer.addVertex(pose, x, y, 0.0F)
                .setColor(0.55F, 1.0F, 0.35F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    public static class SpitBugProjectileRenderState extends EntityRenderState {
        public float scale = 1.0F;
        public boolean lingering;
    }
}
