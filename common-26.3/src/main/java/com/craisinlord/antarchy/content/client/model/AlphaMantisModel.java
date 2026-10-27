package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class AlphaMantisModel extends GeoModel<AlphaMantisEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "mantis");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/mantis_alpha.png");
    }

    @Override
    public Identifier getAnimationResource(AlphaMantisEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "mantis");
    }
}
