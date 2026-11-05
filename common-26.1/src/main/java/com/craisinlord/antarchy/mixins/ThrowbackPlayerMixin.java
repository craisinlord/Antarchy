package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ThrowbackPlayerMixin {
    @ModifyReturnValue(method = "getAttackStrengthScale", at = @At("RETURN"))
    private float antarchy$throwbackNoAttackCooldown(float scale) {
        if (AntarchySettings.throwbackNoAttackCooldown() && this.antarchy$hasRemembrance()) {
            return 1.0F;
        }
        return scale;
    }

    @ModifyReturnValue(method = "getHurtSound", at = @At("RETURN"))
    private SoundEvent antarchy$throwbackClassicHurtSound(SoundEvent sound, DamageSource source) {
        if (AntarchySettings.throwbackClassicHurtSound() && this.antarchy$hasRemembrance()) {
            return AntarchySoundEvents.PLAYER_HURT_CLASSIC.get();
        }
        return sound;
    }

    @ModifyReturnValue(method = "canEat", at = @At("RETURN"))
    private boolean antarchy$throwbackEatToHeal(boolean canEat, boolean canAlwaysEat) {
        if (canEat || !this.antarchy$classicFood()) {
            return canEat;
        }
        Player player = (Player) (Object) this;
        return player.getHealth() < player.getMaxHealth();
    }

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void antarchy$throwbackNoExhaustion(float exhaustion, CallbackInfo ci) {
        if (this.antarchy$classicFood()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean antarchy$classicFood() {
        return AntarchySettings.throwbackClassicFood() && this.antarchy$hasRemembrance();
    }

    @Unique
    private boolean antarchy$hasRemembrance() {
        return ((Player) (Object) this).hasEffect(AntarchyObjects.REMEMBRANCE_EFFECT.get());
    }
}
