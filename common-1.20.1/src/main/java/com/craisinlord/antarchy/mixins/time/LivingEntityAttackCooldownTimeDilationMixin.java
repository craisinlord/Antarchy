package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAttackCooldownTimeDilationMixin {
    @Shadow
    private int attackStrengthTicker;

    @Inject(method = "tick", at = @At("RETURN"))
    private void antarchy$slowAttackCooldown(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!TimeDilationApi.consumeTick(entity, "living_attack_cooldown")) {
            this.attackStrengthTicker = Math.max(0, this.attackStrengthTicker - 1);
        }
    }
}
