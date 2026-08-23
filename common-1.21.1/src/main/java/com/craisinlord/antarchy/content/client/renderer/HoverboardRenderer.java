package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.HoverboardModel;
import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HoverboardRenderer extends GeoEntityRenderer<HoverboardEntity> {
    public HoverboardRenderer(EntityRendererProvider.Context context) {
        super(context, new HoverboardModel());
        this.shadowRadius = 0.5F;
    }
}
