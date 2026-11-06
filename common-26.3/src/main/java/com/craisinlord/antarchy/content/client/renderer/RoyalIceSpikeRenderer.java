package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalIceSpikeModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class RoyalIceSpikeRenderer extends GeoEntityRenderer<RoyalIceSpikeEntity, EntityRenderState> {
    public RoyalIceSpikeRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalIceSpikeModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return renderState.getOrDefaultGeckolibData(RoyalIceSpikeModel.GLIMMER_ECHO, false)
                ? RenderTypes.entityTranslucent(texture)
                : super.getRenderType(renderState, texture);
    }

    @Override
    public int getRenderColor(RoyalIceSpikeEntity animatable, Void relatedObject, float partialTick) {
        return GlimmerEchoRendering.renderColor(animatable.isGlimmerEcho(),
                super.getRenderColor(animatable, relatedObject, partialTick));
    }

    @Override
    protected boolean affectedByCulling(RoyalIceSpikeEntity entity) {
        return false;
    }
}
