package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.TentacleModel;
import com.craisinlord.antarchy.content.entity.kraken.TentacleEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class TentacleRenderer extends GeoEntityRenderer<TentacleEntity, LivingEntityRenderState> {
    public TentacleRenderer(EntityRendererProvider.Context context) {
        super(context, new TentacleModel());
        this.shadowRadius = 0.0F;
    }
}
