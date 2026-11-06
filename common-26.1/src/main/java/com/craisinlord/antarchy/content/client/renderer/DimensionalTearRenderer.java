package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.DimensionalTearModel;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class DimensionalTearRenderer extends GeoEntityRenderer<DimensionalTearEntity, EntityRenderState> {
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucent(DimensionalTearModel.TEXTURE);

    public DimensionalTearRenderer(EntityRendererProvider.Context context) {
        super(context, new DimensionalTearModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    protected boolean affectedByCulling(DimensionalTearEntity entity) {
        return false;
    }

    @Override
    protected int getBlockLightLevel(DimensionalTearEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(DimensionalTearEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public @Nullable RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return RENDER_TYPE;
    }
}
