package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.MantisEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MantisModel extends GeoModel<MantisEntity> {
    @Override
    public ResourceLocation getModelResource(MantisEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/mantis.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MantisEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/mantis.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MantisEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/mantis.animation.json");
    }
}
