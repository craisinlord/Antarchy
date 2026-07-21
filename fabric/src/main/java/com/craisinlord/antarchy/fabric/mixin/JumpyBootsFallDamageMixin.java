package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.item.BloodCrystalArmorItem;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.fabric.util.JumpyBootsFabricHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class JumpyBootsFallDamageMixin {
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void antarchy$cancelBloodCrystalBootsInvertedFallDamage(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        if (!AntarchyGravityApi.isGravityInverted(player)) {
            return;
        }
        if (!(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof BloodCrystalArmorItem)) {
            return;
        }

        player.fallDistance = 0.0F;
        cir.setReturnValue(false);
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float antarchy$reduceJumpyBootsFallDamage(float fallDistance) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!JumpyBootsItem.isWearingJumpyBoots(self)) return fallDistance;
        long protectionUntil = JumpyBootsFabricHelper.getProtectionUntil(self);
        if (self.level().getGameTime() < protectionUntil) {
            return fallDistance * 0.25F;
        }
        return fallDistance;
    }
}
