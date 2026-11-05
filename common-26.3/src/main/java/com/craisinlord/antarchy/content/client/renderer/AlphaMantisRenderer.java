package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.AlphaMantisModel;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
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
import net.minecraft.util.ARGB;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class AlphaMantisRenderer extends GeoEntityRenderer<AlphaMantisEntity, LivingEntityRenderState> {
    public AlphaMantisRenderer(EntityRendererProvider.Context context) {
        super(context, new AlphaMantisModel());
        this.shadowRadius = 1.5F;
        this.withScale(1.875F);
        this.withRenderLayer(new GlimmerEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        if (renderState.getOrDefaultGeckolibData(AlphaMantisModel.GLIMMER_ECHO, false)) {
            return RenderTypes.entityTranslucent(texture);
        }
        return super.getRenderType(renderState, texture);
    }

    @Override
    public int getRenderColor(AlphaMantisEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    private static final class GlimmerEmissiveLayer extends GeoRenderLayer<AlphaMantisEntity, Void, LivingEntityRenderState> {
        private GlimmerEmissiveLayer(GeoRenderer<AlphaMantisEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(AlphaMantisModel.GLIMMER_ECHO, false)) return;
            RenderType emissiveType = RenderTypes.eyes(GlimmerEchoRendering.emissiveTexture("alpha_mantis"));
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, emissiveType, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(GlimmerEchoRendering.ALPHA, 1.0F, 1.0F, 1.0F)));
                poseStack.popPose();
            });
        }
    }
}
