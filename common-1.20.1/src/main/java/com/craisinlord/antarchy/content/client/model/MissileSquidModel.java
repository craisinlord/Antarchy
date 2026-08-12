package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MissileSquidModel extends GeoModel<MissileSquidEntity> {
    @Override
    public ResourceLocation getModelResource(MissileSquidEntity animatable) {
        return new ResourceLocation("antarchy", "geo/missile_squid.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MissileSquidEntity animatable) {
        return new ResourceLocation("antarchy", "textures/entity/missile_squid.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MissileSquidEntity animatable) {
        return new ResourceLocation("antarchy", "animations/missile_squid.animation.json");
    }
}
