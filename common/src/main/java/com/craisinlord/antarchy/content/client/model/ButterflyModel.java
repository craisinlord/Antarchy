package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ButterflyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ButterflyModel extends GeoModel<ButterflyEntity> {
    private static final String TEXTURE_PATH_PREFIX = "textures/entity/butterfly/butterfly_";

    @Override
    public ResourceLocation getModelResource(ButterflyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/butterfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ButterflyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Antarchy.MODID,
                TEXTURE_PATH_PREFIX + animatable.getTextureVariantName() + ".png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(ButterflyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/butterfly.animation.json");
    }
}
