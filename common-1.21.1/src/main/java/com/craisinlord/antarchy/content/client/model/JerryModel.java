package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.JerryEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class JerryModel extends GeoModel<JerryEntity> {
    @Override
    public ResourceLocation getModelResource(JerryEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/" + stageName(animatable) + "_jerry.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JerryEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/jerry/" + stageName(animatable) + "_jerry.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JerryEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/" + stageName(animatable) + "_jerry.animation.json");
    }

    private static String stageName(JerryEntity animatable) {
        return animatable.getStage().getSerializedName();
    }
}
