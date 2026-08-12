package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public final class GlimmeringLivingLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    // High-alpha, luminance-preserving tint drawn straight over the skin (no scale-up) so it
    // reads as the body itself recolored spirit-blue rather than a separate ghost shell.
    private static final float GLIMMERING_TINT_R = 0.4F;
    private static final float GLIMMERING_TINT_G = 0.8F;
    private static final float GLIMMERING_TINT_B = 1.0F;
    private static final float GLIMMERING_TINT_A = 0.2588F;

    public GlimmeringLivingLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        MobEffect glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (glimmering == null || !entity.hasEffect(glimmering)) {
            return;
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(this.getTextureLocation(entity)));
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                LightTexture.FULL_BRIGHT,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                GLIMMERING_TINT_R,
                GLIMMERING_TINT_G,
                GLIMMERING_TINT_B,
                GLIMMERING_TINT_A
        );
    }
}
