package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareBiteEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightmareBiteModel extends GeoModel<NightmareBiteEntity> {
    private static final ResourceLocation PHASE_ONE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/nightmare_bite.png");
    private static final ResourceLocation PHASE_TWO_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/nightmare_bite_phase_2.png");

    @Override
    public ResourceLocation getModelResource(NightmareBiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/nightmare_bite.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NightmareBiteEntity animatable) {
        return animatable.isPhaseTwo() ? PHASE_TWO_TEXTURE : PHASE_ONE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NightmareBiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/nightmare_bite.animation.json");
    }
}
