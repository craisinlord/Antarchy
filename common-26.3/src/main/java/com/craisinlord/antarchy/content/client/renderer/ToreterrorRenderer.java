package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ToreterrorModel;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
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

public class ToreterrorRenderer extends GeoEntityRenderer<ToreterrorEntity, LivingEntityRenderState> {
    public ToreterrorRenderer(EntityRendererProvider.Context context) {
        super(context, new ToreterrorModel());
        this.shadowRadius = 1.6F;
        this.withRenderLayer(new GlimmerEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        if (renderState.getOrDefaultGeckolibData(ToreterrorModel.GLIMMER_ECHO, false)) {
            return RenderTypes.entityTranslucent(texture);
        }
        return super.getRenderType(renderState, texture);
    }

    @Override
    public int getRenderColor(ToreterrorEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }

    private static final class GlimmerEmissiveLayer extends GeoRenderLayer<ToreterrorEntity, Void, LivingEntityRenderState> {
        private GlimmerEmissiveLayer(GeoRenderer<ToreterrorEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(ToreterrorModel.GLIMMER_ECHO, false)) {
                return;
            }
            RenderType emissiveType = RenderTypes.eyes(GlimmerEchoRendering.emissiveTexture("toreterror"));
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
