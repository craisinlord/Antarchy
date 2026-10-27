package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class RoyalIceSpikeModel extends GeoModel<RoyalIceSpikeEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "ice_spike");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/king/ice_spike.png");
    }

    @Override
    public Identifier getAnimationResource(RoyalIceSpikeEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "ice_spike");
    }
}
