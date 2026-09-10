package com.craisinlord.antarchy.content.entity.royal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class RoyalMountFollowOwnerGoal extends Goal {
    private static final double START_DISTANCE = 12.0D;
    private static final double STOP_DISTANCE = 6.0D;
    private static final double TELEPORT_DISTANCE = 48.0D;

    private final RoyalMountEntity mount;
    private LivingEntity owner;
    private int recalcTimer;

    public RoyalMountFollowOwnerGoal(RoyalMountEntity mount) {
        this.mount = mount;
    }

    @Override
    public boolean canUse() {
        if (!this.mount.isTame() || this.mount.isVehicle() || this.mount.isOrderedToSit()) {
            return false;
        }
        LivingEntity potentialOwner = this.mount.getOwner();
        if (potentialOwner == null || potentialOwner.isSpectator()) {
            return false;
        }
        if (this.mount.distanceToSqr(potentialOwner) < START_DISTANCE * START_DISTANCE) {
            return false;
        }
        this.owner = potentialOwner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mount.getNavigation().isDone()
                && this.mount.distanceToSqr(this.owner) > STOP_DISTANCE * STOP_DISTANCE
                && !this.mount.isOrderedToSit();
    }

    @Override
    public void start() {
        this.recalcTimer = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mount.getNavigation().stop();
        this.mount.setFlying(false);
    }

    @Override
    public void tick() {
        this.mount.getLookControl().setLookAt(this.owner, 10.0F, this.mount.getMaxHeadXRot());

        double distanceSqr = this.mount.distanceToSqr(this.owner);
        if (distanceSqr > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            this.mount.teleportTo(this.owner.getX(), this.owner.getY(), this.owner.getZ());
            return;
        }

        boolean ownerAirborne = !this.owner.onGround() || (this.owner.getY() - this.mount.getY()) > 3.0D;
        this.mount.setFlying(ownerAirborne || distanceSqr > 24.0D * 24.0D);

        if (--this.recalcTimer <= 0) {
            this.recalcTimer = 10;
            if (this.mount.isFlying()) {
                Vec3 to = this.owner.position().subtract(this.mount.position());
                Vec3 step = this.mount.position().add(to.normalize().scale(Math.min(to.length(), 8.0D)));
                this.mount.getMoveControl().setWantedPosition(step.x, step.y + 1.0D, step.z, 1.2D);
            } else {
                this.mount.getNavigation().moveTo(this.owner, 1.1D);
            }
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
