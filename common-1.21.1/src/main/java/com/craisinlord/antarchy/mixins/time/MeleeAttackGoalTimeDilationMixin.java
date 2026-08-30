package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalTimeDilationMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    private int ticksUntilNextAttack;

    @Inject(method = "resetAttackCooldown", at = @At("RETURN"))
    private void antarchy$scaleMeleeCooldown(CallbackInfo ci) {
        this.ticksUntilNextAttack = TimeDilationApi.scaleCooldownTicks(this.mob, this.ticksUntilNextAttack);
    }
}
