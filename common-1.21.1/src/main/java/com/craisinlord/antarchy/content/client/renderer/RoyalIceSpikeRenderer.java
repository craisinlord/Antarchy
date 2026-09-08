package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalIceSpikeModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalIceSpikeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RoyalIceSpikeRenderer extends GeoEntityRenderer<RoyalIceSpikeEntity> {
    public RoyalIceSpikeRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalIceSpikeModel());
        this.shadowRadius = 0.0F;
    }
}
