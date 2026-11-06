package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.SkulkingFrightEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class SkulkingFrightModel extends GeoModel<SkulkingFrightEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "skulking_fright");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/skulking_fright.png");
    }

    @Override
    public Identifier getAnimationResource(SkulkingFrightEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "skulking_fright");
    }
}
