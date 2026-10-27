package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class OctopusBombModel extends GeoModel<OctopusBombEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "octopus_bomb");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "textures/entity/octopus_bomb.png");
    }

    @Override
    public Identifier getAnimationResource(OctopusBombEntity animatable) {
        return Identifier.fromNamespaceAndPath("antarchy", "octopus_bomb");
    }
}
