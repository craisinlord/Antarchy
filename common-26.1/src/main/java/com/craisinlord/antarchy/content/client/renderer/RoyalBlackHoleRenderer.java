package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBlackHoleModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class RoyalBlackHoleRenderer extends GeoEntityRenderer<RoyalBlackHoleEntity, EntityRenderState> {
    public static final DataTicket<Float> VISUAL_GROWTH = DataTicket.create("antarchy_royal_black_hole_visual_growth", Float.class);

    public RoyalBlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBlackHoleModel<>());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void extractRenderState(RoyalBlackHoleEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.lightCoords = 0xF000F0;
    }

    @Override
    public void addRenderData(RoyalBlackHoleEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(VISUAL_GROWTH, animatable.visualGrowth());
    }

    @Override
    public void preRenderPass(RenderPassInfo<EntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        float scale = 1.5F * renderPassInfo.getOrDefaultGeckolibData(VISUAL_GROWTH, 1.0F);
        renderPassInfo.poseStack().scale(scale, scale, scale);
        super.preRenderPass(renderPassInfo, renderTasks);
    }

    @Override
    public RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(this.getTextureLocation(renderState));
    }

    @Override
    protected boolean affectedByCulling(RoyalBlackHoleEntity entity) {
        return false;
    }
}
