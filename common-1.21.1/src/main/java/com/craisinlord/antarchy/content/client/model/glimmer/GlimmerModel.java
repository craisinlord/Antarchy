package com.craisinlord.antarchy.content.client.model.glimmer;

import com.craisinlord.antarchy.content.entity.glimmer.GlimmerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GlimmerModel extends GeoModel<GlimmerEntity> {
    @Override
    public ResourceLocation getModelResource(GlimmerEntity animatable) {
        return animatable.getVariant().getBehavior().modelGeo();
    }

    @Override
    public ResourceLocation getAnimationResource(GlimmerEntity animatable) {
        return animatable.getVariant().getBehavior().animationFile();
    }

    @Override
    public ResourceLocation getTextureResource(GlimmerEntity animatable) {
        return animatable.getVariant().getBehavior().texture(animatable);
    }
}
