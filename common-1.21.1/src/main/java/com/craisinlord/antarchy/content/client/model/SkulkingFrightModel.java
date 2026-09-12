package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.SkulkingFrightEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SkulkingFrightModel extends GeoModel<SkulkingFrightEntity> {
    @Override
    public ResourceLocation getModelResource(SkulkingFrightEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/skulking_fright.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SkulkingFrightEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/skulking_fright.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SkulkingFrightEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/skulking_fright.animation.json");
    }
}
