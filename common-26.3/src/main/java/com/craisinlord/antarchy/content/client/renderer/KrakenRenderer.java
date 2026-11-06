package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.KrakenModel;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
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
import net.minecraft.resources.Identifier;

public class KrakenRenderer extends GeoEntityRenderer<KrakenEntity, LivingEntityRenderState> {
    public KrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new KrakenModel());
        this.shadowRadius = 2.0F;
        this.withScale(1.5F);
        this.withRenderLayer(new KrakenEmissiveLayer(this));
    }

    @Override
    public int getRenderColor(KrakenEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return renderState.getOrDefaultGeckolibData(KrakenModel.GLIMMER_ECHO, false)
                ? RenderTypes.entityTranslucent(texture)
                : super.getRenderType(renderState, texture);
    }

    private static final class KrakenEmissiveLayer extends GeoRenderLayer<KrakenEntity, Void, LivingEntityRenderState> {
        private KrakenEmissiveLayer(GeoRenderer<KrakenEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }
            boolean glimmerEcho = renderPassInfo.getOrDefaultGeckolibData(KrakenModel.GLIMMER_ECHO, false);
            RenderType emissiveType = RenderTypes.eyes(glimmerEcho ? KrakenModel.GLIMMER_EMISSIVE_TEXTURE : KrakenModel.EMISSIVE_TEXTURE);
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, emissiveType, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY,
                        glimmerEcho ? GlimmerEchoRendering.emissiveColor() : 0xFFFFFFFF));
                poseStack.popPose();
            });
        }
    }
}
