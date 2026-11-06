package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.AntModel;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;

public class AntRenderer extends GeoEntityRenderer<BaseAntEntity, LivingEntityRenderState> {
    private static final DataTicket<Boolean> BABY = DataTicket.create("antarchy_ant_baby", Boolean.class);
    private static final float ADULT_SHADOW_RADIUS = 0.3F;
    private static final float ADULT_SCALE = 0.85F;
    private static final float BABY_SCALE = 0.5F;

    public AntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        this.withRenderLayer(new AntCarriedFoodGeoLayer(this));
    }

    @Override
    public void extractRenderState(BaseAntEntity entity, LivingEntityRenderState renderState, float partialTick) {
        this.shadowRadius = entity.isBaby() ? ADULT_SHADOW_RADIUS * BABY_SCALE : ADULT_SHADOW_RADIUS;
        super.extractRenderState(entity, renderState, partialTick);
    }

    @Override
    public void addRenderData(BaseAntEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(BABY, animatable.isBaby());
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);

        if (renderPassInfo.getOrDefaultGeckolibData(BABY, false)) {
            renderPassInfo.poseStack().scale(ADULT_SCALE * BABY_SCALE, ADULT_SCALE * BABY_SCALE, ADULT_SCALE * BABY_SCALE);
        } else {
            renderPassInfo.poseStack().scale(ADULT_SCALE, ADULT_SCALE, ADULT_SCALE);
        }
    }
}
