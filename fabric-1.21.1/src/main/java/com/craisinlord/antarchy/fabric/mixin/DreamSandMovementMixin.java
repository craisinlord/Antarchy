package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.movement.DreamSandLowGravityAccess;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricBlocks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DreamSandMovementMixin {
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void antarchy$handleDreamSandJump(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;

        DreamSandLowGravityAccess access = (DreamSandLowGravityAccess) self;
        if (!AntarchySettings.dreamSandEnabled() || antarchy$isDreamSandLowGravityBlacklisted(self)) {
            access.antarchy$clearDreamSandLowGravity();
        } else if (antarchy$isStandingOnDreamSand(self)) {
            antarchy$startDreamSandLowGravity(self, access);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickDreamSandLowGravity(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;

        DreamSandLowGravityAccess access = (DreamSandLowGravityAccess) self;
        int landingGraceTicks = access.antarchy$getDreamSandLandingGraceTicks();
        if (landingGraceTicks > 0) {
            access.antarchy$setDreamSandLandingGraceTicks(landingGraceTicks - 1);
        }

        if (!AntarchySettings.dreamSandEnabled() || antarchy$isDreamSandLowGravityBlacklisted(self)) {
            return;
        }

        if (!access.antarchy$isDreamSandLowGravityActive()
                && !self.onGround()
                && antarchy$isStandingOnDreamSand(self)) {
            antarchy$startDreamSandLowGravity(self, access);
        }

        if (!access.antarchy$isDreamSandLowGravityActive()) {
            return;
        }

        int remainingTicks = access.antarchy$getDreamSandLowGravityTicksRemaining();
        if (remainingTicks > 0) {
            access.antarchy$setDreamSandLowGravityTicksRemaining(remainingTicks - 1);
        }
        if (access.antarchy$getDreamSandLowGravityTicksRemaining() <= 0) {
            access.antarchy$clearDreamSandLowGravity();
            return;
        }

        if (antarchy$isOnSolidGround(self)) {
            access.antarchy$setDreamSandLowGravityActive(false);
            access.antarchy$setDreamSandLowGravityTicksRemaining(0);
            access.antarchy$setDreamSandLandingGraceTicks(2);
        } else if (!self.onGround() && self.getDeltaMovement().y < 0.0D) {
            self.setDeltaMovement(
                    self.getDeltaMovement().x,
                    self.getDeltaMovement().y * AntarchySettings.dreamSandGravityMultiplier(),
                    self.getDeltaMovement().z
            );
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void antarchy$handleDreamSandFall(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        DreamSandLowGravityAccess access = (DreamSandLowGravityAccess) self;
        if (!AntarchySettings.dreamSandEnabled()
                || antarchy$isDreamSandLowGravityBlacklisted(self)
                || (!access.antarchy$isDreamSandLowGravityActive() && access.antarchy$getDreamSandLandingGraceTicks() <= 0)) {
            return;
        }

        self.resetFallDistance();
        cir.setReturnValue(false);
    }

    private static boolean antarchy$isOnSolidGround(LivingEntity livingEntity) {
        return livingEntity.onGround() && livingEntity.getBlockStateOn().blocksMotion();
    }

    private static boolean antarchy$isStandingOnDreamSand(LivingEntity livingEntity) {
        return livingEntity.getBlockStateOn().is(AntarchyFabricBlocks.DREAM_SAND.get());
    }

    private static void antarchy$startDreamSandLowGravity(LivingEntity self, DreamSandLowGravityAccess access) {
        self.setDeltaMovement(
                self.getDeltaMovement().x,
                self.getDeltaMovement().y * AntarchySettings.dreamSandJumpVelocityMultiplier(),
                self.getDeltaMovement().z
        );
        access.antarchy$setDreamSandLowGravityActive(true);
        access.antarchy$setDreamSandLowGravityTicksRemaining((int) Math.max(1L, Math.round(AntarchySettings.dreamSandEffectDurationSeconds() * 20.0D)));
        access.antarchy$setDreamSandLandingGraceTicks(0);
    }

    private static boolean antarchy$isDreamSandLowGravityBlacklisted(LivingEntity livingEntity) {
        return livingEntity.getType().is(AntarchyTags.Entities.DREAM_SAND_LOW_GRAVITY_BLACKLIST);
    }
}
