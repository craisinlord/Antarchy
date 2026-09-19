package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.ChronosphereManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityChronosphereEquipmentMixin {
    @Inject(method = "onEquipItem", at = @At("TAIL"))
    private void antarchy$refreshChronosphere(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo callback) {
        if (slot == EquipmentSlot.CHEST && (Object) this instanceof ServerPlayer player) {
            ChronosphereManager.refresh(player);
        }
    }
}
