package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ScorpionModel extends GeoModel<ScorpionEntity> {
    private static final ResourceLocation MODEL =
            new ResourceLocation(Antarchy.MODID, "geo/scorpion.geo.json");
    private static final ResourceLocation BLUE_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/scorpion/scorpion_blue.png");
    private static final ResourceLocation GREEN_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/scorpion/scorpion_green.png");
    private static final ResourceLocation BLUE_EMISSIVE_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/scorpion/scorpion_blue_emissive.png");
    private static final ResourceLocation GREEN_EMISSIVE_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/scorpion/scorpion_green_emissive.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(Antarchy.MODID, "animations/scorpion.animation.json");

    @Override
    public ResourceLocation getModelResource(ScorpionEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ScorpionEntity animatable) {
        return textureFor(animatable);
    }

    @Override
    public ResourceLocation getAnimationResource(ScorpionEntity animatable) {
        return ANIMATION;
    }

    public static ResourceLocation textureFor(ScorpionEntity animatable) {
        return animatable.getTextureVariant() == ScorpionEntity.GREEN_VARIANT ? GREEN_TEXTURE : BLUE_TEXTURE;
    }

    public static ResourceLocation emissiveTextureFor(ScorpionEntity animatable) {
        return animatable.getTextureVariant() == ScorpionEntity.GREEN_VARIANT ? GREEN_EMISSIVE_TEXTURE : BLUE_EMISSIVE_TEXTURE;
    }
}
