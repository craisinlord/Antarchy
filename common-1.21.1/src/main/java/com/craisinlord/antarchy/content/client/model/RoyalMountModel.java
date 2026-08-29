package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalMountModel extends GeoModel<RoyalMountEntity> {
    @Override
    public ResourceLocation getModelResource(RoyalMountEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/" + animatable.geoNameForRender() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalMountEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/" + animatable.geoNameForRender() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalMountEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/" + animatable.geoNameForRender() + ".animation.json");
    }
}
