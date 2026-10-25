package com.craisinlord.antarchy.content.entity.royal;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class RoyalMountCompanionGoal extends Goal {
    private static final double MAX_OWNER_DISTANCE = 40.0D;

    private final RoyalMountEntity mount;
    private LivingEntity owner;
    private int side = 1;
    private float orbitAngle;
    private int repathTicks;
    private int settledTicks;
    private int cooldown;
    private Vec3 spot;

    public RoyalMountCompanionGoal(RoyalMountEntity mount) {
        this.mount = mount;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private boolean isEligible(LivingEntity candidate) {
        return candidate != null && candidate.isAlive() && !candidate.isSpectator()
                && candidate.level() == this.mount.level()
                && this.mount.isTame() && !this.mount.isVehicle() && !this.mount.isOrderedToSit()
                && !this.mount.isLeashed() && this.mount.getTarget() == null && !this.mount.isActing()
                && this.mount.distanceToSqr(candidate) < MAX_OWNER_DISTANCE * MAX_OWNER_DISTANCE;
    }

    private double settleDistance() {
        return 4.0D + this.mount.getBbWidth() * 0.6D;
    }

    private boolean needsToReposition(LivingEntity candidate) {
        if (this.mount.canFly() && RoyalMountEntity.isAirborne(candidate)) {
            return true;
        }
        if (this.mount.isFlying()) {
            return true;
        }
        double settle = this.settleDistance() + 2.0D;
        return this.mount.distanceToSqr(candidate) > settle * settle;
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        LivingEntity candidate = this.mount.getOwner();
        if (!this.isEligible(candidate) || !this.needsToReposition(candidate)) {
            return false;
        }
        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.isEligible(this.owner) && this.owner == this.mount.getOwner() && this.settledTicks < 40;
    }

    @Override
    public void start() {
        this.side = this.mount.getRandom().nextBoolean() ? 1 : -1;
        this.repathTicks = 0;
        this.settledTicks = 0;
        this.spot = null;
        Vec3 offset = this.mount.position().subtract(this.owner.position());
        this.orbitAngle = (float) Mth.atan2(offset.z, offset.x);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.spot = null;
        this.cooldown = 20;
        this.mount.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        float scale = this.mount.getGrowthScale();
        if (this.mount.canFly() && RoyalMountEntity.isAirborne(this.owner)) {
            this.settledTicks = 0;
            this.spot = null;
            this.mount.claimFlight();
            this.circleOwner(scale);
            return;
        }

        if (--this.repathTicks <= 0 || this.spot == null) {
            this.repathTicks = 20;
            this.spot = this.pickSpotBesideOwner();
        }

        if (this.mount.isFlying()) {
            this.settledTicks = 0;
            this.mount.getLookControl().setLookAt(this.owner, 10.0F, this.mount.getMaxHeadXRot());
            if (this.mount.onGround() || this.mount.isInWater()) {
                this.mount.setFlying(false);
                return;
            }
            this.mount.claimFlight();
            Vec3 target = this.spot != null ? this.spot : this.owner.position();
            double horizontal = Math.sqrt(Mth.square(target.x - this.mount.getX()) + Mth.square(target.z - this.mount.getZ()));
            double y = horizontal > 3.0D + scale ? Math.max(target.y + 1.5D * scale, this.mount.getY() - 2.0D) : target.y - 0.5D;
            this.mount.getMoveControl().setWantedPosition(target.x, y, target.z, 1.0D);
            return;
        }

        double settle = this.settleDistance();
        boolean nearOwner = this.mount.distanceToSqr(this.owner) <= settle * settle;
        boolean nearSpot = this.spot != null && this.mount.position().distanceToSqr(this.spot) <= Mth.square(1.5D + 0.5D * scale);
        if (nearOwner || nearSpot) {
            this.mount.getNavigation().stop();
            if (this.settledTicks == 0) {
                this.mount.turnTowards(this.mount.position().add(this.owner.getLookAngle().multiply(1.0D, 0.0D, 1.0D)), 30.0F);
                if (this.mount.getRandom().nextInt(4) == 0) {
                    this.mount.triggerQuirk();
                }
            }
            this.mount.getLookControl().setLookAt(this.owner, 10.0F, this.mount.getMaxHeadXRot());
            this.settledTicks++;
            return;
        }
        this.settledTicks = 0;
        this.mount.getLookControl().setLookAt(this.owner, 10.0F, this.mount.getMaxHeadXRot());
        if (this.repathTicks == 20) {
            Vec3 destination = this.spot != null ? this.spot : this.owner.position();
            if (!this.mount.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.0D)) {
                this.mount.getNavigation().moveTo(this.owner, 1.0D);
            }
        }
    }

    private void circleOwner(float scale) {
        double radius = 5.0D + this.mount.getBbWidth() * 0.75D;
        this.orbitAngle += this.side * (float) Math.min(0.07D, 1.0D / radius);
        double x = this.owner.getX() + Math.cos(this.orbitAngle) * radius;
        double z = this.owner.getZ() + Math.sin(this.orbitAngle) * radius;
        double y = this.owner.getY() + 2.0D + 0.6D * scale;
        if (!this.mount.canOccupy(x, y, z)) {
            y += 2.0D + scale;
        }
        this.mount.getMoveControl().setWantedPosition(x, y, z, 1.2D);
        this.mount.getLookControl().setLookAt(this.owner, 10.0F, this.mount.getMaxHeadXRot());
    }

    private Vec3 pickSpotBesideOwner() {
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.owner.getYRot());
        Vec3 right = new Vec3(-facing.z, 0.0D, facing.x);
        double distance = this.mount.getBbWidth() * 0.5D + 1.5D;
        for (int attempt = 0; attempt < 2; attempt++) {
            int currentSide = attempt == 0 ? this.side : -this.side;
            Vec3 base = this.owner.position().add(right.scale(currentSide * distance));
            double ground = this.mount.findGroundY(base.x, this.owner.getY() + 3.0D, base.z, 10);
            if (!Double.isNaN(ground) && this.mount.canOccupy(base.x, ground, base.z)) {
                this.side = currentSide;
                return new Vec3(base.x, ground, base.z);
            }
        }
        return null;
    }
}
