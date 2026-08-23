package com.craisinlord.antarchy.content.portalgun;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public record PortalGunWorldPortalShape(
        Vec3 center,
        Vec3 normal,
        Vec3 up,
        Vec3 right,
        double halfWidth,
        double halfHeight,
        double halfDepth
) {
    public static final double DEFAULT_SCAN_DISTANCE = 3.0D;

    public AABB getPlane() {
        return this.createPlaneAround(0.0D, 0.0125D);
    }

    public AABB getFlatPlane() {
        return this.createPlaneAround(0.0D, 0.0D);
    }

    public AABB getScanRange() {
        return this.getFlatPlane().expandTowards(this.normal.scale(-DEFAULT_SCAN_DISTANCE));
    }

    public AABB getPortalInsides() {
        return this.createPlaneAround(0.0D, this.halfDepth);
    }

    public AABB getTeleportPlane(double offset) {
        return this.createPlaneAround(offset, this.halfDepth);
    }

    public Vec3[] getCorners(double normalOffset) {
        Vec3 center = this.center.add(this.normal.scale(normalOffset));
        Vec3 width = this.right.scale(this.halfWidth);
        Vec3 height = this.up.scale(this.halfHeight);
        return new Vec3[] {
                center.subtract(width).subtract(height),
                center.subtract(width).add(height),
                center.add(width).add(height),
                center.add(width).subtract(height)
        };
    }

    public PortalLocalCoords localCoords(Vec3 position) {
        Vec3 relative = position.subtract(this.center);
        return new PortalLocalCoords(relative.dot(this.right), relative.dot(this.up), relative.dot(this.normal));
    }

    public boolean contains(Vec3 position, double depthPadding) {
        PortalLocalCoords coords = this.localCoords(position);
        return Math.abs(coords.horizontal()) <= this.halfWidth
                && Math.abs(coords.vertical()) <= this.halfHeight
                && Math.abs(coords.depth()) <= this.halfDepth + depthPadding;
    }

    public boolean crosses(Vec3 previousPosition, Vec3 currentPosition) {
        PortalLocalCoords previous = this.localCoords(previousPosition);
        PortalLocalCoords current = this.localCoords(currentPosition);
        double depthDelta = current.depth() - previous.depth();
        if (depthDelta > -1.0E-6D || previous.depth() <= 0.0D || current.depth() > 0.0D) {
            return false;
        }
        double t = -previous.depth() / depthDelta;
        if (t < 0.0D || t > 1.0D) {
            return false;
        }
        double horizontal = previous.horizontal() + (current.horizontal() - previous.horizontal()) * t;
        double vertical = previous.vertical() + (current.vertical() - previous.vertical()) * t;
        return Math.abs(horizontal) <= this.halfWidth && Math.abs(vertical) <= this.halfHeight;
    }

    public boolean shouldRenderFront(Vec3 cameraPos) {
        PortalLocalCoords coords = this.localCoords(cameraPos);
        if (coords.depth() <= 0.0D) {
            return false;
        }
        double depthScale = Math.min(DEFAULT_SCAN_DISTANCE, Math.max(0.0D, coords.depth()));
        return Math.abs(coords.horizontal()) <= this.halfWidth + depthScale
                && Math.abs(coords.vertical()) <= this.halfHeight + depthScale;
    }

    public boolean intersectsFront(AABB box, double tolerance) {
        double maxDepth = Double.NEGATIVE_INFINITY;
        for (Vec3 corner : corners(box)) {
            maxDepth = Math.max(maxDepth, this.localCoords(corner).depth());
            if (maxDepth >= -tolerance) {
                return true;
            }
        }
        return false;
    }

    public AABB getBoundsForCulling() {
        return this.createPlaneAround(0.0D, this.halfDepth).inflate(1.5D);
    }

    public List<AABB> getCollisionBoundaries(double borderThickness) {
        double horizontalOuter = this.halfWidth + borderThickness;
        double verticalOuter = this.halfHeight + borderThickness;
        return List.of(
                this.createLocalBox(-horizontalOuter, this.halfWidth, -verticalOuter, verticalOuter, -this.halfDepth, this.halfDepth),
                this.createLocalBox(-this.halfWidth, horizontalOuter, -verticalOuter, verticalOuter, -this.halfDepth, this.halfDepth),
                this.createLocalBox(-this.halfWidth, this.halfWidth, -verticalOuter, -this.halfHeight, -this.halfDepth, this.halfDepth),
                this.createLocalBox(-this.halfWidth, this.halfWidth, this.halfHeight, verticalOuter, -this.halfDepth, this.halfDepth)
        );
    }

    private AABB createPlaneAround(double planeOffset, double thickness) {
        Vec3 boxCenter = this.center.add(this.normal.scale(planeOffset));
        double halfX = this.halfWidth * Math.abs(this.right.x) + this.halfHeight * Math.abs(this.up.x) + thickness * Math.abs(this.normal.x);
        double halfY = this.halfWidth * Math.abs(this.right.y) + this.halfHeight * Math.abs(this.up.y) + thickness * Math.abs(this.normal.y);
        double halfZ = this.halfWidth * Math.abs(this.right.z) + this.halfHeight * Math.abs(this.up.z) + thickness * Math.abs(this.normal.z);
        return new AABB(boxCenter.x - halfX, boxCenter.y - halfY, boxCenter.z - halfZ, boxCenter.x + halfX, boxCenter.y + halfY, boxCenter.z + halfZ);
    }

    private AABB createLocalBox(double minHorizontal, double maxHorizontal, double minVertical, double maxVertical, double minDepth, double maxDepth) {
        Vec3[] corners = new Vec3[] {
                this.center.add(this.right.scale(minHorizontal)).add(this.up.scale(minVertical)).add(this.normal.scale(minDepth)),
                this.center.add(this.right.scale(minHorizontal)).add(this.up.scale(minVertical)).add(this.normal.scale(maxDepth)),
                this.center.add(this.right.scale(minHorizontal)).add(this.up.scale(maxVertical)).add(this.normal.scale(minDepth)),
                this.center.add(this.right.scale(minHorizontal)).add(this.up.scale(maxVertical)).add(this.normal.scale(maxDepth)),
                this.center.add(this.right.scale(maxHorizontal)).add(this.up.scale(minVertical)).add(this.normal.scale(minDepth)),
                this.center.add(this.right.scale(maxHorizontal)).add(this.up.scale(minVertical)).add(this.normal.scale(maxDepth)),
                this.center.add(this.right.scale(maxHorizontal)).add(this.up.scale(maxVertical)).add(this.normal.scale(minDepth)),
                this.center.add(this.right.scale(maxHorizontal)).add(this.up.scale(maxVertical)).add(this.normal.scale(maxDepth))
        };
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (Vec3 corner : corners) {
            minX = Math.min(minX, corner.x);
            minY = Math.min(minY, corner.y);
            minZ = Math.min(minZ, corner.z);
            maxX = Math.max(maxX, corner.x);
            maxY = Math.max(maxY, corner.y);
            maxZ = Math.max(maxZ, corner.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static Vec3[] corners(AABB box) {
        return new Vec3[] {
                new Vec3(box.minX, box.minY, box.minZ),
                new Vec3(box.minX, box.minY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.minX, box.maxY, box.maxZ),
                new Vec3(box.maxX, box.minY, box.minZ),
                new Vec3(box.maxX, box.minY, box.maxZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.maxZ)
        };
    }

    public record PortalLocalCoords(double horizontal, double vertical, double depth) {
    }
}
