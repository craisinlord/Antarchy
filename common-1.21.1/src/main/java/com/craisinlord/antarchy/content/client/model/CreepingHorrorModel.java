package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.CreepingHorrorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CreepingHorrorModel extends GeoModel<CreepingHorrorEntity> {
    @Override
    public ResourceLocation getModelResource(CreepingHorrorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/creeping_horror.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CreepingHorrorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/creeping_horror.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CreepingHorrorEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/creeping_horror.animation.json");
    }
}
