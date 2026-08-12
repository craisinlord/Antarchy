package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.JumpyBugEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class JumpyBugModel extends GeoModel<JumpyBugEntity> {
    public static final ResourceLocation EMISSIVE_TEXTURE =
            new ResourceLocation(Antarchy.MODID, "textures/entity/jumpy_bug_emissive.png");

    @Override
    public ResourceLocation getModelResource(JumpyBugEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "geo/jumpy_bug.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JumpyBugEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "textures/entity/jumpy_bug.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JumpyBugEntity animatable) {
        return new ResourceLocation(Antarchy.MODID, "animations/jumpy_bug.animation.json");
    }
}
