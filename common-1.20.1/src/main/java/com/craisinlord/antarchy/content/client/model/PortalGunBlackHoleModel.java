package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunBlackHoleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PortalGunBlackHoleModel extends GeoModel<PortalGunBlackHoleEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Antarchy.MODID, "textures/vfx/portal_gun_black_hole.png");

    @Override
    public ResourceLocation getModelResource(PortalGunBlackHoleEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/portal_gun_portal.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PortalGunBlackHoleEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PortalGunBlackHoleEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/portal_gun_portal.animation.json");
    }
}
