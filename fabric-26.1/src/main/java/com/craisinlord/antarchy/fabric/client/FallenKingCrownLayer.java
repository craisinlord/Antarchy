package com.craisinlord.antarchy.fabric.client;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class FallenKingCrownLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelRenderState crownBlock = new BlockModelRenderState();

    public FallenKingCrownLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (!state.headEquipment.is(AntarchyFabricItems.FALLEN_KING_CROWN.get()) || state.isInvisible) {
            return;
        }

        Minecraft.getInstance().blockModelResolver.update(this.crownBlock, AntarchyFabricBlocks.FALLEN_KING_CROWN_BLOCK.get().defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.5D, -0.125D, -0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.crownBlock.submit(poseStack, submitNodeCollector, lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
        poseStack.popPose();
    }
}
