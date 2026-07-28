package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.fabric.BloodglassManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class BloodglassEquipMixin {

    @Inject(method = "onEquipItem", at = @At("TAIL"))
    private void antarchy$onBloodglassEquip(EquipmentSlot slot, ItemStack from, ItemStack to, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player) {
            BloodglassManager.handleArmorEquipChange(player, slot, from, to);
        }
    }
}
