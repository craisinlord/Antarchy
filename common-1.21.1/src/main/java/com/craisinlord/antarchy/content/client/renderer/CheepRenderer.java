package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.CheepModel;
import com.craisinlord.antarchy.content.entity.CheepEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CheepRenderer extends GeoEntityRenderer<CheepEntity> {
    public CheepRenderer(EntityRendererProvider.Context context) {
        super(context, new CheepModel());
    }
}
