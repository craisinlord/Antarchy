package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.royal.RoyalBoltEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class RoyalBoltRenderer extends EntityRenderer<RoyalBoltEntity, EntityRenderState> {
    public RoyalBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
