package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.LurkingTerrorModel;
import com.craisinlord.antarchy.content.entity.LurkingTerrorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LurkingTerrorRenderer extends GeoEntityRenderer<LurkingTerrorEntity> {
    public LurkingTerrorRenderer(EntityRendererProvider.Context context) {
        super(context, new LurkingTerrorModel());
        this.shadowRadius = 0.5F;
    }
}
