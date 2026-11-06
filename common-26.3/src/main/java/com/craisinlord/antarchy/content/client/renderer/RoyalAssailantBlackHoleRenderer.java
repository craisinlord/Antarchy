package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.RoyalBlackHoleModel;
import com.craisinlord.antarchy.content.entity.royal.RoyalAssailantBlackHoleEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class RoyalAssailantBlackHoleRenderer extends GeoEntityRenderer<RoyalAssailantBlackHoleEntity, EntityRenderState> {
    public RoyalAssailantBlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context, new RoyalBlackHoleModel<>());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void extractRenderState(RoyalAssailantBlackHoleEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.lightCoords = 0xF000F0;
    }

    @Override
    public RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(this.getTextureLocation(renderState));
    }

    @Override
    protected boolean affectedByCulling(RoyalAssailantBlackHoleEntity entity) {
        return false;
    }
}
