package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EasterBunnyModel extends GeoModel<EasterBunnyEntity> {
    @Override
    public ResourceLocation getModelResource(EasterBunnyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/easter_bunny.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EasterBunnyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/easter_bunny.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EasterBunnyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/easter_bunny.animation.json");
    }
}
