package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public final class SeashellModel extends GeoModel<SeashellBlockEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "seashell");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/block/seashell.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "seashell");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(SeashellBlockEntity animatable) {
        return ANIMATION;
    }
}
