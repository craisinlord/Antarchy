package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.OuranwoodDeerModel;
import com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class OuranwoodDeerRenderer extends GeoEntityRenderer<OuranwoodDeerEntity, LivingEntityRenderState> {
    private static final float SHADOW_RADIUS = 0.5F;

    public OuranwoodDeerRenderer(EntityRendererProvider.Context context) {
        super(context, new OuranwoodDeerModel());
        this.shadowRadius = SHADOW_RADIUS;
    }
}
