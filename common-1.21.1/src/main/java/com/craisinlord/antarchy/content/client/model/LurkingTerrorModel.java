package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.LurkingTerrorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LurkingTerrorModel extends GeoModel<LurkingTerrorEntity> {
    @Override
    public ResourceLocation getModelResource(LurkingTerrorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/lurking_terror.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LurkingTerrorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/lurking_terror.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LurkingTerrorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/lurking_terror.animation.json");
    }
}
