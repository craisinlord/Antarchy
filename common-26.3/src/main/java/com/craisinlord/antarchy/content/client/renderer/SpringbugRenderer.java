package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.SpringbugModel;
import com.craisinlord.antarchy.content.entity.SpringbugEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class SpringbugRenderer extends GeoEntityRenderer<SpringbugEntity, LivingEntityRenderState> {
    public static final DataTicket<Boolean> CAMOUFLAGED = DataTicket.create("antarchy_springbug_camouflaged", Boolean.class);
    public static final DataTicket<Boolean> CLINGING = DataTicket.create("antarchy_springbug_clinging", Boolean.class);
    public static final DataTicket<Float> BB_HEIGHT = DataTicket.create("antarchy_springbug_bb_height", Float.class);
    public static final DataTicket<Integer> VISUAL_ALPHA = DataTicket.create("antarchy_springbug_visual_alpha", Integer.class);

    public SpringbugRenderer(EntityRendererProvider.Context context) {
        super(context, new SpringbugModel());
        this.shadowRadius = 6.0F;
        this.withRenderLayer(new SpringbugEmissiveLayer(this));
    }

    @Override
    public void extractRenderState(SpringbugEntity entity, LivingEntityRenderState renderState, float partialTick) {
        this.shadowRadius = entity.isCamouflaged() ? 1.0F : 6.0F;
        super.extractRenderState(entity, renderState, partialTick);
    }

    @Override
    public void addRenderData(SpringbugEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(CAMOUFLAGED, animatable.isCamouflaged());
        renderState.addGeckolibData(CLINGING, animatable.isClingingToCeiling());
        renderState.addGeckolibData(BB_HEIGHT, animatable.getBbHeight());
        renderState.addGeckolibData(VISUAL_ALPHA, visualAlpha(animatable, partialTick));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return renderState.getOrDefaultGeckolibData(CAMOUFLAGED, false) ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
    }

    @Override
    public int getRenderColor(SpringbugEntity animatable, Void relatedObject, float partialTick) {
        return ARGB.color(visualAlpha(animatable, partialTick), 255, 255, 255);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);

        if (renderPassInfo.getOrDefaultGeckolibData(CLINGING, false)) {
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.translate(0.0F, renderPassInfo.getOrDefaultGeckolibData(BB_HEIGHT, 0.0F) + 0.05F, 0.0F);
            poseStack.rotate(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    private static int visualAlpha(SpringbugEntity animatable, float partialTick) {
        return Math.max(10, Math.min(255, Math.round(animatable.getVisualAlpha(partialTick) * 255.0F)));
    }

    private static final class SpringbugEmissiveLayer extends GeoRenderLayer<SpringbugEntity, Void, LivingEntityRenderState> {
        private SpringbugEmissiveLayer(GeoRenderer<SpringbugEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }

            RenderType emissiveType = renderPassInfo.getOrDefaultGeckolibData(CAMOUFLAGED, false)
                    ? RenderTypes.entityTranslucent(SpringbugModel.EMISSIVE_TEXTURE)
                    : RenderTypes.eyes(SpringbugModel.EMISSIVE_TEXTURE);
            int alpha = renderPassInfo.getOrDefaultGeckolibData(VISUAL_ALPHA, 255);
            int colour = (alpha << 24) | 0x00FFFFFF;
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, emissiveType, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, colour));
                poseStack.popPose();
            });
        }
    }
}
