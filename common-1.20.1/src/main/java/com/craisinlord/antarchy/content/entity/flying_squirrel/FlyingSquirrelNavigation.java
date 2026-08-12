package com.craisinlord.antarchy.content.entity.flying_squirrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FlyingSquirrelNavigation extends GroundPathNavigation {
    private static final double MIN_GLIDE_DISTANCE = 10.0D;
    private static final double MAX_GLIDE_DISTANCE = 64.0D;
    private static final double MIN_GLIDE_HEIGHT = 3.0D;
    private static final int LAUNCH_EDGE_SEARCH_RADIUS = 7;
    private static final int REPATH_INTERVAL = 10;

    private final FlyingSquirrelEntity squirrel;
    @Nullable
    private BlockPos requestedTarget;
    @Nullable
    private BlockPos adjustedTarget;
    @Nullable
    private String requestedPurpose;
    @Nullable
    private GlideLaunchPlan glideLaunchPlan;
    @Nullable
    private BlockPos pathToPosition;

    private double requestedSpeed;
    private boolean glidingToLaunch;
    private boolean glideStarted;
    private int repathDelay;
    private int repathFailStreak;

    public FlyingSquirrelNavigation(FlyingSquirrelEntity squirrel, Level level) {
        super(squirrel, level);
        this.squirrel = squirrel;
        this.setCanFloat(true);
    }

    boolean requestTravelTo(BlockPos target, double speed, String purpose) {
        this.requestedTarget = target.immutable();
        this.requestedPurpose = purpose;
        this.requestedSpeed = speed;
        this.repathDelay = 0;

        this.adjustedTarget = this.adjustNavigationTarget(this.requestedTarget);
        this.glideLaunchPlan = null;
        this.glidingToLaunch = false;
        this.glideStarted = false;
        if (!this.squirrel.isGliding()
                && this.squirrel.onGround()) {

            GlideLaunchPlan plan = this.findLaunchPlan(this.adjustedTarget);
            if (plan != null
                    && !this.squirrel.isRecentGlidePath(
                    Vec3.atBottomCenterOf(plan.launchStandPos),
                    this.adjustedTarget
            )
                    && this.shouldGlideBetween(
                    Vec3.atBottomCenterOf(plan.launchStandPos),
                    this.adjustedTarget
            )
                    && this.hasLaunchEdgeToward(plan.launchStandPos, this.adjustedTarget)) {
                this.glideLaunchPlan = plan;
                this.glidingToLaunch = true;
            }
        }

        return this.beginGroundOrLaunchMove("request");
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        return this.requestTravelTo(BlockPos.containing(x, y, z), speed, "generic");
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        Path path = this.createPath(entity, 0);
        if (path != null) {
            return this.moveTo(path, speed);
        } else {
            this.pathToPosition = entity.blockPosition().immutable();
            this.requestedTarget = this.pathToPosition;
            this.adjustedTarget = this.adjustNavigationTarget(this.pathToPosition);
            this.requestedPurpose = "generic";
            this.requestedSpeed = speed;
            this.glideLaunchPlan = null;
            this.glidingToLaunch = false;
            this.glideStarted = false;
            return true;
        }
    }

    @Override
    @Nullable
    public Path createPath(BlockPos pos, int accuracy) {
        this.pathToPosition = pos.immutable();
        return super.createPath(this.adjustNavigationTarget(pos), accuracy);
    }

    @Override
    @Nullable
    public Path createPath(Entity entity, int accuracy) {
        this.pathToPosition = entity.blockPosition().immutable();
        return super.createPath(entity, accuracy);
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        return this.isNavigationStandPos(pos)
                || this.isNavigationTransitionPos(pos)
                || super.isStableDestination(pos);
    }

    @Override
    protected boolean canUpdatePath() {
        return !this.squirrel.isGliding();
    }

    @Override
    public void stop() {
        super.stop();
        this.clearTraversal();
    }

    void cancelTraversal() {
        super.stop();
        this.clearTraversal();
    }

    @Override
    public void tick() {
        if (this.requestedTarget == null) {
            super.tick();
            return;
        }

        if (this.squirrel.isGliding()) {
            this.glideStarted = true;
            return;
        }
        if (this.glideStarted) {
            this.glideStarted = false;
            this.glidingToLaunch = false;
            this.glideLaunchPlan = null;

            if (this.adjustedTarget != null && !this.squirrel.isAtStandPos(this.adjustedTarget)) {
                this.beginGroundOrLaunchMove("post_glide");
            } else {
                this.clearTraversal();
            }
            return;
        }
        if (this.glidingToLaunch && this.glideLaunchPlan != null && this.adjustedTarget != null) {
            this.tickGlideApproach();
            return;
        }
        this.tickGroundNav();

        if (this.adjustedTarget != null && this.squirrel.isAtStandPos(this.adjustedTarget)) {
            this.clearTraversal();
            return;
        }

        if (this.repathDelay > 0) {
            this.repathDelay--;
        }

        if (this.repathDelay <= 0 && (this.isDone() || this.getPath() == null)) {
            this.beginGroundOrLaunchMove("repath");
        }
    }

    private void tickGlideApproach() {
        if (this.glideLaunchPlan == null || this.adjustedTarget == null) {
            this.glidingToLaunch = false;
            this.beginGroundOrLaunchMove("glide_missing_plan");
            return;
        }

        BlockPos launch = this.glideLaunchPlan.launchStandPos;

        if (this.squirrel.isAtStandPos(launch)) {
            if (this.hasLaunchEdgeToward(launch, this.adjustedTarget)
                    && this.shouldGlideBetween(Vec3.atBottomCenterOf(launch), this.adjustedTarget)) {
                String reason = this.requestedPurpose == null
                        ? "direct_navigation"
                        : "direct_" + this.requestedPurpose;
                this.squirrel.startGliding(this.adjustedTarget, reason);
                this.glideStarted = true;
                return;
            }
            this.glidingToLaunch = false;
            this.glideLaunchPlan = null;
            this.beginGroundOrLaunchMove("glide_launch_rejected");
            return;
        }

        this.tickGroundNav();

        if (this.repathDelay > 0) {
            this.repathDelay--;
        }

        if (this.repathDelay <= 0 && (this.isDone() || this.getPath() == null)) {
            this.tryStartMoveToward(launch, this.requestedSpeed, "glide_launch_approach");
        }
    }

    private boolean beginGroundOrLaunchMove(String reason) {
        BlockPos target = this.glidingToLaunch && this.glideLaunchPlan != null
                ? this.glideLaunchPlan.launchStandPos
                : this.adjustedTarget;

        if (target == null) {
            return false;
        }

        return this.tryStartMoveToward(target, this.requestedSpeed, reason);
    }

    private void tickGroundNav() {
        super.tick();
        this.tickPathToPositionFallback();
    }

    private boolean tryStartMoveToward(BlockPos target, double speed, String reason) {
        this.pathToPosition = target.immutable();

        Path path = super.createPath(target, 0);
        if (path != null) {
            boolean started = super.moveTo(path, speed);
            if (started) {
                this.repathFailStreak = 0;
                this.repathDelay = REPATH_INTERVAL;
                return true;
            }
        }
        this.repathFailStreak++;
        this.repathDelay = REPATH_INTERVAL * Math.min(this.repathFailStreak, 4);
        this.speedModifier = speed;
        return true;
    }

    private void tickPathToPositionFallback() {
        if (!this.isDone()) {
            return;
        }

        if (this.pathToPosition == null) {
            return;
        }

        double threshold = Math.max(this.mob.getBbWidth(), 1.0F);

        if (this.pathToPosition.closerToCenterThan(this.mob.position(), threshold)
                || (this.mob.getY() > this.pathToPosition.getY()
                && BlockPos.containing(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ())
                .closerToCenterThan(this.mob.position(), threshold))) {
            this.pathToPosition = null;
            return;
        }

        this.mob.getMoveControl().setWantedPosition(
                this.pathToPosition.getX() + 0.5D,
                this.pathToPosition.getY(),
                this.pathToPosition.getZ() + 0.5D,
                this.speedModifier
        );
    }

    private BlockPos adjustNavigationTarget(BlockPos desiredTarget) {
        if (this.isNavigationStandPos(desiredTarget) || this.isNavigationTransitionPos(desiredTarget)) {
            return desiredTarget.immutable();
        }

        BlockPos adjusted = this.findNearbyNavigationStandPos(desiredTarget, 4, 4);
        return adjusted != null ? adjusted : desiredTarget.immutable();
    }

    private boolean isNavigationStandPos(BlockPos pos) {
        return FlyingSquirrelEntity.isOpenStandPos(this.level, pos);
    }

    private boolean isNavigationTransitionPos(BlockPos pos) {
        return FlyingSquirrelEntity.isStandableSurface(this.level.getBlockState(pos.below()))
                && FlyingSquirrelEntity.hasNearbyCanopy(this.level, pos);
    }

    @Nullable
    private BlockPos findNearbyNavigationStandPos(BlockPos targetPos, int horizontalRadius, int verticalRadius) {
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (BlockPos candidate : BlockPos.betweenClosed(
                targetPos.offset(-horizontalRadius, -verticalRadius, -horizontalRadius),
                targetPos.offset(horizontalRadius, verticalRadius, horizontalRadius))) {

            if (!this.isNavigationStandPos(candidate)) {
                continue;
            }

            double score = -Vec3.atBottomCenterOf(candidate).distanceToSqr(Vec3.atBottomCenterOf(targetPos));
            if (FlyingSquirrelEntity.isAdjacentToLog(this.level, candidate)) {
                score += 6.0D;
            }
            if (FlyingSquirrelEntity.hasNearbyCanopy(this.level, candidate)) {
                score += 4.0D;
            }
            if (candidate.getY() > targetPos.getY()) {
                score += (candidate.getY() - targetPos.getY()) * 0.35D;
            }

            if (score > bestScore) {
                bestScore = score;
                bestPos = candidate.immutable();
            }
        }

        return bestPos;
    }

    private boolean shouldGlideBetween(Vec3 fromPos, BlockPos standPos) {
        if (this.squirrel.isGliding()) {
            return false;
        }

        double dx = standPos.getX() + 0.5D - fromPos.x;
        double dz = standPos.getZ() + 0.5D - fromPos.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double verticalDrop = fromPos.y - (standPos.getY() + 0.5D);

        return horizontalDistance >= MIN_GLIDE_DISTANCE
                && horizontalDistance <= MAX_GLIDE_DISTANCE
                && verticalDrop >= MIN_GLIDE_HEIGHT;
    }

    @Nullable
    private GlideLaunchPlan findLaunchPlan(BlockPos targetStandPos) {
        BlockPos origin = this.squirrel.blockPosition();
        GlideLaunchPlan bestPlan = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-LAUNCH_EDGE_SEARCH_RADIUS, -2, -LAUNCH_EDGE_SEARCH_RADIUS),
                origin.offset(LAUNCH_EDGE_SEARCH_RADIUS, 2, LAUNCH_EDGE_SEARCH_RADIUS))) {

            if (!FlyingSquirrelEntity.isOpenStandPos(this.level, candidate)) {
                continue;
            }

            if (!this.hasLaunchEdgeToward(candidate, targetStandPos)) {
                continue;
            }
            if (!this.squirrel.isAtStandPos(candidate)
                    && Vec3.atBottomCenterOf(candidate).distanceToSqr(this.squirrel.position()) > 36.0D) {
                continue;
            }

            if (this.squirrel.isRecentGlidePath(Vec3.atBottomCenterOf(candidate), targetStandPos)) {
                continue;
            }

            Vec3 launchPos = Vec3.atBottomCenterOf(candidate);
            double glideScore = this.squirrel.scoreGlideTargetFrom(launchPos, targetStandPos);
            if (glideScore == Double.NEGATIVE_INFINITY) {
                continue;
            }

            double walkDistance = launchPos.distanceTo(this.squirrel.position());
            double score = glideScore - walkDistance * 1.75D;
            if (this.squirrel.isAtStandPos(candidate)) {
                score += 3.0D;
            }

            if (score > bestScore) {
                bestScore = score;
                bestPlan = new GlideLaunchPlan(candidate.immutable());
            }
        }

        return bestPlan;
    }

    private boolean hasLaunchEdgeToward(BlockPos launchStandPos, BlockPos targetStandPos) {
        Direction launchDirection = this.getLaunchDirectionToward(launchStandPos, targetStandPos);
        BlockPos edgePos = launchStandPos.relative(launchDirection);
        return FlyingSquirrelEntity.isOpenAir(this.level, edgePos)
                && !FlyingSquirrelEntity.isStandableSurface(this.level.getBlockState(edgePos.below()));
    }

    private Direction getLaunchDirectionToward(BlockPos launchStandPos, BlockPos targetStandPos) {
        double dx = (targetStandPos.getX() + 0.5D) - (launchStandPos.getX() + 0.5D);
        double dz = (targetStandPos.getZ() + 0.5D) - (launchStandPos.getZ() + 0.5D);

        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0.0D ? Direction.EAST : Direction.WEST;
        }

        return dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
    }

    private void clearTraversal() {
        this.requestedTarget = null;
        this.adjustedTarget = null;
        this.requestedPurpose = null;
        this.requestedSpeed = 0.0D;
        this.glideLaunchPlan = null;
        this.glidingToLaunch = false;
        this.glideStarted = false;
        this.repathDelay = 0;
        this.repathFailStreak = 0;
        this.pathToPosition = null;
    }

    static final class GlideLaunchPlan {
        final BlockPos launchStandPos;

        GlideLaunchPlan(BlockPos launchStandPos) {
            this.launchStandPos = launchStandPos;
        }
    }
}
