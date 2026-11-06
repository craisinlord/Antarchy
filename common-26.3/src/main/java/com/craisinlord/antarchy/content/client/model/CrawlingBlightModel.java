package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.CrawlingBlightEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class CrawlingBlightModel extends GeoModel<CrawlingBlightEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "crawling_blight");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/crawling_blight.png");
    }

    @Override
    public Identifier getAnimationResource(CrawlingBlightEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "crawling_blight");
    }
}
