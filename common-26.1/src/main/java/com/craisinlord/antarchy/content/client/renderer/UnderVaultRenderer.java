package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.block.entity.UnderVaultBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.client.renderer.blockentity.state.VaultRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class UnderVaultRenderer implements BlockEntityRenderer<UnderVaultBlockEntity, VaultRenderState> {
    private final VaultRenderer delegate;

    public UnderVaultRenderer(BlockEntityRendererProvider.Context context) {
        this.delegate = new VaultRenderer(context);
    }

    @Override
    public VaultRenderState createRenderState() {
        return this.delegate.createRenderState();
    }

    @Override
    public void extractRenderState(UnderVaultBlockEntity blockEntity, VaultRenderState state, float partialTick,
                                   Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        state.displayItem = null;
        this.delegate.extractRenderState(blockEntity.getVaultData(), state, partialTick, cameraPosition, breakProgress);
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
    }

    @Override
    public void submit(VaultRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.delegate.submit(state, poseStack, collector, camera);
    }
}
