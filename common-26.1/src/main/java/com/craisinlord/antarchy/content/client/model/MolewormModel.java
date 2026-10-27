package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class MolewormModel extends GeoModel<MolewormEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "moleworm");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/moleworm.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "moleworm");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(MolewormEntity animatable) {
        return ANIMATION;
    }
}
