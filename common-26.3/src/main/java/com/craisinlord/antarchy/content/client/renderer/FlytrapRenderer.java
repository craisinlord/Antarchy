package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.FlytrapModel;
import com.craisinlord.antarchy.content.entity.FlytrapEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class FlytrapRenderer extends GeoEntityRenderer<FlytrapEntity, LivingEntityRenderState> {
    public FlytrapRenderer(EntityRendererProvider.Context context) {
        super(context, new FlytrapModel());
        this.shadowRadius = 1.4F;
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }
}
