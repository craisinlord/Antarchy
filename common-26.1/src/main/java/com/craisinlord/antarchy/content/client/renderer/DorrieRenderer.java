package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.DorrieModel;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class DorrieRenderer extends GeoEntityRenderer<DorrieEntity, LivingEntityRenderState> {
    public DorrieRenderer(EntityRendererProvider.Context context) {
        super(context, new DorrieModel());
    }
}
