package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.lucid.LucidBoltEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class LucidBoltModel extends GeoModel<LucidBoltEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "lucid_bolt");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/lucid_bolt.png");
    }

    @Override
    public Identifier getAnimationResource(LucidBoltEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "lucid_bolt");
    }
}
