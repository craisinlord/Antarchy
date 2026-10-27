package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.OctopusBombModel;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class OctopusBombRenderer extends GeoEntityRenderer<OctopusBombEntity, LivingEntityRenderState> {
    public OctopusBombRenderer(EntityRendererProvider.Context context) {
        super(context, new OctopusBombModel());
        this.shadowRadius = 1.2F;
    }
}
