package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class DimensionalTearModel extends GeoModel<DimensionalTearEntity> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/dimensional_tear.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "dimensional_tear");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DimensionalTearEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "dimensional_tear");
    }
}
