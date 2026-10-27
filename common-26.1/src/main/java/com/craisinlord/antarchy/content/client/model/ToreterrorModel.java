package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class ToreterrorModel extends GeoModel<ToreterrorEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "toreterror");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/toreterror.png");
    }

    @Override
    public Identifier getAnimationResource(ToreterrorEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "toreterror");
    }
}
