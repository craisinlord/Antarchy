package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.RoyalGuardianSwordAbilities;
import com.craisinlord.antarchy.content.item.RoyalGuardianSwordItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class RoyalGuardianSwordCombatMixin {
    @Inject(method = "hurt", at = @At("RETURN"))
    private void antarchy$releaseGuardianElement(DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || amount <= 0.0F || RoyalGuardianSwordAbilities.isApplyingSecondaryDamage()) return;
        Entity attacker = source.getEntity();
        if (!(attacker instanceof Player player) || source.getDirectEntity() != player
                || !(player.getMainHandItem().getItem() instanceof RoyalGuardianSwordItem)
                || !RoyalGuardianSwordAbilities.isChargedMeleeAttack(player, (LivingEntity) (Object) this)
                || !RoyalGuardianSwordItem.isDischargeReady(player.getMainHandItem(), player.level().getGameTime())) return;
        RoyalGuardianSwordAbilities.discharge(player, (LivingEntity) (Object) this, player.getMainHandItem());
    }
}
