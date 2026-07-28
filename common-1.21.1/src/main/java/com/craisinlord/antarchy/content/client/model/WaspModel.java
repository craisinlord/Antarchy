package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WaspModel extends GeoModel<WaspEntity> {
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/wasp.png");
    private static final ResourceLocation PANDA_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/wasp_panda.png");

    @Override
    public ResourceLocation getModelResource(WaspEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/wasp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WaspEntity animatable) {
        return animatable.isPandaVariant() ? PANDA_TEXTURE : DEFAULT_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WaspEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/wasp.animation.json");
    }
}
