package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class MolevoreModel extends GeoModel<MolevoreEntity> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "molevore");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/molevore.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "molevore");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(MolevoreEntity animatable) {
        return ANIMATION;
    }
}
