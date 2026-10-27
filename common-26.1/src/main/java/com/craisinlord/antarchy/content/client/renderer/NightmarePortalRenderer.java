package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.NightmarePortalModel;
import com.craisinlord.antarchy.content.entity.nightmare.NightmarePortalEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class NightmarePortalRenderer extends GeoEntityRenderer<NightmarePortalEntity, EntityRenderState> {
    public NightmarePortalRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmarePortalModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected int getBlockLightLevel(NightmarePortalEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(NightmarePortalEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityCutout(NightmarePortalModel.TEXTURE);
    }

    @Override
    protected boolean affectedByCulling(NightmarePortalEntity entity) {
        return false;
    }
}
