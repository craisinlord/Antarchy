package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EmperorScorpionModel extends GeoModel<EmperorScorpionEntity> {

    @Override
    public ResourceLocation getModelResource(EmperorScorpionEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/emperor_scorpion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EmperorScorpionEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/emperor_scorpion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EmperorScorpionEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/emperor_scorpion.animation.json");
    }
}
