package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class TentacleModel extends GeoModel<TentacleEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "tentacle");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "textures/entity/tentacle.png");
    }

    @Override
    public Identifier getAnimationResource(TentacleEntity animatable) {
        return Identifier.fromNamespaceAndPath("antarchy", "tenctacle");
    }
}
