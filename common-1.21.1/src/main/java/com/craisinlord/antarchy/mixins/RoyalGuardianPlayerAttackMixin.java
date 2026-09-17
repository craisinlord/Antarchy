package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.RoyalGuardianSwordAbilities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures attack charge before vanilla resets the player's attack-strength ticker. */
@Mixin(Player.class)
public abstract class RoyalGuardianPlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void antarchy$beginGuardianAttack(Entity target, CallbackInfo ci) {
        RoyalGuardianSwordAbilities.beginMeleeAttack((Player) (Object) this, target);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void antarchy$endGuardianAttack(Entity target, CallbackInfo ci) {
        RoyalGuardianSwordAbilities.endMeleeAttack((Player) (Object) this);
    }
}
