package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.AlphaMantisModel;
import com.craisinlord.antarchy.content.entity.AlphaMantisEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class AlphaMantisRenderer extends GeoEntityRenderer<AlphaMantisEntity, LivingEntityRenderState> {
    public AlphaMantisRenderer(EntityRendererProvider.Context context) {
        super(context, new AlphaMantisModel());
        this.shadowRadius = 1.5F;
        this.withScale(1.875F);
    }
}
