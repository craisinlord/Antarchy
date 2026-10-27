package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeBlocks;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;

public final class FallenKingCrownArmorStandLayer extends RenderLayer<ArmorStandRenderState, ArmorStandArmorModel> {
    public FallenKingCrownArmorStandLayer(RenderLayerParent<ArmorStandRenderState, ArmorStandArmorModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, ArmorStandRenderState state, float yRot, float xRot) {
        if (!state.headEquipment.is(AntarchyNeoforgeItems.FALLEN_KING_CROWN.get()) || state.isInvisible) {
            return;
        }

        BlockModelRenderState crown = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(crown, AntarchyNeoforgeBlocks.FALLEN_KING_CROWN.get().defaultBlockState(), FallenKingCrownLayer.CROWN_DISPLAY_CONTEXT);
        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.5D, -0.125D, -0.5D);
        poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F));
        crown.submit(poseStack, submitNodeCollector, packedLight, LivingEntityRenderer.getOverlayCoords(state, 0.0F), 0);
        poseStack.popPose();
    }
}
