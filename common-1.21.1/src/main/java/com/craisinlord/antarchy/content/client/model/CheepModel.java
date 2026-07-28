package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.CheepEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CheepModel extends GeoModel<CheepEntity> {
    @Override
    public ResourceLocation getModelResource(CheepEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/cheep.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CheepEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/cheep/cheep_magenta.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CheepEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/cheep.animation.json");
    }
}
