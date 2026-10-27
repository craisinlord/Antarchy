package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ManticoreModel;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class ManticoreRenderer extends GeoEntityRenderer<ManticoreEntity, LivingEntityRenderState> {
    public ManticoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ManticoreModel());
        this.shadowRadius = 0.9F;
    }
}
