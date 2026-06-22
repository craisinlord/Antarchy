package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AntCarriedFoodLayer<T extends BaseAntEntity> extends RenderLayer<T, SilverfishModel<T>> {
    private final ItemRenderer itemRenderer;

    public AntCarriedFoodLayer(RenderLayerParent<T, SilverfishModel<T>> renderer, ItemRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T ant,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        ItemStack carriedFood = ant.getCarriedFood();
        if (carriedFood.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.08F, -0.06F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.22F, 0.22F, 0.22F);
        this.itemRenderer.renderStatic(
                ant,
                carriedFood,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                buffer,
                ant.level(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ant.getId()
        );
        poseStack.popPose();
    }
}
