package com.craisinlord.antarchy.content.entity.royal;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class RoyalMountCombatGoal extends Goal {
    private static final int FLIGHT_DECISION_INTERVAL = 40;

    private final RoyalMountEntity mount;
    private LivingEntity target;
    private int repathTicks;
    private int flightDecisionTicks;
    private boolean wantsFlight;
    private int stuckTicks;
    private Vec3 lastCheckPosition = Vec3.ZERO;
    private float orbitAngle;
    private int orbitDirection = 1;
    private int diveTicks;
    private int unseenTicks;

    public RoyalMountCombatGoal(RoyalMountEntity mount) {
        this.mount = mount;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = this.mount.getTarget();
        if (!this.isValid(candidate)) {
            return false;
        }
        this.target = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity current = this.mount.getTarget();
        if (current != this.target || !this.isValid(current)) {
            return false;
        }
        double range = this.mount.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE) * 1.2D;
        return this.mount.distanceToSqr(current) < range * range && this.unseenTicks < 200;
    }

    private boolean isValid(LivingEntity candidate) {
        return candidate != null && candidate.isAlive() && !this.mount.isVehicle() && !this.mount.isOrderedToSit()
                && candidate.level() == this.mount.level() && this.mount.canAttack(candidate);
    }

    @Override
    public void start() {
        this.repathTicks = 0;
        this.flightDecisionTicks = 0;
        this.wantsFlight = false;
        this.stuckTicks = 0;
        this.unseenTicks = 0;
        this.diveTicks = 0;
        this.lastCheckPosition = this.mount.position();
        Vec3 offset = this.mount.position().subtract(this.target.position());
        this.orbitAngle = (float) Mth.atan2(offset.z, offset.x);
        this.orbitDirection = this.mount.getRandom().nextBoolean() ? 1 : -1;
    }

    @Override
    public void stop() {
        this.target = null;
        this.mount.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity current = this.target;
        if (current == null) {
            return;
        }
        boolean canSee = this.mount.getSensing().hasLineOfSight(current);
        this.unseenTicks = canSee ? 0 : this.unseenTicks + 1;
        double gap = this.mount.combatGap(current);
        this.mount.getLookControl().setLookAt(current, 30.0F, 30.0F);
        this.trackStuck();
        this.updateFlightDecision(current, gap);

        if (this.mount.isActing()) {
            this.holdDuringAction(current);
            return;
        }

        int chosen = this.mount.chooseCombatAction(current, gap, canSee);
        if (chosen != RoyalMountEntity.ACTION_NONE && this.isFacing(current, this.facingTolerance(chosen))) {
            if (this.mount.startAction(chosen, current, current.getEyePosition(), false)) {
                this.diveTicks = 0;
                this.holdDuringAction(current);
                return;
            }
        }
        if (chosen != RoyalMountEntity.ACTION_NONE) {
            this.mount.turnTowards(current.position(), 18.0F);
        }

        if (this.mount.isFlying()) {
            this.tickAerial(current, gap);
        } else {
            this.tickGround(current, gap, canSee);
        }
    }

    private float facingTolerance(int action) {
        return switch (action) {
            case RoyalMountEntity.ACTION_BITE -> 50.0F;
            case RoyalMountEntity.ACTION_SIGNATURE -> 180.0F;
            default -> 120.0F;
        };
    }

    private boolean isFacing(LivingEntity current, float tolerance) {
        Vec3 toTarget = current.position().subtract(this.mount.position());
        float desired = (float) (Mth.atan2(toTarget.z, toTarget.x) * Mth.RAD_TO_DEG) - 90.0F;
        return Math.abs(Mth.wrapDegrees(desired - this.mount.getYRot())) <= tolerance;
    }

    private void holdDuringAction(LivingEntity current) {
        this.mount.getNavigation().stop();
        if (this.mount.isFlying()) {
            this.mount.claimFlight();
            if (this.mount.currentAction() == RoyalMountEntity.ACTION_BITE) {
                this.mount.getMoveControl().setWantedPosition(current.getX(), current.getY() + current.getBbHeight() * 0.5D, current.getZ(), 0.6D);
            } else {
                this.mount.getMoveControl().setWantedPosition(this.mount.getX(), this.mount.getY(), this.mount.getZ(), 0.4D);
            }
        } else if (this.mount.currentAction() != RoyalMountEntity.ACTION_BITE) {
            this.mount.setDeltaMovement(this.mount.getDeltaMovement().multiply(0.5D, 1.0D, 0.5D));
        }
    }

    private void trackStuck() {
        if (this.mount.tickCount % 20 != 0) {
            return;
        }
        Vec3 position = this.mount.position();
        boolean trying = !this.mount.getNavigation().isDone() || this.mount.isFlying();
        if (trying && position.distanceToSqr(this.lastCheckPosition) < 1.0D) {
            this.stuckTicks += 20;
        } else {
            this.stuckTicks = 0;
        }
        this.lastCheckPosition = position;
    }

    private void updateFlightDecision(LivingEntity current, double gap) {
        if (!this.mount.canFly()) {
            this.wantsFlight = false;
            return;
        }
        if (--this.flightDecisionTicks <= 0 || this.stuckTicks >= 40) {
            this.flightDecisionTicks = FLIGHT_DECISION_INTERVAL;
            boolean targetAbove = current.getY() - this.mount.getY() > 4.0D + this.mount.getBbHeight() * 0.25D;
            boolean targetAirborne = !current.onGround() && !current.isInWater() && RoyalMountEntity.isAirborne(current) || targetAbove;
            boolean groundBlocked = !this.mount.isFlying() && (this.stuckTicks >= 40
                    || this.mount.getNavigation().isDone() && gap > 4.0D && this.mount.getNavigation().createPath(current, 0) == null);
            boolean prefersAir = this.mount.prefersAerialCombat() && gap > 3.0D;
            this.wantsFlight = targetAirborne || groundBlocked || prefersAir || (this.mount.isFlying() && this.diveTicks > 0);
            if (this.stuckTicks >= 40) {
                this.stuckTicks = 0;
            }
        }
        if (this.wantsFlight) {
            this.mount.claimFlight();
        }
    }

    private void tickAerial(LivingEntity current, double gap) {
        if (!this.wantsFlight) {
            return;
        }
        this.mount.claimFlight();
        float scale = this.mount.getGrowthScale();
        boolean wantsBite = this.mount.isActionAvailable(RoyalMountEntity.ACTION_BITE)
                && (!this.mount.prefersAerialCombat() || gap < this.mount.preferredCombatRange() * 0.5D || this.diveTicks > 0);
        if (wantsBite && this.mount.isActionReady()) {
            this.diveTicks++;
            Vec3 approach = current.position().subtract(this.mount.position()).multiply(1.0D, 0.0D, 1.0D);
            Vec3 offset = approach.lengthSqr() > 1.0E-4D ? approach.normalize().scale(-(this.mount.getBbWidth() * 0.5D + 0.6D * scale)) : Vec3.ZERO;
            this.mount.getMoveControl().setWantedPosition(current.getX() + offset.x, current.getY() + 0.2D, current.getZ() + offset.z, 1.4D);
            if (this.diveTicks > 80) {
                this.diveTicks = 0;
                this.orbitDirection = -this.orbitDirection;
            }
            return;
        }
        this.diveTicks = 0;
        double radius = Math.max(6.0D + 1.5D * scale, this.mount.preferredCombatRange()) + this.mount.getBbWidth() * 0.5D;
        this.orbitAngle += this.orbitDirection * (float) Math.min(0.08D, 1.2D / radius);
        double x = current.getX() + Math.cos(this.orbitAngle) * radius;
        double z = current.getZ() + Math.sin(this.orbitAngle) * radius;
        double y = current.getY() + 3.0D + 0.8D * scale;
        if (!this.mount.canOccupy(x, y, z)) {
            this.orbitDirection = -this.orbitDirection;
            y += 2.0D + scale;
        }
        this.mount.getMoveControl().setWantedPosition(x, y, z, 1.1D);
    }

    private void tickGround(LivingEntity current, double gap, boolean canSee) {
        double preferred = this.mount.preferredCombatRange();
        boolean ranged = preferred > 0.0D && this.mount.canSpit();
        if (ranged && canSee && gap < preferred * 0.5D && !this.mount.isActionAvailable(RoyalMountEntity.ACTION_BITE)) {
            Vec3 away = this.mount.position().subtract(current.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() > 1.0E-4D && --this.repathTicks <= 0) {
                this.repathTicks = 10;
                Vec3 destination = this.mount.position().add(away.normalize().scale(4.0D + this.mount.getBbWidth() * 0.5D));
                this.mount.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.2D);
            }
            return;
        }
        if (ranged && canSee && gap <= preferred && gap >= preferred * 0.5D && !this.mount.isActionAvailable(RoyalMountEntity.ACTION_BITE)) {
            this.mount.getNavigation().stop();
            this.mount.turnTowards(current.position(), 12.0F);
            return;
        }
        if (this.mount.canBiteReach(current)) {
            this.mount.getNavigation().stop();
            this.mount.turnTowards(current.position(), 18.0F);
            return;
        }
        if (--this.repathTicks <= 0) {
            this.repathTicks = 6 + this.mount.getRandom().nextInt(6);
            this.mount.getNavigation().moveTo(current, 1.2D);
        }
    }
}
