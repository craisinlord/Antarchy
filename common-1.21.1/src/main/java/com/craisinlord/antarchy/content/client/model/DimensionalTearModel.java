package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DimensionalTearModel extends GeoModel<DimensionalTearEntity> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/dimensional_tear.png");

    @Override
    public ResourceLocation getModelResource(DimensionalTearEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/dimensional_tear.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DimensionalTearEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DimensionalTearEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/dimensional_tear.animation.json");
    }
}
