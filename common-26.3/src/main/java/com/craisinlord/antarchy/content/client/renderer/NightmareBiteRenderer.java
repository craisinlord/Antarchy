package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.NightmareBiteModel;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareBiteEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class NightmareBiteRenderer extends GeoEntityRenderer<NightmareBiteEntity, EntityRenderState> {
    public NightmareBiteRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareBiteModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected boolean affectedByCulling(NightmareBiteEntity entity) {
        return false;
    }
}
