package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ManticoreModel extends GeoModel<ManticoreEntity> {
    @Override
    public ResourceLocation getModelResource(ManticoreEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/manticore.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ManticoreEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "textures/entity/manticore.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ManticoreEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/manticore.animation.json");
    }
}
