package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.item.RpoLauncherItem;
import com.craisinlord.antarchy.content.item.SizeRayItem;
import com.craisinlord.antarchy.content.item.SquidzookaItem;
import com.craisinlord.antarchy.content.item.WaterCannonItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class CrossbowArmPoseMixin {

    @Inject(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
            at = @At("RETURN"), cancellable = true)
    private static void antarchy$customArmPose(
            Avatar player, ItemStack stack, InteractionHand hand,
            CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        Item item = stack.getItem();

        if (item instanceof GravityGunItem || item instanceof PortalGunItem || item instanceof SquidzookaItem || item instanceof RpoLauncherItem || item instanceof WaterCannonItem) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        } else if (item instanceof SizeRayItem) {
            if (player.isUsingItem() && player.getUsedItemHand() == hand) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            }
        }
    }
}
