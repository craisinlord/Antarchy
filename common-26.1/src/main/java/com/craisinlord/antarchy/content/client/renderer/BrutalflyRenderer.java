package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.BrutalflyModel;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import com.geckolib.constant.dataticket.DataTicket;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class BrutalflyRenderer extends GeoEntityRenderer<BrutalflyEntity, LivingEntityRenderState> {
    public static final DataTicket<Boolean> COCOONED = DataTicket.create("antarchy_brutalfly_cocooned", Boolean.class);

    private static final double COCOONED_VISUAL_Y_OFFSET = 1.0D;

    public BrutalflyRenderer(EntityRendererProvider.Context context) {
        super(context, new BrutalflyModel());
        this.shadowRadius = 1.35F;
        this.withRenderLayer(new BrutalflyEmissiveLayer(this));
    }

    @Override
    public void extractRenderState(BrutalflyEntity entity, LivingEntityRenderState state, float partialTick) {
        this.shadowRadius = entity.isCocooned() ? 0.25F : 1.35F;
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public void addRenderData(BrutalflyEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(COCOONED, animatable.isCocooned());
    }

    @Override
    public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        if (renderState.getOrDefaultGeckolibData(COCOONED, false)) {
            return RenderTypes.entityCutout(texture);
        }

        return super.getRenderType(renderState, texture);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        if (renderPassInfo.getOrDefaultGeckolibData(COCOONED, false)) {
            renderPassInfo.poseStack().translate(0.0D, COCOONED_VISUAL_Y_OFFSET, 0.0D);
        }
    }

    @Override
    protected int getBlockLightLevel(BrutalflyEntity entity, BlockPos pos) {
        return entity.isCocooned() ? 8 : super.getBlockLightLevel(entity, pos);
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }

    private static final class BrutalflyEmissiveLayer extends GeoRenderLayer<BrutalflyEntity, Void, LivingEntityRenderState> {
        private static final RenderType EMISSIVE_TYPE = RenderTypes.eyes(BrutalflyModel.EMISSIVE_TEXTURE);

        private BrutalflyEmissiveLayer(GeoRenderer<BrutalflyEntity, Void, LivingEntityRenderState> renderer) {
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
