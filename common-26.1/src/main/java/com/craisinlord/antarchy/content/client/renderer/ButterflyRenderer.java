package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ButterflyModel;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
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
import net.minecraft.resources.Identifier;
import com.geckolib.renderer.GeoEntityRenderer;

public class ButterflyRenderer extends GeoEntityRenderer<ButterflyEntity, LivingEntityRenderState> {
    private static final float VISUAL_SCALE = 0.8F;
    private static final float SHADOW_RADIUS = 0.18F;

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel());
        this.shadowRadius = SHADOW_RADIUS;
        this.withScale(VISUAL_SCALE);
        this.withRenderLayer(new GlimmerEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        if (renderState.getOrDefaultGeckolibData(ButterflyModel.GLIMMER_ECHO, false)) {
            return RenderTypes.entityTranslucent(texture);
        }
        return super.getRenderType(renderState, texture);
    }

    @Override
    public int getRenderColor(ButterflyEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    private static final class GlimmerEmissiveLayer extends GeoRenderLayer<ButterflyEntity, Void, LivingEntityRenderState> {
        private GlimmerEmissiveLayer(GeoRenderer<ButterflyEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(ButterflyModel.GLIMMER_ECHO, false)) {
                return;
            }
            RenderType emissiveType = RenderTypes.eyes(GlimmerEchoRendering.emissiveTexture("butterfly"));
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, emissiveType, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, GlimmerEchoRendering.emissiveColor()));
                poseStack.popPose();
            });
        }
    }
}
