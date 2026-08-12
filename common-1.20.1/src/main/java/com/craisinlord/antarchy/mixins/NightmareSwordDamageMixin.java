package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.NightmareSwordItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class NightmareSwordDamageMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float antarchy$applyNightmareSwordDamage(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide) {
            return amount;
        }

        if (!(source.getEntity() instanceof Player attacker)) {
            return amount;
        }

        if (!(attacker.getMainHandItem().getItem() instanceof NightmareSwordItem)) {
            return amount;
        }

        return NightmareSwordItem.calculateDamage(attacker);
    }
}
