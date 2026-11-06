package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.VortexModel;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class VortexRenderer extends GeoEntityRenderer<VortexEntity, LivingEntityRenderState> {
    public VortexRenderer(EntityRendererProvider.Context context) {
        super(context, new VortexModel());
        this.shadowRadius = 0.5F;
        this.withScale(0.9F);
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }
}
