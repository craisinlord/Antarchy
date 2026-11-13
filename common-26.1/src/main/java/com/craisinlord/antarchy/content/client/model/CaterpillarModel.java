package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class CaterpillarModel extends GeoModel<CaterpillarEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "caterpillar");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/caterpillar.png");
    }

    @Override
    public Identifier getAnimationResource(CaterpillarEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "caterpillar");
    }
}
