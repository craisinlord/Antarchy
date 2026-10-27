package com.craisinlord.antarchy.fabric.client;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;

public final class FallenKingCrownArmorStandLayer extends RenderLayer<ArmorStandRenderState, ArmorStandArmorModel> {
    private final BlockModelRenderState crownBlock = new BlockModelRenderState();
    private final BlockModelResolver blockModelResolver;

    public FallenKingCrownArmorStandLayer(RenderLayerParent<ArmorStandRenderState, ArmorStandArmorModel> renderer, BlockModelResolver blockModelResolver) {
        super(renderer);
        this.blockModelResolver = blockModelResolver;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ArmorStandRenderState state, float yRot, float xRot) {
        if (!state.headEquipment.is(AntarchyFabricItems.FALLEN_KING_CROWN.get()) || state.isInvisible) {
            return;
        }

        this.blockModelResolver.update(this.crownBlock, AntarchyFabricBlocks.FALLEN_KING_CROWN_BLOCK.get().defaultBlockState(), FallenKingCrownLayer.BLOCK_DISPLAY_CONTEXT);
        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.5D, -0.125D, -0.5D);
        poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F));
        this.crownBlock.submit(poseStack, submitNodeCollector, lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
        poseStack.popPose();
    }
}
