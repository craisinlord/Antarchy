package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.royal.RoyalBoltEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RoyalBoltRenderer extends EntityRenderer<RoyalBoltEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/particle/generic_0.png");

    public RoyalBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RoyalBoltEntity entity) {
        return TEXTURE;
    }
}
