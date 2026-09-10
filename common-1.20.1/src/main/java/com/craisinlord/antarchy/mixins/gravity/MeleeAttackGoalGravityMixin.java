package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalGravityMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    @Final
    private double speedModifier;

    @Shadow
    @Final
    private boolean followingTargetEvenIfNotSeen;

    @Shadow
    private int ticksUntilNextAttack;

    @Shadow
    protected abstract void checkAndPerformAttack(LivingEntity enemy);

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void antarchy$useInvertedMelee(CallbackInfoReturnable<Boolean> cir) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        LivingEntity target = this.mob.getTarget();
        cir.setReturnValue(this.antarchy$isValidInvertedMeleeTarget(target));
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void antarchy$continueInvertedMelee(CallbackInfoReturnable<Boolean> cir) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        LivingEntity target = this.mob.getTarget();
        cir.setReturnValue(this.antarchy$isValidInvertedMeleeTarget(target));
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void antarchy$startInvertedMelee(CallbackInfo ci) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        LivingEntity target = this.mob.getTarget();
        this.mob.getNavigation().stop();
        this.mob.setAggressive(true);
        this.ticksUntilNextAttack = 0;
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void antarchy$tickInvertedMelee(CallbackInfo ci) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        LivingEntity target = this.mob.getTarget();
        if (!this.antarchy$isValidInvertedMeleeTarget(target)) {
            this.mob.getNavigation().stop();
            ci.cancel();
            return;
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        // Do not call PathNavigation.moveTo here: that starts the unsafe vanilla
        // path acquisition path. MoveControl receives a world-space target and
        // the gravity movement mixin converts it into the inverted frame.
        this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), this.speedModifier);
        this.ticksUntilNextAttack = Math.max(0, this.ticksUntilNextAttack - 1);
        this.checkAndPerformAttack(target);
        ci.cancel();
    }

    private boolean antarchy$isValidInvertedMeleeTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (this.followingTargetEvenIfNotSeen && !this.mob.isWithinRestriction(target.blockPosition())) {
            return false;
        }
        return !(target instanceof Player player) || (!player.isSpectator() && !player.isCreative());
    }
}
