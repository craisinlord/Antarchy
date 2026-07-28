package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TentacleModel extends GeoModel<TentacleEntity> {
    @Override
    public ResourceLocation getModelResource(TentacleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "geo/tentacle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TentacleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/tentacle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TentacleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "animations/tenctacle.animation.json");
    }
}
