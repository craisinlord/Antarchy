package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UnderVaultBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.VaultRenderer;

public final class UnderVaultRenderer implements BlockEntityRenderer<UnderVaultBlockEntity> {
    private final VaultRenderer delegate;

    public UnderVaultRenderer(BlockEntityRendererProvider.Context context) {
        this.delegate = new VaultRenderer(context);
    }

    @Override
    public void render(UnderVaultBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        this.delegate.render(blockEntity.getVaultData(), partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
