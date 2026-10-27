package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MolevoreModel;
import com.craisinlord.antarchy.content.entity.MolevoreEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MolevoreRenderer extends GeoEntityRenderer<MolevoreEntity, LivingEntityRenderState> {
    public MolevoreRenderer(EntityRendererProvider.Context context) {
        super(context, new MolevoreModel());
        this.shadowRadius = 2.0F;
    }
}
