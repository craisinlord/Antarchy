package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedAttackGoal.class)
public abstract class RangedAttackGoalTimeDilationMixin {
    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowRangedGoalTick(CallbackInfo ci) {
        if (!TimeDilationApi.consumeTick(this.mob, "ranged_goal_tick")) {
            ci.cancel();
        }
    }
}
