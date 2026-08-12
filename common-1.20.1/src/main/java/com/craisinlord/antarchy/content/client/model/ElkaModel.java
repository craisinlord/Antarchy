package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ElkaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ElkaModel extends GeoModel<ElkaEntity> {
    private static final ResourceLocation ADULT_GEO = rl("geo/elka.geo.json");
    private static final ResourceLocation BABY_GEO = rl("geo/elka_baby.geo.json");
    private static final ResourceLocation ADULT_ANIM = rl("animations/elka.animation.json");
    private static final ResourceLocation BABY_ANIM = rl("animations/elka_baby.animation.json");
    private static final ResourceLocation ADULT_TEXTURE = rl("textures/entity/elka.png");
    private static final ResourceLocation BABY_TEXTURE = rl("textures/entity/elka_baby.png");

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(Antarchy.MODID, path);
    }

    @Override
    public ResourceLocation getModelResource(ElkaEntity animatable) {
        return animatable.isBaby() ? BABY_GEO : ADULT_GEO;
    }

    @Override
    public ResourceLocation getAnimationResource(ElkaEntity animatable) {
        return animatable.isBaby() ? BABY_ANIM : ADULT_ANIM;
    }

    @Override
    public ResourceLocation getTextureResource(ElkaEntity animatable) {
        return animatable.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
    }
}
