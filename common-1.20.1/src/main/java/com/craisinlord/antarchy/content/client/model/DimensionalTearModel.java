package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DimensionalTearModel extends GeoModel<DimensionalTearEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/vfx/dimensional_tear.png");

    @Override
    public ResourceLocation getModelResource(DimensionalTearEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/dimensional_tear.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DimensionalTearEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DimensionalTearEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/dimensional_tear.animation.json");
    }
}
