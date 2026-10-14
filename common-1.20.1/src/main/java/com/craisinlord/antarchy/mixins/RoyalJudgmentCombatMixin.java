package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.effect.JudgmentMarkManager;
import com.craisinlord.antarchy.content.effect.CommandedEntityAccess;
import com.craisinlord.antarchy.content.item.RoyalGuardianArmorItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class RoyalJudgmentCombatMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float antarchy$boostMarkedTargetDamage(float amount, DamageSource source) {
        if (com.craisinlord.antarchy.content.item.RoyalGuardianSwordAbilities.isApplyingSecondaryDamage()) return amount;
        LivingEntity target = (LivingEntity) (Object) this;
        if (target instanceof CommandedEntityAccess access && access.antarchy$isRoyalInvested()) {
            double reduction = Math.max(0.0D, Math.min(1.0D, AntarchySettings.royalGuardianMusterDamageReduction()));
            amount *= (float) (1.0D - reduction);
        }
        Player attacker = antarchy$resolvePlayer(source);
        if (attacker != null && amount > 0.0F && JudgmentMarkManager.isMarkedBy(attacker, target)) {
            return amount * (float) RoyalGuardianArmorItem.judgmentDamageMultiplier(attacker);
        }
        return amount;
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void antarchy$markAfterHit(DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || amount <= 0.0F
                || com.craisinlord.antarchy.content.item.RoyalGuardianSwordAbilities.isApplyingSecondaryDamage()) {
            return;
        }
        LivingEntity target = (LivingEntity) (Object) this;
        Player attacker = antarchy$resolvePlayer(source);
        if (attacker != null) {
            JudgmentMarkManager.recordSuccessfulHit(attacker, target);
        }
    }

    private static Player antarchy$resolvePlayer(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player player) {
            return player;
        }
        if (source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof Player player) {
            return player;
        }
        return null;
    }
}
