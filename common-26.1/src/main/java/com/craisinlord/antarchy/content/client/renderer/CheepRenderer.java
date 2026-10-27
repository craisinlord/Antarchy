package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CheepModel;
import com.craisinlord.antarchy.content.entity.CheepEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.geckolib.renderer.GeoEntityRenderer;

public class CheepRenderer extends GeoEntityRenderer<CheepEntity, LivingEntityRenderState> {
    public CheepRenderer(EntityRendererProvider.Context context) {
        super(context, new CheepModel());
    }
}
