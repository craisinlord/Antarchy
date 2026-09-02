package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.block.DreamSandBlock;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.movement.DreamSandLowGravityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class DreamSandLowGravityMixin implements DreamSandLowGravityAccess {
    @Unique
    private boolean antarchy$dreamSandLowGravityActive;
    @Unique
    private int antarchy$dreamSandLowGravityTicksRemaining;
    @Unique
    private int antarchy$dreamSandLandingGraceTicks;

    @Override
    public boolean antarchy$isDreamSandLowGravityActive() {
        return this.antarchy$dreamSandLowGravityActive;
    }

    @Override
    public void antarchy$setDreamSandLowGravityActive(boolean active) {
        this.antarchy$dreamSandLowGravityActive = active;
    }

    @Override
    public int antarchy$getDreamSandLowGravityTicksRemaining() {
        return this.antarchy$dreamSandLowGravityTicksRemaining;
    }

    @Override
    public void antarchy$setDreamSandLowGravityTicksRemaining(int ticks) {
        this.antarchy$dreamSandLowGravityTicksRemaining = Math.max(0, ticks);
    }

    @Override
    public int antarchy$getDreamSandLandingGraceTicks() {
        return this.antarchy$dreamSandLandingGraceTicks;
    }

    @Override
    public void antarchy$setDreamSandLandingGraceTicks(int ticks) {
        this.antarchy$dreamSandLandingGraceTicks = Math.max(0, ticks);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$updateDreamSandLowGravity(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!AntarchySettings.dreamSandEnabled()) {
            this.antarchy$clearDreamSandLowGravity();
            return;
        }

        boolean supportedByDreamSand = this.antarchy$isSupportedByDreamSand(self);
        int durationTicks = Math.max(1, Mth.ceil(AntarchySettings.dreamSandEffectDurationSeconds() * 20.0D));
        if (supportedByDreamSand) {
            this.antarchy$dreamSandLowGravityActive = true;
            this.antarchy$dreamSandLowGravityTicksRemaining = durationTicks;
            this.antarchy$dreamSandLandingGraceTicks = 5;
        } else if (this.antarchy$dreamSandLowGravityTicksRemaining > 0) {
            this.antarchy$dreamSandLowGravityTicksRemaining--;
            this.antarchy$dreamSandLowGravityActive = true;
        } else {
            this.antarchy$dreamSandLowGravityActive = false;
        }

        if (this.antarchy$dreamSandLandingGraceTicks > 0 && !supportedByDreamSand) {
            this.antarchy$dreamSandLandingGraceTicks--;
        }

        if (!this.antarchy$dreamSandLowGravityActive || self.onGround() || self.onClimbable() || self.isInWaterOrBubble() || self.isInLava()) {
            return;
        }

        Vec3 motion = self.getDeltaMovement();
        boolean movingWithGravity = motion.y < 0.0D;
        if (!movingWithGravity) {
            return;
        }

        double gravityMultiplier = Math.max(0.0D, Math.min(1.0D, AntarchySettings.dreamSandGravityMultiplier()));
        self.setDeltaMovement(motion.x, motion.y * gravityMultiplier, motion.z);
        self.fallDistance = 0.0F;
        this.antarchy$dreamSandLandingGraceTicks = 5;
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void antarchy$boostDreamSandJump(CallbackInfo ci) {
        if (!this.antarchy$dreamSandLowGravityActive) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        Vec3 motion = self.getDeltaMovement();
        boolean movingAwayFromGravity = motion.y > 0.0D;
        if (!movingAwayFromGravity) {
            return;
        }

        self.setDeltaMovement(motion.x, motion.y * AntarchySettings.dreamSandJumpVelocityMultiplier(), motion.z);
        self.fallDistance = 0.0F;
        this.antarchy$dreamSandLandingGraceTicks = 5;
    }

    @Unique
    private boolean antarchy$isSupportedByDreamSand(LivingEntity entity) {
        AntarchyGravityDirection gravityDirection = AntarchyGravityApi.getGravityDirection(entity);
        AABB box = entity.getBoundingBox();
        BlockPos supportPos = gravityDirection.isInverted()
                ? BlockPos.containing(entity.getX(), box.maxY + 0.05D, entity.getZ())
                : BlockPos.containing(entity.getX(), box.minY - 0.05D, entity.getZ());
        return entity.level().getBlockState(supportPos).getBlock() instanceof DreamSandBlock;
    }
}
