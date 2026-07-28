package com.craisinlord.antarchy.fabric.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ElytraStartFlyingMixin {

    @Shadow
    public abstract void startFallFlying();

    @Inject(method = "tryToStartFallFlying()Z", at = @At("RETURN"), cancellable = true)
    private void antarchy$allowCustomElytra(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        Player player = (Player) (Object) this;
        if (!player.onGround() && !player.isFallFlying() && !player.isInWater()
                && !player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof ElytraItem && ElytraItem.isFlyEnabled(chest)) {
                this.startFallFlying();
                cir.setReturnValue(true);
            }
        }
    }
}
