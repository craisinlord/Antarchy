package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RollyPollyModel;
import com.craisinlord.antarchy.content.entity.RollyPollyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class RollyPollyRenderer extends GeoEntityRenderer<RollyPollyEntity, LivingEntityRenderState> {
    public RollyPollyRenderer(EntityRendererProvider.Context context) {
        super(context, new RollyPollyModel());
        this.shadowRadius = 0.5F;
    }
}
