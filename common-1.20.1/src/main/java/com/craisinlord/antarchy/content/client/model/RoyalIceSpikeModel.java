package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalIceSpikeModel extends GeoModel<RoyalIceSpikeEntity> {
    @Override
    public ResourceLocation getModelResource(RoyalIceSpikeEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/ice_spike.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalIceSpikeEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "textures/entity/king/ice_spike.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalIceSpikeEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/ice_spike.animation.json");
    }
}
