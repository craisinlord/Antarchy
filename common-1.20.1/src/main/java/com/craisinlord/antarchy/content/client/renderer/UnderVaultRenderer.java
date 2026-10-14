package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UnderVaultBlockEntity;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultClientData;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultData;
import com.craisinlord.antarchy.content.block.entity.undervault.UnderVaultLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class UnderVaultRenderer implements BlockEntityRenderer<UnderVaultBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public UnderVaultRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(UnderVaultBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        UnderVaultData vaultData = blockEntity.getVaultData();
        if (!UnderVaultLogic.Client.shouldDisplayActiveEffects(vaultData.getSharedData())) {
            return;
        }
        Level level = vaultData.getLevel();
        if (level == null) {
            return;
        }
        ItemStack stack = vaultData.getSharedData().getDisplayItem();
        if (stack.isEmpty()) {
            return;
        }
        this.random.setSeed(seedForItemStack(stack));
        UnderVaultClientData clientData = vaultData.getClientData();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.4F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, clientData.previousSpin(), clientData.currentSpin())));
        renderMultipleFromCount(this.itemRenderer, poseStack, bufferSource, packedLight, stack, this.random, level);
        poseStack.popPose();
    }

    private static int seedForItemStack(ItemStack stack) {
        return stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue();
    }

    private static int renderedAmount(int count) {
        if (count <= 1) {
            return 1;
        }
        if (count <= 16) {
            return 2;
        }
        if (count <= 32) {
            return 3;
        }
        return count <= 48 ? 4 : 5;
    }

    private static void renderMultipleFromCount(ItemRenderer itemRenderer, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                                ItemStack stack, RandomSource random, Level level) {
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        boolean gui3d = model.isGui3d();
        int amount = renderedAmount(stack.getCount());
        float scaleX = model.getTransforms().ground.scale.x();
        float scaleY = model.getTransforms().ground.scale.y();
        float scaleZ = model.getTransforms().ground.scale.z();
        if (!gui3d) {
            float offsetX = -0.0F * (amount - 1) * 0.5F * scaleX;
            float offsetY = -0.0F * (amount - 1) * 0.5F * scaleY;
            float offsetZ = -0.09375F * (amount - 1) * 0.5F * scaleZ;
            poseStack.translate(offsetX, offsetY, offsetZ);
        }
        for (int i = 0; i < amount; i++) {
            poseStack.pushPose();
            if (i > 0) {
                if (gui3d) {
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float z = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    poseStack.translate(x, y, z);
                } else {
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    poseStack.translate(x, y, 0.0F);
                }
            }
            itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, model);
            poseStack.popPose();
            if (!gui3d) {
                poseStack.translate(0.0F * scaleX, 0.0F * scaleY, 0.09375F * scaleZ);
            }
        }
    }
}
