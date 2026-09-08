package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomStrollGoal.class)
public abstract class RandomStrollGoalGravityMixin {
    @Unique
    private static final int ANTARCHY$INVERTED_STROLL_ATTEMPTS = 12;
    @Unique
    private static final int ANTARCHY$INVERTED_STROLL_HORIZONTAL_RANGE = 7;
    @Unique
    private static final int ANTARCHY$INVERTED_STROLL_VERTICAL_RANGE = 2;

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    protected double wantedX;

    @Shadow
    protected double wantedY;

    @Shadow
    protected double wantedZ;

    @Shadow
    @Final
    protected double speedModifier;

    @Shadow
    protected int interval;

    @Shadow
    protected boolean forceTrigger;

    @Shadow
    @Final
    private boolean checkNoActionTime;

    @Unique
    private int antarchy$invertedStrollTicks;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void antarchy$useInvertedRandomStroll(CallbackInfoReturnable<Boolean> cir) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        if (this.mob.hasControllingPassenger()) {
            cir.setReturnValue(false);
            return;
        }

        if (!this.forceTrigger) {
            if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
                cir.setReturnValue(false);
                return;
            }
            int chance = Math.max(1, this.interval);
            if (this.mob.getRandom().nextInt(chance) != 0) {
                cir.setReturnValue(false);
                return;
            }
        }

        cir.setReturnValue(this.antarchy$findInvertedStrollTarget());
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void antarchy$startInvertedRandomStroll(CallbackInfo ci) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        this.mob.getNavigation().stop();
        this.mob.getMoveControl().setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        this.antarchy$invertedStrollTicks = 0;
        this.forceTrigger = false;
        ci.cancel();
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void antarchy$continueInvertedRandomStroll(CallbackInfoReturnable<Boolean> cir) {
        if (!AntarchyGravityApi.isGravityInverted(this.mob)) {
            return;
        }

        double dx = this.wantedX - this.mob.getX();
        double dy = this.wantedY - this.mob.getY();
        double dz = this.wantedZ - this.mob.getZ();
        boolean arrived = dx * dx + dy * dy + dz * dz <= 1.0D;
        boolean timedOut = this.antarchy$invertedStrollTicks++ >= 200;
        if (!arrived && !timedOut && !this.mob.hasControllingPassenger()) {
            // RandomStrollGoal inherits Goal.tick(), so continuation is the
            // declared per-tick hook available for refreshing direct movement.
            this.mob.getMoveControl().setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }
        cir.setReturnValue(!this.mob.hasControllingPassenger() && !arrived && !timedOut);
    }

    @Unique
    private boolean antarchy$findInvertedStrollTarget() {
        int baseX = Mth.floor(this.mob.getX());
        int baseY = Mth.floor(this.mob.getY() + this.mob.getBbHeight()) - 1;
        int baseZ = Mth.floor(this.mob.getZ());

        for (int attempt = 0; attempt < ANTARCHY$INVERTED_STROLL_ATTEMPTS; attempt++) {
            int x = baseX + this.mob.getRandom().nextInt(ANTARCHY$INVERTED_STROLL_HORIZONTAL_RANGE * 2 + 1) - ANTARCHY$INVERTED_STROLL_HORIZONTAL_RANGE;
            int y = baseY + this.mob.getRandom().nextInt(ANTARCHY$INVERTED_STROLL_VERTICAL_RANGE * 2 + 1) - ANTARCHY$INVERTED_STROLL_VERTICAL_RANGE;
            int z = baseZ + this.mob.getRandom().nextInt(ANTARCHY$INVERTED_STROLL_HORIZONTAL_RANGE * 2 + 1) - ANTARCHY$INVERTED_STROLL_HORIZONTAL_RANGE;
            if (this.antarchy$isValidInvertedStrollTarget(x, y, z)) {
                this.wantedX = x + 0.5D;
                this.wantedY = y + 1.0D - 1.0E-3D;
                this.wantedZ = z + 0.5D;
                return true;
            }
        }

        return false;
    }

    @Unique
    private boolean antarchy$isValidInvertedStrollTarget(int x, int y, int z) {
        Level level = this.mob.level();
        BlockPos ceilingPos = new BlockPos(x, y + 1, z);
        if (!level.getBlockState(ceilingPos).isFaceSturdy(level, ceilingPos, Direction.DOWN)) {
            return false;
        }

        EntityDimensions dimensions = this.mob.getDimensions(Pose.STANDING);
        double halfWidth = dimensions.width() / 2.0D;
        double topY = y + 1.0D - 1.0E-3D;
        AABB bodyBox = new AABB(
                x + 0.5D - halfWidth,
                topY - dimensions.height(),
                z + 0.5D - halfWidth,
                x + 0.5D + halfWidth,
                topY,
                z + 0.5D + halfWidth
        ).deflate(1.0E-4D);
        return level.noCollision(this.mob, bodyBox);
    }
}
