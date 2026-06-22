package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrutalflyModel extends GeoModel<BrutalflyEntity> {
    @Override
    public ResourceLocation getModelResource(BrutalflyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/brutalfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BrutalflyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/brutalfly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BrutalflyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/brutalfly.animation.json");
    }
}
