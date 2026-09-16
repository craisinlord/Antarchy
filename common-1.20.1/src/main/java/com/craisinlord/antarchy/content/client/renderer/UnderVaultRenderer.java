package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UnderVaultBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Compact 1.20 vault display: a rotating key replaces the vanilla vault core. */
public final class UnderVaultRenderer implements BlockEntityRenderer<UnderVaultBlockEntity> {
    public UnderVaultRenderer() {
    }

    public UnderVaultRenderer(BlockEntityRendererProvider.Context ignored) {
    }

    @Override
    public void render(UnderVaultBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.55D, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((blockEntity.getLevel().getGameTime() + partialTick) * 2.0F));
        poseStack.scale(0.65F, 0.65F, 0.65F);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(Items.TRIPWIRE_HOOK),
                ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer,
                blockEntity.getLevel(), 0);
        poseStack.popPose();
    }
}
