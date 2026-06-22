package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class FallenKingCrownLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ModelResourceLocation WORN_MODEL = ModelResourceLocation.inventory(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("antarchy", "fallen_king_crown_worn")
    );
    private final ItemRenderer itemRenderer;

    public FallenKingCrownLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!headStack.is(AntarchyFabricContent.FALLEN_KING_CROWN.get()) || player.isInvisible()) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        this.itemRenderer.render(
                headStack,
                ItemDisplayContext.HEAD,
                false,
                poseStack,
                buffer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                Minecraft.getInstance().getModelManager().getModel(WORN_MODEL)
        );
        poseStack.popPose();
    }
}
