package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class VortexModel extends GeoModel<VortexEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "vortex");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/vortex.png");
    }

    @Override
    public Identifier getAnimationResource(VortexEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "vortex");
    }
}
