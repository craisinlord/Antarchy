package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.ToreterrorModel;
import com.craisinlord.antarchy.content.entity.ToreterrorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ToreterrorRenderer extends GeoEntityRenderer<ToreterrorEntity> {

    public ToreterrorRenderer(EntityRendererProvider.Context context) {
        super(context, new ToreterrorModel());
        this.shadowRadius = 1.6F;
    }

    @Override
    protected float getDeathMaxRotation(ToreterrorEntity animatable) {
        return 0.0F;
    }
}
