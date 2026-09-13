package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ComputerModel extends GeoModel<ComputerBlockEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "geo/computer.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/block/computer/computer.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "animations/computer.animation.json");

    @Override
    public ResourceLocation getModelResource(ComputerBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ComputerBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ComputerBlockEntity animatable) {
        return ANIMATION;
    }
}
