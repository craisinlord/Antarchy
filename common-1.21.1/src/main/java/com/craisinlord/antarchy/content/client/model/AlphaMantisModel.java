package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AlphaMantisModel extends GeoModel<AlphaMantisEntity> {
    @Override
    public ResourceLocation getModelResource(AlphaMantisEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/mantis.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AlphaMantisEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/mantis_alpha.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AlphaMantisEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/mantis.animation.json");
    }
}
