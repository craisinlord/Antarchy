package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.SpringbugEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpringbugModel extends GeoModel<SpringbugEntity> {
    public static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/springbug_emissive.png");

    @Override
    public ResourceLocation getModelResource(SpringbugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/springbug.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpringbugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/springbug.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpringbugEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/springbug.animation.json");
    }
}
