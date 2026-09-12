package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.stratoshark.StratosharkEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StratosharkModel extends GeoModel<StratosharkEntity> {
    public static final ResourceLocation BASE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/stratoshark/stratoshark.png");
    public static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/stratoshark/stratoshark_emissive.png");

    @Override
    public ResourceLocation getModelResource(StratosharkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/stratoshark.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StratosharkEntity animatable) {
        return BASE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(StratosharkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/stratoshark.animation.json");
    }
}
