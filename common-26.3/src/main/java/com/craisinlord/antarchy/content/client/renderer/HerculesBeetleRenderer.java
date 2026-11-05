package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.HerculesBeetleModel;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
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

public class HerculesBeetleRenderer extends GeoEntityRenderer<HerculesBeetleEntity, LivingEntityRenderState> {
    public HerculesBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new HerculesBeetleModel());
        this.withRenderLayer(new HerculesBeetleEmissiveLayer(this));
    }

    @Override
    public int getRenderColor(HerculesBeetleEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return renderState.getOrDefaultGeckolibData(HerculesBeetleModel.GLIMMER_ECHO, false)
                ? RenderTypes.entityTranslucent(texture)
                : super.getRenderType(renderState, texture);
    }

    private static final class HerculesBeetleEmissiveLayer extends GeoRenderLayer<HerculesBeetleEntity, Void, LivingEntityRenderState> {
        private HerculesBeetleEmissiveLayer(GeoRenderer<HerculesBeetleEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }
            boolean glimmerEcho = renderPassInfo.getOrDefaultGeckolibData(HerculesBeetleModel.GLIMMER_ECHO, false);
            RenderType emissiveType = RenderTypes.eyes(glimmerEcho ? HerculesBeetleModel.GLIMMER_EMISSIVE_TEXTURE : HerculesBeetleModel.EMISSIVE_TEXTURE);
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
