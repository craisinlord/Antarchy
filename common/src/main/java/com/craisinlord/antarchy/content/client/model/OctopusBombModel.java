package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OctopusBombModel extends GeoModel<OctopusBombEntity> {
    @Override
    public ResourceLocation getModelResource(OctopusBombEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "geo/octopus_bomb.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OctopusBombEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/octopus_bomb.png");
    }

    @Override
    public ResourceLocation getAnimationResource(OctopusBombEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "animations/octopus_bomb.animation.json");
    }
}
