package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class MissileSquidModel extends GeoModel<MissileSquidEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "missile_squid");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "textures/entity/missile_squid.png");
    }

    @Override
    public Identifier getAnimationResource(MissileSquidEntity animatable) {
        return Identifier.fromNamespaceAndPath("antarchy", "missile_squid");
    }
}
