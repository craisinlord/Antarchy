package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.model.TermiteModel;
import com.craisinlord.antarchy.content.entity.ant.TermiteEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class TermiteRenderer extends GeoEntityRenderer<TermiteEntity, LivingEntityRenderState> {
    private static final float ADULT_SHADOW_RADIUS = 0.3F;
    private static final float ADULT_SCALE = 0.85F;
    private static final float BABY_SCALE = 0.5F;
    private static final float LEGACY_BABY_SHADOW_SCALE = 0.5F;

    public TermiteRenderer(EntityRendererProvider.Context context) {
        super(context, new TermiteModel());
        this.shadowRadius = ADULT_SHADOW_RADIUS;
    }

    @Override
    protected float getShadowRadius(LivingEntityRenderState state) {
        return state.isBaby ? ADULT_SHADOW_RADIUS * BABY_SCALE * LEGACY_BABY_SHADOW_SCALE : ADULT_SHADOW_RADIUS;
    }

    @Override
    public void preRenderPass(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        PoseStack poseStack = renderPassInfo.poseStack();
        if (renderPassInfo.renderState().isBaby) {
            poseStack.scale(ADULT_SCALE * BABY_SCALE, ADULT_SCALE * BABY_SCALE, ADULT_SCALE * BABY_SCALE);
        } else {
            poseStack.scale(ADULT_SCALE, ADULT_SCALE, ADULT_SCALE);
        }
    }
}
