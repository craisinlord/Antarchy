package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.HerculesBeetleModel;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class HerculesBeetleRenderer extends GeoEntityRenderer<HerculesBeetleEntity, LivingEntityRenderState> {
    public HerculesBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new HerculesBeetleModel());
        this.withRenderLayer(new HerculesBeetleEmissiveLayer(this));
    }

    private static final class HerculesBeetleEmissiveLayer extends GeoRenderLayer<HerculesBeetleEntity, Void, LivingEntityRenderState> {
        private static final RenderType EMISSIVE_TYPE = RenderTypes.eyes(HerculesBeetleModel.EMISSIVE_TEXTURE);

        private HerculesBeetleEmissiveLayer(GeoRenderer<HerculesBeetleEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, EMISSIVE_TYPE, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
                poseStack.popPose();
            });
        }
    }
}
