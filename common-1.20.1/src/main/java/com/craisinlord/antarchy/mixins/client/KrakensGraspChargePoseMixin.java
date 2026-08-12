package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.item.KrakensGraspItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class KrakensGraspChargePoseMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void antarchy$fixKrakensGraspChargePose(
            LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, HumanoidArm arm,
            PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (isChargingKrakensGraspWithArm(entity, arm)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(162.0F));
            poseStack.translate(0.0D, -1.0D, 0.0D);
        }
    }

    private static boolean isChargingKrakensGraspWithArm(LivingEntity entity, HumanoidArm arm) {
        if (!entity.isUsingItem() || !(entity.getUseItem().getItem() instanceof KrakensGraspItem)) {
            return false;
        }

        InteractionHand usedHand = entity.getUsedItemHand();
        boolean usedHandIsMain = usedHand == InteractionHand.MAIN_HAND;
        HumanoidArm usedArm = usedHandIsMain == (entity.getMainArm() == HumanoidArm.RIGHT) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        return usedArm == arm;
    }
}
