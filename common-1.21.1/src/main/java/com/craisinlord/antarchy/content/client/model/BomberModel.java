package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.BomberEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BomberModel extends GeoModel<BomberEntity> {
    @Override
    public ResourceLocation getModelResource(BomberEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/bomber.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BomberEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/bomber.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BomberEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/bomber.animation.json");
    }
}
