package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.GlimmerEchoRendering;
import com.craisinlord.antarchy.content.entity.WaterBombEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WaterBombModel extends GeoModel<WaterBombEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/water_bomb.png");
    private static final ResourceLocation GLIMMER_TEXTURE = GlimmerEchoRendering.texture("water_bomb");

    @Override
    public ResourceLocation getModelResource(WaterBombEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/water_bomb.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WaterBombEntity animatable) {
        return GlimmerEchoRendering.textureLocation(animatable, GLIMMER_TEXTURE, TEXTURE);
    }

    @Override
    public ResourceLocation getAnimationResource(WaterBombEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/water_bomb.animation.json");
    }
}
