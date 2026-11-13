package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.OctopusBombModel;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
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

public class OctopusBombRenderer extends GeoEntityRenderer<OctopusBombEntity, LivingEntityRenderState> {
    public OctopusBombRenderer(EntityRendererProvider.Context context) {
        super(context, new OctopusBombModel());
        this.shadowRadius = 1.2F;
        this.withRenderLayer(new OctopusBombEmissiveLayer(this));
    }

    @Override
    public int getRenderColor(OctopusBombEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(), super.getRenderColor(animatable, relatedObject, partialTick));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState state, Identifier texture) {
        return state.getOrDefaultGeckolibData(OctopusBombModel.GLIMMER_ECHO, false)
                ? RenderTypes.entityTranslucent(texture) : super.getRenderType(state, texture);
    }

    private static final class OctopusBombEmissiveLayer extends GeoRenderLayer<OctopusBombEntity, Void, LivingEntityRenderState> {
        private OctopusBombEmissiveLayer(GeoRenderer<OctopusBombEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> info, SubmitNodeCollector tasks) {
            if (!info.willRender()) {
                return;
            }
            boolean glimmer = info.getOrDefaultGeckolibData(OctopusBombModel.GLIMMER_ECHO, false);
            RenderType type = RenderTypes.eyes(glimmer ? OctopusBombModel.GLIMMER_EMISSIVE_TEXTURE : OctopusBombModel.EMISSIVE_TEXTURE);
            PoseStack stack = info.poseStack();
            tasks.order(1).submitCustomGeometry(stack, type, (pose, buffer) -> {
                stack.pushPose();
                stack.last().set(pose);
                info.renderPosed(() -> info.model().render(info, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY,
                        glimmer ? GlimmerEchoRendering.emissiveColor() : 0xFFFFFFFF));
                stack.popPose();
            });
        }
    }
}
