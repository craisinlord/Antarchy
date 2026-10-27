package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.AppleCowModel;
import com.craisinlord.antarchy.content.entity.AppleCowEntity;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.EnchantedGoldenAppleCow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

public class AppleCowRenderer extends GeoEntityRenderer<AppleCowEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> ENCHANTED_ADULT = DataTicket.create("antarchy_apple_cow_enchanted_adult", Boolean.class);
    private static final float ADULT_SHADOW_RADIUS = 0.7F;
    private static final float BABY_SCALE = 0.85F;

    public AppleCowRenderer(EntityRendererProvider.Context context) {
        super(context, new AppleCowModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.withRenderLayer(new EnchantedLayer(this));
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(texture);
    }

    @Override
    public void extractRenderState(AppleCowEntity entity, LivingEntityRenderState renderState, float partialTick) {
        this.shadowRadius = entity.isBaby() ? ADULT_SHADOW_RADIUS * BABY_SCALE : ADULT_SHADOW_RADIUS;
        super.extractRenderState(entity, renderState, partialTick);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);

        if (renderPassInfo.getOrDefaultGeckolibData(AppleCowModel.BABY, false)) {
            renderPassInfo.poseStack().scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
        }
    }

    private static final class EnchantedLayer extends GeoRenderLayer<AppleCowEntity, Void, LivingEntityRenderState> {
        private EnchantedLayer(AppleCowRenderer renderer) {
            super(renderer);
        }

        @Override
        public void addRenderData(AppleCowEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
            renderState.addGeckolibData(ENCHANTED_ADULT, animatable instanceof EnchantedGoldenAppleCow && !animatable.isBaby());
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender() || !renderPassInfo.getOrDefaultGeckolibData(ENCHANTED_ADULT, false)) {
                return;
            }

            Identifier texture = renderPassInfo.getGeckolibData(AppleCowModel.TEXTURE);
            if (texture == null) {
                return;
            }

            int light = renderPassInfo.packedLight();
            submitModelCopy(renderPassInfo, renderTasks, RenderTypes.entityGlint(), light);
            submitModelCopy(renderPassInfo, renderTasks, RenderTypes.entityCutout(texture), light);
        }

        private static void submitModelCopy(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks, RenderType renderType, int light) {
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
                poseStack.popPose();
            });
        }
    }
}
