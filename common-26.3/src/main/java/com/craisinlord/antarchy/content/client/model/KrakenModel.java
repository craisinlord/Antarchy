package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class KrakenModel extends GeoModel<KrakenEntity> {
    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/kraken_emissive.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "kraken");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("antarchy", "textures/entity/kraken.png");
    }

    @Override
    public Identifier getAnimationResource(KrakenEntity animatable) {
        return Identifier.fromNamespaceAndPath("antarchy", "kraken");
    }
}
