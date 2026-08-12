package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.EmperorScorpionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EmperorScorpionModel extends GeoModel<EmperorScorpionEntity> {
    public static final ResourceLocation NORMAL_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion.png");
    public static final ResourceLocation HARDEN_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion_harden.png");

    public static final ResourceLocation EMISSIVE_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/emperor_scorpion/emperor_scorpion_emissive.png");

    @Override
    public ResourceLocation getModelResource(EmperorScorpionEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/emperor_scorpion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EmperorScorpionEntity animatable) {
        return NORMAL_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(EmperorScorpionEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/emperor_scorpion.animation.json");
    }
}
