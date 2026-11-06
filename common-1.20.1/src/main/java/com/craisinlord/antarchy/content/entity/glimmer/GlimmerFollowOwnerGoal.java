package com.craisinlord.antarchy.content.entity.glimmer;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GlimmerFollowOwnerGoal extends Goal {
    private static final double MIN_STOP_DISTANCE = 3.0D;
    private static final double STOP_BEYOND_WIDTH = 2.0D;
    private static final double START_BEYOND_STOP = 5.0D;
    private static final double TELEPORT_DISTANCE = 20.0D;
    private static final double OWNER_MOVED_SQR = 1.0D;
    private static final int REPATH_SUCCESS_TICKS = 6;
    private static final int REPATH_FAILURE_TICKS = 10;
    private static final int TELEPORT_RETRY_TICKS = 10;

    private final GlimmerEntity glimmer;
    private final double speedModifier;
    private LivingEntity owner;
    private Vec3 lastOwnerPos;
    private int repathTicks;
    private int teleportRetryTicks;
    private boolean drifting;

    public GlimmerFollowOwnerGoal(GlimmerEntity glimmer, double speedModifier) {
        this.glimmer = glimmer;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private double stopDistance() {
        return Math.max(MIN_STOP_DISTANCE, STOP_BEYOND_WIDTH + this.glimmer.getBbWidth());
    }

    private double startDistance() {
        return this.stopDistance() + START_BEYOND_STOP;
    }

    @Override
    public boolean canUse() {
        if (!this.glimmer.isTame() || this.glimmer.isOrderedToSit() || !this.glimmer.isFollowingOwner()) {
            return false;
        }

        LivingEntity owner = this.glimmer.getOwner();
        if (owner == null || owner.isSpectator()) {
            return false;
        }

        double start = this.startDistance();
        if (this.glimmer.distanceToSqr(owner) < start * start) {
            return false;
        }

        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null || !this.owner.isAlive() || this.owner.isSpectator() || this.owner.level() != this.glimmer.level()) {
            return false;
        }
        if (!this.glimmer.isTame() || this.glimmer.isOrderedToSit() || !this.glimmer.isFollowingOwner()) {
            return false;
        }
        double stop = this.stopDistance();
        return this.glimmer.distanceToSqr(this.owner) > stop * stop;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        this.teleportRetryTicks = 0;
        this.lastOwnerPos = null;
        this.drifting = false;
        this.glimmer.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.owner = null;
        this.lastOwnerPos = null;
        this.drifting = false;
        this.glimmer.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        this.glimmer.getLookControl().setLookAt(this.owner, 10.0F, this.glimmer.getMaxHeadXRot());

        if (this.glimmer.distanceToSqr(this.owner) > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            this.tickTeleport();
            return;
        }

        if (--this.repathTicks <= 0) {
            this.repath();
        }
        if (this.drifting) {
            this.glimmer.getMoveControl().setWantedPosition(this.owner.getX(), this.owner.getY(), this.owner.getZ(), this.speedModifier);
        }
    }

    private void tickTeleport() {
        if (--this.teleportRetryTicks > 0) {
            return;
        }
        this.teleportRetryTicks = TELEPORT_RETRY_TICKS;

        this.glimmer.tryToTeleportToOwner();
        if (this.glimmer.distanceToSqr(this.owner) > TELEPORT_DISTANCE * TELEPORT_DISTANCE && this.owner.onGround()) {
            this.glimmer.teleportTo(this.owner.getX(), this.owner.getY(), this.owner.getZ());
        }
        this.repathTicks = 0;
        this.lastOwnerPos = null;
    }

    private void repath() {
        boolean ownerMoved = this.lastOwnerPos == null || this.owner.position().distanceToSqr(this.lastOwnerPos) > OWNER_MOVED_SQR;
        if (!ownerMoved && !this.drifting && !this.glimmer.getNavigation().isDone()) {
            this.repathTicks = REPATH_SUCCESS_TICKS;
            return;
        }

        this.lastOwnerPos = this.owner.position();
        Path path = this.glimmer.getNavigation().createPath(this.owner, 1);
        if (path != null && path.canReach() && this.glimmer.getNavigation().moveTo(path, this.speedModifier)) {
            this.drifting = false;
            this.repathTicks = REPATH_SUCCESS_TICKS;
            return;
        }

        this.glimmer.getNavigation().stop();
        this.drifting = true;
        this.repathTicks = REPATH_FAILURE_TICKS;
    }
}
