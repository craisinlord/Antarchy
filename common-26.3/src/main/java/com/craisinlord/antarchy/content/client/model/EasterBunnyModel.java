package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class EasterBunnyModel extends GeoModel<EasterBunnyEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "easter_bunny");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/easter_bunny.png");
    }

    @Override
    public Identifier getAnimationResource(EasterBunnyEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "easter_bunny");
    }
}
