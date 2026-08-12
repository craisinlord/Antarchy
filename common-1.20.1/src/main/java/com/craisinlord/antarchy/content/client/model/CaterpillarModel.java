package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CaterpillarModel extends GeoModel<CaterpillarEntity> {
    @Override
    public ResourceLocation getModelResource(CaterpillarEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/caterpillar.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CaterpillarEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "textures/entity/caterpillar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CaterpillarEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/caterpillar.animation.json");
    }
}
