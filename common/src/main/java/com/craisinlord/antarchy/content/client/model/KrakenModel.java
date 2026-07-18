package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KrakenModel extends GeoModel<KrakenEntity> {
    public static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/kraken_emissive.png");

    @Override
    public ResourceLocation getModelResource(KrakenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "geo/kraken.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KrakenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "textures/entity/kraken.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KrakenEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", "animations/kraken.animation.json");
    }
}
