package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
import com.craisinlord.antarchy.content.entity.royal.RoyalBlackHoleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalBlackHoleModel<T extends RoyalBlackHoleEntity> extends GeoModel<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/entity/queen/black_hole.png");
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("queen_black_hole");

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/black_hole.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, TEXTURE);
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/black_hole.animation.json");
    }
}
