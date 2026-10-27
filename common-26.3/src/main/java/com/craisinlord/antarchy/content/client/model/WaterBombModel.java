package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class WaterBombModel extends GeoModel<WaterBombEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "water_bomb");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/water_bomb.png");
    }

    @Override
    public Identifier getAnimationResource(WaterBombEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "water_bomb");
    }
}
