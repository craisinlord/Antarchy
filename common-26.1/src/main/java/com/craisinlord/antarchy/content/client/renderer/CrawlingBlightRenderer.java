package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CrawlingBlightModel;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class CrawlingBlightRenderer extends GeoEntityRenderer<CrawlingBlightEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> WALL_CLIMBING = DataTicket.create("antarchy_crawling_blight_wall_climbing", Boolean.class);

    public CrawlingBlightRenderer(EntityRendererProvider.Context context) {
        super(context, new CrawlingBlightModel());
        this.shadowRadius = 0.5F;
        this.withScale(0.85F);
    }

    @Override
    public void addRenderData(CrawlingBlightEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(WALL_CLIMBING, animatable.isWallClimbing());
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);

        if (renderPassInfo.getOrDefaultGeckolibData(WALL_CLIMBING, false)) {
            renderPassInfo.poseStack().mulPose(Axis.XP.rotationDegrees(-90.0F));
            renderPassInfo.poseStack().translate(0, -0.5D, 0.5D);
        }
    }
}
