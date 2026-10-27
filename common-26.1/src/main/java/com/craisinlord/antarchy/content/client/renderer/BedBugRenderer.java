package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BedBugModel;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
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

public class BedBugRenderer extends GeoEntityRenderer<BedBugEntity, LivingEntityRenderState> {
    private static final float ADULT_SCALE = 0.9F;
    private static final float BABY_SCALE = 0.38F;
    private static final float ADULT_SHADOW_RADIUS = 0.65F;

    public BedBugRenderer(EntityRendererProvider.Context context) {
        super(context, new BedBugModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.withScale(ADULT_SCALE);
        this.withRenderLayer(new BedBugEmissiveLayer(this));
    }

    @Override
    public void extractRenderState(BedBugEntity entity, LivingEntityRenderState state, float partialTick) {
        this.shadowRadius = entity.isBaby() ? ADULT_SHADOW_RADIUS * BABY_SCALE : ADULT_SHADOW_RADIUS;
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        if (renderPassInfo.renderState().isBaby) {
            renderPassInfo.poseStack().scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
        }
    }

    private static final class BedBugEmissiveLayer extends GeoRenderLayer<BedBugEntity, Void, LivingEntityRenderState> {
        private static final RenderType EMISSIVE_TYPE = RenderTypes.eyes(BedBugModel.EMISSIVE_TEXTURE);

        private BedBugEmissiveLayer(GeoRenderer<BedBugEntity, Void, LivingEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
            if (!renderPassInfo.willRender()) {
                return;
            }
            PoseStack poseStack = renderPassInfo.poseStack();
            renderTasks.order(1).submitCustomGeometry(poseStack, EMISSIVE_TYPE, (pose, buffer) -> {
                poseStack.pushPose();
                poseStack.last().set(pose);
                renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
                poseStack.popPose();
            });
        }
    }
}
