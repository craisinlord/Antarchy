package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.PrimordialArmorItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
/*
 * Fixes player attack checks under inverted gravity.
 */
public abstract class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void antarchy$blockGravityGunMelee(Entity target, CallbackInfo ci) {
        if (!AntarchySettings.gravityGunEnabled()) {
            return;
        }

        Player player = (Player) (Object) this;
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GravityGunItem) {
            ci.cancel();
        }
    }

    /*
     * Primordial's full set bonus makes player melee knockback ignore the target's knockback resistance.
     */
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void antarchy$ignorePrimordialKnockbackResistance(LivingEntity target, double strength, double x, double z, Operation<Void> original) {
        Player player = (Player) (Object) this;
        if (!PrimordialArmorItem.hasFullSet(player)) {
            original.call(target, strength, x, z);
            return;
        }

        if (strength <= 0.0D) {
            return;
        }

        target.hasImpulse = true;
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
