package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalBossModel extends GeoModel<RoyalBossEntity> {
    @Override
    public ResourceLocation getModelResource(RoyalBossEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/" + animatable.geoNameForRender() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalBossEntity animatable) {
        String name = animatable.geoNameForRender();
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/" + name + "/" + name + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalBossEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/" + animatable.geoNameForRender() + ".animation.json");
    }
}
