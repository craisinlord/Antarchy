package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class FallenKingCrownLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final BlockRenderDispatcher blockRenderer;

    public FallenKingCrownLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
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
        poseStack.translate(0.5D, -0.125D, -0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.blockRenderer.renderSingleBlock(
                AntarchyFabricContent.FALLEN_KING_CROWN_BLOCK.get().defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F)
        );
        poseStack.popPose();
    }
}
