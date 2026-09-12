package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import com.craisinlord.antarchy.content.item.SpringyBootsItem;
import com.craisinlord.antarchy.fabric.util.SpringyBootsFabricHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SpringyBootsFallDamageMixin {
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void antarchy$cancelBloodCrystalBootsInvertedFallDamage(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        if (!AntarchyGravityApi.isGravityInverted(player)) {
            return;
        }
        if (AntarchyEnchantments.featherRisingLevel(player) <= 0) {
            return;
        }

        player.fallDistance = 0.0F;
        cir.setReturnValue(false);
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float antarchy$reduceSpringyBootsFallDamage(float fallDistance) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!SpringyBootsItem.isWearingSpringyBoots(self)) return fallDistance;
        long protectionUntil = SpringyBootsFabricHelper.getProtectionUntil(self);
        if (self.level().getGameTime() < protectionUntil) {
            return fallDistance * 0.25F;
        }
        return fallDistance;
    }
}
