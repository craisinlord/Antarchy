package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.JudgmentMarkAccess;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public final class RoyalIndicatorLivingLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public RoyalIndicatorLivingLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch) {
        RoyalIndicatorRenderer.render(poseStack, buffers, entity,
                RoyalEffectHooks.commandedHolder() != null && entity.hasEffect(RoyalEffectHooks.commandedHolder()),
                entity instanceof JudgmentMarkAccess access && access.antarchy$isJudgmentMarked(), ageInTicks);
    }
}
