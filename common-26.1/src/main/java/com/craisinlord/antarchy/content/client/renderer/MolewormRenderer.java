package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.MolewormModel;
import com.craisinlord.antarchy.content.entity.MolewormEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class MolewormRenderer extends GeoEntityRenderer<MolewormEntity, LivingEntityRenderState> {
    public MolewormRenderer(EntityRendererProvider.Context context) {
        super(context, new MolewormModel());
        this.shadowRadius = 0.25F;
    }
}
