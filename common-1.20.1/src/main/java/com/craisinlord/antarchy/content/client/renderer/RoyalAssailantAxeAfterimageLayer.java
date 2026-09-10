package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.item.RoyalAssailantBattleAxeItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class RoyalAssailantAxeAfterimageLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final int[] TINTS = {0x66FF3B4D, 0x4DFF263D, 0x33E8142F, 0x1FCC0D26};

    public RoyalAssailantAxeAfterimageLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (player.isSpectator() || player.isInvisible() || player.getAttackAnim(partialTick) <= 0.01F) return;
        ItemStack stack = player.getMainHandItem();
        HumanoidArm arm = player.getMainArm();
        if (!(stack.getItem() instanceof RoyalAssailantBattleAxeItem)) {
            stack = player.getOffhandItem();
            arm = player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        }
        if (!(stack.getItem() instanceof RoyalAssailantBattleAxeItem)) return;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(player)));
        float swing = player.getAttackAnim(partialTick);
        for (int i = TINTS.length - 1; i >= 0; i--) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, (i + 1) * 0.08F);
            poseStack.mulPose(Axis.YP.rotation((i + 1) * 0.12F * (swing < 0.5F ? -1.0F : 1.0F)));
            int tint = TINTS[i];
            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT,
                    LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                    ((tint >> 16) & 0xFF) / 255.0F, ((tint >> 8) & 0xFF) / 255.0F,
                    (tint & 0xFF) / 255.0F, ((tint >>> 24) & 0xFF) / 255.0F);
            poseStack.popPose();
        }
        for (int i = TINTS.length - 1; i >= 0; i--) {
            poseStack.pushPose();
            this.getParentModel().translateToHand(arm, poseStack);
            poseStack.mulPose(Axis.YP.rotation((i + 1) * 0.12F * (swing < 0.5F ? -1.0F : 1.0F)));
            Minecraft.getInstance().getItemRenderer().renderStatic(player, stack,
                    arm == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    arm == HumanoidArm.LEFT, poseStack, buffer, player.level(), packedLight,
                    LivingEntityRenderer.getOverlayCoords(player, 0.0F), player.getId());
            poseStack.popPose();
        }
    }
}
