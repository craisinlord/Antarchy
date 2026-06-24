package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.SizeRayItem;
import com.craisinlord.antarchy.content.item.SquidzookaItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class CrossbowArmPoseMixin {

    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true)
    private static void antarchy$customArmPose(
            AbstractClientPlayer player, InteractionHand hand,
            CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();

        if (item instanceof GravityGunItem || item instanceof SquidzookaItem || item instanceof WaterCannonItem) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        } else if (item instanceof SizeRayItem) {
            if (player.isUsingItem() && player.getUsedItemHand() == hand) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            }
        }
    }
}
