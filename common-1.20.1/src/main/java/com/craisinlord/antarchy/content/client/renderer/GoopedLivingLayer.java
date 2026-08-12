package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.effect.GoopedEffectHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public final class GoopedLivingLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final int GOOPED_TINT = 0x6632FF55;

    public GoopedLivingLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        Holder<MobEffect> gooped = GoopedEffectHooks.holder();
        if (gooped == null || !entity.hasEffect(gooped.value())) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(1.018F, 1.018F, 1.018F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                0.196F,
                1.0F,
                0.333F,
                0.4F
        );
        poseStack.popPose();
    }
}
