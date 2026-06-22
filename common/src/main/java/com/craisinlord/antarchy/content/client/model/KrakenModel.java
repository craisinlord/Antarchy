package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KrakenModel extends GeoModel<KrakenEntity> {
    @Override
    public ResourceLocation getModelResource(KrakenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "geo/kraken.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KrakenEntity animatable) {
        return animatable.isPhaseTwo()
                ? ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/kraken_purple.png")
                : ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/kraken.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KrakenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "animations/kraken.animation.json");
    }
}
