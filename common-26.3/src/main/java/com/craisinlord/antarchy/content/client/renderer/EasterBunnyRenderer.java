package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.EasterBunnyModel;
import com.craisinlord.antarchy.content.entity.EasterBunnyEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class EasterBunnyRenderer extends GeoEntityRenderer<EasterBunnyEntity, LivingEntityRenderState> {
    private static final float SHADOW_RADIUS = 0.3F;

    public EasterBunnyRenderer(EntityRendererProvider.Context context) {
        super(context, new EasterBunnyModel());
        this.shadowRadius = SHADOW_RADIUS;
    }
}
