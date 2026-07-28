package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class ParalyzedStoneLivingLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation STONE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");
    private static final int STONE_TINT = 0xFFB1ADA4;

    public ParalyzedStoneLivingLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!entity.hasEffect(AntarchyObjects.PARALYZED_EFFECT.get())) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(1.02f, 1.02f, 1.02f);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(STONE_TEXTURE));
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                STONE_TINT
        );
        poseStack.popPose();
    }
}
