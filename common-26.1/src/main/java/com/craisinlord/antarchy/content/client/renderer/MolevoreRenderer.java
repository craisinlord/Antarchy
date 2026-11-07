package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MolevoreModel;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MolevoreRenderer extends GeoEntityRenderer<MolevoreEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> HEAD_TRACKING = DataTicket.create("antarchy_molevore_head_tracking", Boolean.class);

    public MolevoreRenderer(EntityRendererProvider.Context context) {
        super(context, new MolevoreModel());
        this.shadowRadius = 2.0F;
    }

    @Override
    public void addRenderData(MolevoreEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(HEAD_TRACKING, animatable.isHeadTrackingActive());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        if (renderPassInfo.getOrDefaultGeckolibData(HEAD_TRACKING, false)) {
            MolevoreModel.applyHeadTracking(renderPassInfo.renderState(), snapshots);
        }
    }
}
