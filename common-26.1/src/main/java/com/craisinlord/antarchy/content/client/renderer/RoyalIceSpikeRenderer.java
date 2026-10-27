package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalIceSpikeModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class RoyalIceSpikeRenderer extends GeoEntityRenderer<RoyalIceSpikeEntity, EntityRenderState> {
    public RoyalIceSpikeRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalIceSpikeModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected boolean affectedByCulling(RoyalIceSpikeEntity entity) {
        return false;
    }
}
