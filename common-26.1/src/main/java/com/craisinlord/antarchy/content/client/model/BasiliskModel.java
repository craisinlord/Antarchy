package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.basilisk.BasiliskEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class BasiliskModel extends GeoModel<BasiliskEntity> {
    public static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/basilisk_emissive.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "basilisk");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/basilisk.png");
    }

    @Override
    public Identifier getAnimationResource(BasiliskEntity animatable) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "basilisk");
    }
}
