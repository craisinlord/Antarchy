package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.LucidModel;
import com.craisinlord.antarchy.content.entity.lucid.LucidEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class LucidRenderer extends GeoEntityRenderer<LucidEntity, LivingEntityRenderState> {

    private static final float VISUAL_SCALE = 0.9F;
    private static final double VISUAL_Y_OFFSET = -0.30D;

    public LucidRenderer(EntityRendererProvider.Context context) {
        super(context, new LucidModel());
        this.shadowRadius = 0.5F;
        this.withScale(VISUAL_SCALE);
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        renderPassInfo.poseStack().translate(0.0D, VISUAL_Y_OFFSET, 0.0D);
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0.0F;
    }
}
