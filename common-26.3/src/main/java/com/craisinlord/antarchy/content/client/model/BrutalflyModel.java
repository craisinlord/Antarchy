package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.brutalfly.BrutalflyEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class BrutalflyModel extends GeoModel<BrutalflyEntity> {
    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/brutalfly_emissive.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "brutalfly");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/brutalfly.png");
    }

    @Override
    public Identifier getAnimationResource(BrutalflyEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "brutalfly");
    }
}
