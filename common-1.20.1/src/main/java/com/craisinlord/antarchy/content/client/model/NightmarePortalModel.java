package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmarePortalEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightmarePortalModel extends GeoModel<NightmarePortalEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/vfx/nightmare_portal.png");

    @Override
    public ResourceLocation getModelResource(NightmarePortalEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/nightmare_portal.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NightmarePortalEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NightmarePortalEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/nightmare_portal.animation.json");
    }
}
