package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CrawlingBlightModel extends GeoModel<CrawlingBlightEntity> {
    @Override
    public ResourceLocation getModelResource(CrawlingBlightEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/crawling_blight.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CrawlingBlightEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/crawling_blight.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CrawlingBlightEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/crawling_blight.animation.json");
    }
}
