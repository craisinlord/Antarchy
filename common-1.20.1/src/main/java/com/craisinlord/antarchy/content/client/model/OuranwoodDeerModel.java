package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OuranwoodDeerModel extends GeoModel<OuranwoodDeerEntity> {
    private static final ResourceLocation ADULT_GEO = rl("geo/ouranwood_deer.geo.json");
    private static final ResourceLocation BABY_GEO = rl("geo/ouranwood_deer_baby.geo.json");
    private static final ResourceLocation ADULT_ANIM = rl("animations/ouranwood_deer.animation.json");
    private static final ResourceLocation BABY_ANIM = rl("animations/ouranwood_deer_baby.animation.json");
    private static final ResourceLocation BUCK_TEXTURE = rl("textures/entity/ouranwood_deer/deer_buck.png");
    private static final ResourceLocation DOE_TEXTURE = rl("textures/entity/ouranwood_deer/deer_doe.png");
    private static final ResourceLocation STAG_TEXTURE = rl("textures/entity/ouranwood_deer/deer_stag.png");

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(Antarchy.MODID, path);
    }

    @Override
    public ResourceLocation getModelResource(OuranwoodDeerEntity animatable) {
        return animatable.isBaby() ? BABY_GEO : ADULT_GEO;
    }

    @Override
    public ResourceLocation getAnimationResource(OuranwoodDeerEntity animatable) {
        return animatable.isBaby() ? BABY_ANIM : ADULT_ANIM;
    }

    @Override
    public ResourceLocation getTextureResource(OuranwoodDeerEntity animatable) {
        if (animatable.isBaby()) {
            return STAG_TEXTURE;
        }
        return switch (animatable.getVariant()) {
            case BUCK -> BUCK_TEXTURE;
            case DOE -> DOE_TEXTURE;
        };
    }
}
