package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MantisModel;
import com.craisinlord.antarchy.content.entity.MantisEntity;
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

public class MantisRenderer extends GeoEntityRenderer<MantisEntity, LivingEntityRenderState> {
    public MantisRenderer(EntityRendererProvider.Context context) {
        super(context, new MantisModel());
        this.shadowRadius = 1.0F;
        this.withScale(1.25F);
        this.withRenderLayer(new GlimmerEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        if (renderState.getOrDefaultGeckolibData(MantisModel.GLIMMER_ECHO, false)) {
            return RenderTypes.entityTranslucent(texture);
        }
        return super.getRenderType(renderState, texture);
    }

    @Override
    public int getRenderColor(MantisEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    private static final class GlimmerEmissiveLayer extends GeoRenderLayer<MantisEntity, Void, LivingEntityRenderState> {
        private GlimmerEmissiveLayer(GeoRenderer<MantisEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(MantisModel.GLIMMER_ECHO, false)) return;
            RenderType emissiveType = RenderTypes.eyes(GlimmerEchoRendering.emissiveTexture("mantis"));
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
