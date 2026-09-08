package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalBlackHoleModel extends GeoModel<RoyalBlackHoleEntity> {
    @Override
    public ResourceLocation getModelResource(RoyalBlackHoleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/black_hole.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalBlackHoleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/queen/black_hole.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalBlackHoleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/black_hole.animation.json");
    }
}
