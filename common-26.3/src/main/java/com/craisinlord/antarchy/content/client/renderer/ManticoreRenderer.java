package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ManticoreModel;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
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

public class ManticoreRenderer extends GeoEntityRenderer<ManticoreEntity, LivingEntityRenderState> {
    public ManticoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ManticoreModel());
        this.shadowRadius = 0.9F;
        this.withRenderLayer(new ManticoreEmissiveLayer(this));
    }

    @Override
    public int getRenderColor(ManticoreEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return renderState.getOrDefaultGeckolibData(ManticoreModel.GLIMMER_ECHO, false)
                ? RenderTypes.entityTranslucent(texture)
                : super.getRenderType(renderState, texture);
    }

    private static final class ManticoreEmissiveLayer extends GeoRenderLayer<ManticoreEntity, Void, LivingEntityRenderState> {
        private ManticoreEmissiveLayer(GeoRenderer<ManticoreEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(ManticoreModel.GLIMMER_ECHO, false)) {
                return;
            }
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, RenderTypes.eyes(ManticoreModel.GLIMMER_EMISSIVE_TEXTURE), (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0,
                        OverlayTexture.NO_OVERLAY, GlimmerEchoRendering.emissiveColor()));
                poseStack.popPose();
            });
        }
    }
}
