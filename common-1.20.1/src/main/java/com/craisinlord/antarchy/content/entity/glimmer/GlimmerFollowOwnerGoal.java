package com.craisinlord.antarchy.content.entity.glimmer;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class GlimmerFollowOwnerGoal extends Goal {
    private static final float START_DISTANCE = 8.0F;
    private static final float STOP_DISTANCE = 3.0F;
    private static final float TELEPORT_DISTANCE = 20.0F;

    private final GlimmerEntity glimmer;
    private final double speedModifier;
    private LivingEntity owner;

    public GlimmerFollowOwnerGoal(GlimmerEntity glimmer, double speedModifier) {
        this.glimmer = glimmer;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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

        if (this.glimmer.distanceToSqr(owner) < START_DISTANCE * START_DISTANCE) {
            return false;
        }

        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null || !this.glimmer.isTame() || this.glimmer.isOrderedToSit() || !this.glimmer.isFollowingOwner()) {
            return false;
        }
        return this.glimmer.distanceToSqr(this.owner) > STOP_DISTANCE * STOP_DISTANCE;
    }

    @Override
    public void start() {
        this.glimmer.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.owner = null;
        this.glimmer.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        this.glimmer.getLookControl().setLookAt(this.owner, 10.0F, this.glimmer.getMaxHeadXRot());

        double distanceSq = this.glimmer.distanceToSqr(this.owner);
        if (distanceSq > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
            this.glimmer.teleportTo(this.owner.getX(), this.owner.getY(), this.owner.getZ());
            return;
        }

        this.glimmer.getNavigation().moveTo(this.owner, this.speedModifier);
    }
}
