package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ToreterrorModel extends GeoModel<ToreterrorEntity> {

    @Override
    public ResourceLocation getModelResource(ToreterrorEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/toreterror.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ToreterrorEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "textures/entity/toreterror.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ToreterrorEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/toreterror.animation.json");
    }
}
