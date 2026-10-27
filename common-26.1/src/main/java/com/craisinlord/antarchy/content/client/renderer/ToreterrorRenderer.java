package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ToreterrorModel;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class ToreterrorRenderer extends GeoEntityRenderer<ToreterrorEntity, LivingEntityRenderState> {
    public ToreterrorRenderer(EntityRendererProvider.Context context) {
        super(context, new ToreterrorModel());
        this.shadowRadius = 1.6F;
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }
}
