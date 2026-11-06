package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.item.KrakensGraspItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class KrakensGraspChargePoseMixin {

    @Inject(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void antarchy$fixKrakensGraspChargePose(
            ArmedEntityRenderState state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm,
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (isChargingKrakensGraspWithArm(state, arm)) {
            poseStack.rotate(Axis.XP.rotationDegrees(162.0F));
            poseStack.translate(0.0D, -1.0D, 0.0D);
        }
    }

    private static boolean isChargingKrakensGraspWithArm(ArmedEntityRenderState state, HumanoidArm arm) {
        if (!(state instanceof HumanoidRenderState humanoid) || !humanoid.isUsingItem) {
            return false;
        }

        boolean usedHandIsMain = humanoid.useItemHand == InteractionHand.MAIN_HAND;
        HumanoidArm usedArm = usedHandIsMain == (humanoid.mainArm == HumanoidArm.RIGHT) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        return usedArm == arm && humanoid.getUseItemStackForArm(usedArm).getItem() instanceof KrakensGraspItem;
    }
}
