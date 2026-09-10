package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobAttackTimeDilationMixin {
    @Unique
    private long antarchy$nextAttackTick = Long.MIN_VALUE;

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void antarchy$rateLimitAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Mob mob = (Mob) (Object) this;
        if (mob.level().isClientSide) {
            return;
        }
        double rate = TimeDilationApi.getRate(mob);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return;
        }
        long now = mob.level().getGameTime();
        if (now < this.antarchy$nextAttackTick) {
            cir.setReturnValue(false);
            return;
        }
        this.antarchy$nextAttackTick = now + TimeDilationApi.scaleCooldownTicks(mob, 20);
    }
}
