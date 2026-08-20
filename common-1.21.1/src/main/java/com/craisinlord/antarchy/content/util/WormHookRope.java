package com.craisinlord.antarchy.content.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the path of a taut rope from a fixed anchor point to a moving end point (the
 * player's eyes), bending around block corners when the straight-line path would otherwise
 * clip through terrain. The anchor never moves (the worm hook is stuck in a block), so this
 * only needs to add/remove bends on the player's side of the rope.
 *
 * Ported from the corner-wrapping algorithm in Yyon's GrappleMod (GPL-3.0), adapted from a
 * custom vector class to vanilla Vec3 and from a moving-hook/moving-player pair to a fixed
 * anchor.
 */
public final class WormHookRope {
    private static final double BEND_OFFSET = 0.05D;
    private static final double INTO_BLOCK = 0.05D;
    private static final int MAX_RECURSIONS = 10;

    private final List<Vec3> points = new ArrayList<>();
    private final List<Direction> bottomSides = new ArrayList<>();
    private final List<Direction> topSides = new ArrayList<>();

    private Vec3 prevEnd;
    private double ropeLength;

    public WormHookRope(Vec3 anchor, Vec3 end) {
        points.add(anchor);
        points.add(end);
        bottomSides.add(null);
        bottomSides.add(null);
        topSides.add(null);
        topSides.add(null);
        this.prevEnd = end;
    }

    /** Recomputes the bend list for the current anchor/end positions. Anchor must stay constant. */
    public void update(Entity context, Vec3 anchor, Vec3 end, double ropeLength) {
        this.ropeLength = ropeLength;
        Level level = context.level();

        points.set(0, anchor);
        points.set(points.size() - 1, end);

        // Unwind bends the rope has swung clear of.
        while (points.size() > 2) {
            int index = points.size() - 2;
            Vec3 closest = points.get(index);
            Direction bottomSide = bottomSides.get(index);
            Direction topSide = topSides.get(index);
            Vec3 ropeVec = end.subtract(closest);
            Vec3 beforePoint = points.get(index - 1);
            Vec3 edgeVec = normal(bottomSide).cross(normal(topSide));
            Vec3 planeNormal = beforePoint.subtract(closest).cross(edgeVec);

            if (planeNormal.lengthSqr() > 1.0E-9D && ropeVec.dot(planeNormal) > 0) {
                remove(index);
            } else {
                break;
            }
        }

        Vec3 closest = points.get(points.size() - 2);
        Vec3 prevClosest = points.size() == 2 ? anchor : closest;
        updateSegment(level, context, closest, prevClosest, end, prevEnd, points.size() - 1, 0);

        this.prevEnd = end;
    }

    private void updateSegment(Level level, Entity context, Vec3 top, Vec3 prevTop, Vec3 bottom, Vec3 prevBottom, int index, int recursions) {
        BlockHitResult bottomHit = rayTraceBlocks(level, context, bottom, top);
        if (bottomHit == null) {
            return;
        }
        if (rayTraceBlocks(level, context, prevBottom, prevTop) != null) {
            return;
        }

        Vec3 bottomHitVec = bottomHit.getLocation();
        Direction bottomSide = bottomHit.getDirection();
        Vec3 bottomNormal = normal(bottomSide);

        double prevRopeLen = prevTop.subtract(prevBottom).length();

        Vec3 cornerBound1 = bottomHitVec.add(changeLen(bottomNormal, -INTO_BLOCK));

        Vec3 boundOption1 = linePlaneIntersection(prevTop, prevBottom, cornerBound1, bottomNormal);
        Vec3 boundOption2 = linePlaneIntersection(top, prevTop, cornerBound1, bottomNormal);
        Vec3 boundOption3 = linePlaneIntersection(prevBottom, bottom, cornerBound1, bottomNormal);

        for (Vec3 cornerBound2 : new Vec3[]{boundOption1, boundOption2, boundOption3}) {
            if (cornerBound2 == null) {
                continue;
            }

            BlockHitResult cornerHit = rayTraceBlocks(level, context, cornerBound2, cornerBound1);
            if (cornerHit == null) {
                continue;
            }

            Vec3 cornerHitPos = cornerHit.getLocation();
            Direction cornerSide = cornerHit.getDirection();

            if (cornerSide == bottomSide || cornerSide.getOpposite() == bottomSide) {
                continue;
            }

            Vec3 actualCorner = cornerHitPos.add(changeLen(bottomNormal, INTO_BLOCK));
            Vec3 bend = actualCorner.add(changeLen(bottomNormal, BEND_OFFSET)).add(changeLen(normal(cornerSide), BEND_OFFSET));
            Vec3 topRopeVec = bend.subtract(top);
            Vec3 bottomRopeVec = bend.subtract(bottom);

            if (topRopeVec.length() < 0.05D && bottomSides.get(index - 1) == bottomSide && topSides.get(index - 1) == cornerSide) {
                continue;
            }
            if (bottomRopeVec.length() < 0.05D && bottomSides.get(index) == bottomSide && topSides.get(index) == cornerSide) {
                continue;
            }

            points.add(index, bend);
            bottomSides.add(index, bottomSide);
            topSides.add(index, cornerSide);

            if (getConsumedLength() + 0.2D > this.ropeLength) {
                remove(index);
                continue;
            }

            double newRopeLen = topRopeVec.length() + bottomRopeVec.length();
            double prevTopToBend = newRopeLen <= 0 ? 0 : topRopeVec.length() * prevRopeLen / newRopeLen;
            Vec3 prevBend = prevTop.add(changeLen(prevBottom.subtract(prevTop), prevTopToBend));

            if (recursions < MAX_RECURSIONS) {
                updateSegment(level, context, top, prevTop, bend, prevBend, index, recursions + 1);
            }
            break;
        }
    }

    private void remove(int index) {
        points.remove(index);
        bottomSides.remove(index);
        topSides.remove(index);
    }

    /** The pivot point closest to the moving end: either the anchor, or the last bend before it. */
    public Vec3 getPivot() {
        return points.get(points.size() - 2);
    }

    /** Rope length used up getting from the anchor to the pivot (0 if the rope has no bends). */
    public double getConsumedLength() {
        double dist = 0;
        for (int i = 0; i < points.size() - 2; i++) {
            dist += points.get(i).subtract(points.get(i + 1)).length();
        }
        return dist;
    }

    public List<Vec3> points() {
        return points;
    }

    private static Vec3 normal(Direction direction) {
        if (direction == null) {
            return Vec3.ZERO;
        }
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    private static Vec3 changeLen(Vec3 v, double len) {
        double oldLen = v.length();
        return oldLen == 0 ? v : v.scale(len / oldLen);
    }

    private static Vec3 linePlaneIntersection(Vec3 linePoint1, Vec3 linePoint2, Vec3 planePoint, Vec3 planeNormal) {
        Vec3 lineVec = linePoint2.subtract(linePoint1);
        double denom = lineVec.dot(planeNormal);
        if (denom == 0) {
            return null;
        }
        double d = planePoint.subtract(linePoint1).dot(planeNormal) / denom;
        return linePoint1.add(lineVec.scale(d));
    }

    private static BlockHitResult rayTraceBlocks(Level level, Entity context, Vec3 from, Vec3 to) {
        BlockHitResult result = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, context));
        return result.getType() == HitResult.Type.MISS ? null : result;
    }
}
