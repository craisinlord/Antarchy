package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.EyeOfTheStormItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.PrimordialArmorItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void antarchy$blockGravityGunMelee(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack mainHand = player.getMainHandItem();
        if (AntarchySettings.gravityGunEnabled() && mainHand.getItem() instanceof GravityGunItem) {
            ci.cancel();
            return;
        }
        if (AntarchySettings.eyeOfTheStormEnabled() && mainHand.getItem() instanceof EyeOfTheStormItem) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V"))
    private void antarchy$ignorePrimordialKnockbackResistance(LivingEntity target, double strength, double x, double z, DamageSource source, float damage, boolean comesFromEffect, Operation<Void> original) {
        Player player = (Player) (Object) this;
        if (!PrimordialArmorItem.hasFullSet(player)) {
            original.call(target, strength, x, z, source, damage, comesFromEffect);
            return;
        }

        antarchy$applyUnresistedKnockback(target, strength, x, z);
    }

    @WrapOperation(method = "doSweepAttack*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void antarchy$ignorePrimordialSweepKnockbackResistance(LivingEntity target, double strength, double x, double z, DamageSource source, float damage, Operation<Void> original) {
        Player player = (Player) (Object) this;
        if (!PrimordialArmorItem.hasFullSet(player)) {
            original.call(target, strength, x, z, source, damage);
            return;
        }

        antarchy$applyUnresistedKnockback(target, strength, x, z);
    }

    @Unique
    private static void antarchy$applyUnresistedKnockback(LivingEntity target, double strength, double x, double z) {
        if (strength <= 0.0D) {
            return;
        }

        target.needsSync = true;
        while (x * x + z * z < 1.0E-5D) {
            x = (Math.random() - Math.random()) * 0.01D;
            z = (Math.random() - Math.random()) * 0.01D;
        }

        Vec3 previousMovement = target.getDeltaMovement();
        Vec3 knockback = new Vec3(x, 0.0D, z).normalize().scale(strength);
        target.setDeltaMovement(
                previousMovement.x / 2.0D - knockback.x,
                target.onGround() ? Math.min(0.4D, previousMovement.y / 2.0D + strength) : previousMovement.y,
                previousMovement.z / 2.0D - knockback.z
        );
    }
}
