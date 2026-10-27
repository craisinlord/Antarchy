package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.HushProjectileEntity;
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

public class HushProjectileRenderer extends EntityRenderer<HushProjectileEntity, EntityRenderState> {
    private static final Identifier ORB_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType ORB_RENDER_TYPE = RenderTypes.entityTranslucent(ORB_TEXTURE);

    public HushProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(HushProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.scale(0.55F, 0.55F, 0.55F);

        int packedLight = state.lightCoords;
        submitNodeCollector.submitCustomGeometry(poseStack, ORB_RENDER_TYPE, (pose, vertexConsumer) -> {
            addVertex(vertexConsumer, pose, -0.5F, -0.5F, 0.0F, 1.0F, packedLight);
            addVertex(vertexConsumer, pose, 0.5F, -0.5F, 1.0F, 1.0F, packedLight);
            addVertex(vertexConsumer, pose, 0.5F, 0.5F, 1.0F, 0.0F, packedLight);
            addVertex(vertexConsumer, pose, -0.5F, 0.5F, 0.0F, 0.0F, packedLight);
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
            int packedLight
    ) {
        vertexConsumer.addVertex(pose, x, y, 0.0F)
                .setColor(1.0F, 0.08F, 0.08F, 0.95F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
