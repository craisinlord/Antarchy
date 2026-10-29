package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.craisinlord.antarchy.content.entity.royal.RoyalElementalProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalElementalProjectileModel extends GeoModel<RoyalElementalProjectileEntity> {
    private static final ResourceLocation GLIMMER_FIRE_BALL = GlimmerEchoRendering.texture("king_fire_ball");
    private static final ResourceLocation GLIMMER_ICE_BALL = GlimmerEchoRendering.texture("king_ice_ball");
    private static final ResourceLocation GLIMMER_DREAM_FIRE_BALL = GlimmerEchoRendering.texture("queen_dream_fire_ball");

    @Override
    public ResourceLocation getModelResource(RoyalElementalProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID,
                "geo/" + (animatable.isIceball() ? "ice_ball" : "fire_ball") + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalElementalProjectileEntity animatable) {
        if (GlimmerEchoes.isGlimmerEcho(animatable)) {
            return animatable.isDreamFireball() ? GLIMMER_DREAM_FIRE_BALL
                    : animatable.isIceball() ? GLIMMER_ICE_BALL : GLIMMER_FIRE_BALL;
        }
        if (animatable.isDreamFireball()) {
            return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/queen/dream_fire_ball.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID,
                "textures/entity/king/" + (animatable.isIceball() ? "ice_ball" : "fire_ball") + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalElementalProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID,
                "animations/" + (animatable.isIceball() ? "ice_ball" : "fire_ball") + ".animation.json");
    }
}
