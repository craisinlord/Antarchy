package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PortalGunPortalModel extends GeoModel<PortalGunPortalEntity> {
    private static final ResourceLocation BLUE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_blue.png");
    private static final ResourceLocation ORANGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/vfx/portal_gun_portal_orange.png");

    @Override
    public ResourceLocation getModelResource(PortalGunPortalEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/portal_gun_portal.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PortalGunPortalEntity animatable) {
        return animatable.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE ? BLUE_TEXTURE : ORANGE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PortalGunPortalEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/portal_gun_portal.animation.json");
    }
}
