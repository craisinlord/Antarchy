package com.craisinlord.antarchy.content.client.renderer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public final class RoyalIndicatorGeoLayer<T extends GeoAnimatable, O, R extends GeoRenderState> extends GeoRenderLayer<T, O, R> {
    public RoyalIndicatorGeoLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    public void addRenderData(T animatable, O relatedObject, R renderState, float partialTick) {
        if (animatable instanceof LivingEntity living) {
            RoyalIndicatorRenderer.extractIndicatorState(living, renderState);
        } else {
            RoyalIndicatorRenderer.clearIndicatorState(renderState);
        }
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!(renderPassInfo.renderState() instanceof EntityRenderState state)) {
            return;
        }
        if (!renderPassInfo.getOrDefaultGeckolibData(RoyalIndicatorRenderer.COMMANDED, false)
                && !renderPassInfo.getOrDefaultGeckolibData(RoyalIndicatorRenderer.JUDGED, false)) {
            return;
        }
        PoseStack poseStack = renderPassInfo.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.0F, -0.01F, 0.0F);
        float bodyYaw = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_BODY_YAW, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180.0F));
        RoyalIndicatorRenderer.submit(poseStack, renderTasks, state);
        poseStack.popPose();
    }
}
