package com.craisinlord.antarchy.content.entity.royal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class RoyalMountFollowOwnerGoal extends Goal {
    private final RoyalMountEntity mount;
    private LivingEntity owner;
    private int recalcTimer;
    private int stuckTicks;
    private int teleportCooldown;
    private Vec3 lastCheckPosition = Vec3.ZERO;

    public RoyalMountFollowOwnerGoal(RoyalMountEntity mount) {
        this.mount = mount;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private double startDistance() {
        return 14.0D + 2.0D * this.mount.getGrowthScale();
    }

    private double stopDistance() {
        return 6.0D + 1.5D * this.mount.getGrowthScale();
    }

    private double teleportDistance() {
        return 48.0D + 4.0D * this.mount.getGrowthScale();
    }

    private boolean canFollow(LivingEntity candidate) {
        return candidate != null && candidate.isAlive() && !candidate.isSpectator()
                && candidate.level() == this.mount.level()
                && this.mount.isTame() && !this.mount.isVehicle() && !this.mount.isOrderedToSit()
                && !this.mount.isLeashed() && this.mount.getTarget() == null;
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = this.mount.getOwner();
        if (!this.canFollow(candidate)) {
            return false;
        }
        double start = this.startDistance();
        if (this.mount.distanceToSqr(candidate) < start * start) {
            return false;
        }
        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.canFollow(this.owner) || this.owner != this.mount.getOwner()) {
            return false;
        }
        double stop = this.stopDistance();
        return this.mount.distanceToSqr(this.owner) > stop * stop;
    }

    @Override
    public void start() {
        this.recalcTimer = 0;
        this.stuckTicks = 0;
        this.lastCheckPosition = this.mount.position();
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mount.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.mount.getLookControl().setLookAt(this.owner, 10.0F, this.mount.getMaxHeadXRot());
        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }
        this.trackStuck();

        double distance = this.mount.distanceTo(this.owner);
        if ((distance > this.teleportDistance() || this.stuckTicks >= 100 && distance > 24.0D) && this.teleportCooldown <= 0) {
            this.teleportCooldown = 20;
            if (this.mount.tryTeleportNear(this.owner)) {
                this.stuckTicks = 0;
                return;
            }
        }

        boolean ownerAirborne = RoyalMountEntity.isAirborne(this.owner);
        boolean wantsFlight = this.mount.canFly() && (ownerAirborne || distance > 24.0D + this.mount.getBbWidth()
                || this.stuckTicks >= 40 || this.owner.getY() - this.mount.getY() > 4.0D);
        if (wantsFlight && this.mount.claimFlight()) {
            if (--this.recalcTimer <= 0) {
                this.recalcTimer = 5;
                float scale = this.mount.getGrowthScale();
                Vec3 destination = this.owner.position().add(0.0D, 2.0D + 0.6D * scale, 0.0D);
                Vec3 to = destination.subtract(this.mount.position());
                double stepLength = Math.min(to.length(), 12.0D + 2.0D * scale);
                Vec3 step = to.lengthSqr() > 1.0E-4D ? this.mount.position().add(to.normalize().scale(stepLength)) : destination;
                this.mount.getMoveControl().setWantedPosition(step.x, step.y, step.z, 1.3D);
            }
            return;
        }
        if (this.mount.isFlying()) {
            return;
        }
        if (--this.recalcTimer <= 0) {
            this.recalcTimer = 10;
            this.mount.getNavigation().moveTo(this.owner, 1.2D);
        }
    }

    private void trackStuck() {
        if (this.mount.tickCount % 20 != 0) {
            return;
        }
        Vec3 position = this.mount.position();
        if (position.distanceToSqr(this.lastCheckPosition) < 1.0D) {
            this.stuckTicks += 20;
        } else {
            this.stuckTicks = 0;
        }
        this.lastCheckPosition = position;
    }
}
