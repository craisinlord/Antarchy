package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.VortexModel;
import com.craisinlord.antarchy.content.entity.vortex.VortexEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class VortexRenderer extends GeoEntityRenderer<VortexEntity> {
    public VortexRenderer(EntityRendererProvider.Context context) {
        super(context, new VortexModel());
        this.shadowRadius = 0.5F;
        this.withScale(0.9F);
    }

    @Override
    protected float getDeathMaxRotation(VortexEntity animatable) {
        return 0.0F;
    }
}
