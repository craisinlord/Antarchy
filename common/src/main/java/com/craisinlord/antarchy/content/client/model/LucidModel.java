package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LucidModel extends GeoModel<LucidEntity> {

    @Override
    public ResourceLocation getModelResource(LucidEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/lucid.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LucidEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/lucid.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LucidEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/lucid.animation.json");
    }
}
