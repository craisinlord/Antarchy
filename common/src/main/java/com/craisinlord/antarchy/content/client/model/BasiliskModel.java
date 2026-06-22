package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BasiliskModel extends GeoModel<BasiliskEntity> {

    @Override
    public ResourceLocation getModelResource(BasiliskEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/basilisk.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BasiliskEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/basilisk.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BasiliskEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/basilisk.animation.json");
    }
}
