package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightmareModel extends GeoModel<NightmareEntity> {
    private static final ResourceLocation PHASE_ONE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare.png");
    private static final ResourceLocation PHASE_TWO_TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2.png");
    private static final ResourceLocation PHASE_ONE_EMISSIVE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_emissive.png");
    private static final ResourceLocation PHASE_TWO_EMISSIVE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/nightmare/nightmare_phase_2_emissive.png");

    @Override
    public ResourceLocation getModelResource(NightmareEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/nightmare.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NightmareEntity animatable) {
        return textureFor(animatable);
    }

    @Override
    public ResourceLocation getAnimationResource(NightmareEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/nightmare.animation.json");
    }

    public static ResourceLocation textureFor(NightmareEntity animatable) {
        return animatable.hasPhaseTwoTexture() ? PHASE_TWO_TEXTURE : PHASE_ONE_TEXTURE;
    }

    public static ResourceLocation emissiveTextureFor(NightmareEntity animatable) {
        return animatable.hasPhaseTwoTexture() ? PHASE_TWO_EMISSIVE : PHASE_ONE_EMISSIVE;
    }
}
