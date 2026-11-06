package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RoyalIceSpikeModel extends GeoModel<RoyalIceSpikeEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/king/ice_spike.png");
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("king_ice_spike");

    @Override
    public ResourceLocation getModelResource(RoyalIceSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/ice_spike.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoyalIceSpikeEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, TEXTURE);
    }

    @Override
    public ResourceLocation getAnimationResource(RoyalIceSpikeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/ice_spike.animation.json");
    }
}
