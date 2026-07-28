package com.craisinlord.antarchy.fabric.client;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

public final class FallenKingCrownArmorStandLayer extends RenderLayer<ArmorStand, ArmorStandModel> {
    private final BlockRenderDispatcher blockRenderer;

    public FallenKingCrownArmorStandLayer(RenderLayerParent<ArmorStand, ArmorStandModel> renderer) {
        super(renderer);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ArmorStand armorStand, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack headStack = armorStand.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        if (!headStack.is(AntarchyFabricItems.FALLEN_KING_CROWN.get()) || armorStand.isInvisible()) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.5D, -0.125D, -0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.blockRenderer.renderSingleBlock(
                AntarchyFabricBlocks.FALLEN_KING_CROWN_BLOCK.get().defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(armorStand, 0.0F)
        );
        poseStack.popPose();
    }
}
