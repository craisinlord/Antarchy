package com.craisinlord.antarchy.content.client.renderer.clouds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ElythiaGiantCloud {
    public static final float VOXEL = 12.0F;
    private static final double TWO_PI = Math.PI * 2.0D;

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final float originX;
    private final float originZ;
    private final boolean[] cells;

    private ElythiaGiantCloud(int sizeX, int sizeY, int sizeZ, float originX, float originZ, boolean[] cells) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.originX = originX;
        this.originZ = originZ;
        this.cells = cells;
    }

    public int sizeX() {
        return this.sizeX;
    }

    public int sizeY() {
        return this.sizeY;
    }

    public int sizeZ() {
        return this.sizeZ;
    }

    public float originX() {
        return this.originX;
    }

    public float originZ() {
        return this.originZ;
    }

    public boolean filled(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= this.sizeX || y >= this.sizeY || z >= this.sizeZ) {
            return false;
        }
        return this.cells[(y * this.sizeZ + z) * this.sizeX + x];
    }

    public static float shade(int normalX, int normalY, int normalZ) {
        return normalY > 0 ? 1.0F : normalY < 0 ? 0.7F : normalX != 0 ? 0.9F : 0.8F;
    }

    private record Sphere(double x, double y, double z, double radius) {
    }

    public static ElythiaGiantCloud generate(long seed) {
        return generate(seed, 1.0D);
    }

    public static ElythiaGiantCloud generate(long seed, double scale) {
        Random random = new Random(seed);
        List<Sphere> spheres = new ArrayList<>();
        double height = (420.0D + random.nextDouble() * 110.0D) * scale;
        double baseRadius = (72.0D + random.nextDouble() * 20.0D) * scale;
        double lean = random.nextDouble() * TWO_PI;
        double leanAmount = height * (0.08D + random.nextDouble() * 0.1D);
        tower(spheres, random, 0.0D, 0.0D, height, baseRadius, 7 + random.nextInt(2),
                Math.cos(lean) * leanAmount, Math.sin(lean) * leanAmount, true);

        int shoulders = 2 + random.nextInt(2);
        double shoulderAngle = random.nextDouble() * TWO_PI;
        for (int i = 0; i < shoulders; i++) {
            shoulderAngle += TWO_PI / shoulders * (0.6D + random.nextDouble() * 0.5D);
            double distance = baseRadius * (0.9D + random.nextDouble() * 0.5D);
            double shoulderHeight = height * (0.25D + random.nextDouble() * 0.3D);
            double shoulderRadius = baseRadius * (0.55D + random.nextDouble() * 0.25D);
            tower(spheres, random, Math.cos(shoulderAngle) * distance, Math.sin(shoulderAngle) * distance,
                    shoulderHeight, shoulderRadius, 4 + random.nextInt(2),
                    Math.cos(lean) * leanAmount * 0.4D, Math.sin(lean) * leanAmount * 0.4D, false);
        }

        int skirt = 5 + random.nextInt(3);
        for (int i = 0; i < skirt; i++) {
            double angle = random.nextDouble() * TWO_PI;
            double distance = baseRadius * (1.0D + random.nextDouble() * 0.6D);
            double radius = baseRadius * (0.35D + random.nextDouble() * 0.2D);
            spheres.add(new Sphere(Math.cos(angle) * distance, -radius * 0.35D, Math.sin(angle) * distance, radius));
        }

        return rasterize(spheres);
    }

    private static void tower(List<Sphere> spheres, Random random, double baseX, double baseZ, double height,
                              double baseRadius, int levels, double leanX, double leanZ, boolean crown) {
        double topX = baseX;
        double topZ = baseZ;
        double topY = 0.0D;
        double topRadius = baseRadius;
        for (int level = 0; level < levels; level++) {
            double t = level / (double) (levels - 1);
            double radius = baseRadius * (1.0D - 0.35D * t) * (0.9D + random.nextDouble() * 0.2D);
            double cy = radius * 0.3D + t * (height - radius * 1.3D);
            double cx = baseX + leanX * t + (random.nextDouble() - 0.5D) * radius * 0.25D;
            double cz = baseZ + leanZ * t + (random.nextDouble() - 0.5D) * radius * 0.25D;
            spheres.add(new Sphere(cx, cy, cz, radius * 0.85D));
            int lobes = 4 + random.nextInt(3);
            double start = random.nextDouble() * TWO_PI;
            for (int i = 0; i < lobes; i++) {
                double angle = start + TWO_PI * i / lobes + (random.nextDouble() - 0.5D) * 0.6D;
                double distance = radius * (0.6D + random.nextDouble() * 0.25D);
                double lobeRadius = radius * (0.48D + random.nextDouble() * 0.2D);
                double ly = cy + (random.nextDouble() - 0.35D) * radius * 0.6D;
                double lx = cx + Math.cos(angle) * distance;
                double lz = cz + Math.sin(angle) * distance;
                spheres.add(new Sphere(lx, ly, lz, lobeRadius));
                billow(spheres, random, lx, ly, lz, lobeRadius, Math.cos(angle), Math.sin(angle), 1 + random.nextInt(2));
            }
            topX = cx;
            topZ = cz;
            topY = cy;
            topRadius = radius;
        }
        if (!crown) {
            return;
        }
        int turrets = 3 + random.nextInt(3);
        double start = random.nextDouble() * TWO_PI;
        for (int i = 0; i < turrets; i++) {
            double angle = start + TWO_PI * i / turrets + (random.nextDouble() - 0.5D) * 0.8D;
            double distance = topRadius * random.nextDouble() * 0.55D;
            double radius = topRadius * (0.45D + random.nextDouble() * 0.25D);
            double ty = topY + topRadius * (0.15D + random.nextDouble() * 0.75D);
            double tx = topX + Math.cos(angle) * distance;
            double tz = topZ + Math.sin(angle) * distance;
            spheres.add(new Sphere(tx, ty, tz, radius));
            billow(spheres, random, tx, ty, tz, radius, Math.cos(angle), Math.sin(angle), 2 + random.nextInt(2));
        }
    }

    private static void billow(List<Sphere> spheres, Random random, double x, double y, double z, double radius,
                               double outX, double outZ, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.atan2(outZ, outX) + (random.nextDouble() - 0.5D) * 2.2D;
            double up = 0.1D + random.nextDouble() * 0.8D;
            double flat = Math.sqrt(1.0D - up * up);
            double distance = radius * (0.6D + random.nextDouble() * 0.2D);
            double child = radius * (0.5D + random.nextDouble() * 0.15D);
            spheres.add(new Sphere(
                    x + Math.cos(angle) * flat * distance,
                    y + up * distance,
                    z + Math.sin(angle) * flat * distance,
                    child));
        }
    }

    private static ElythiaGiantCloud rasterize(List<Sphere> spheres) {
        double minX = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = 0.0D;
        double maxZ = -Double.MAX_VALUE;
        for (Sphere sphere : spheres) {
            double reach = sphere.radius();
            minX = Math.min(minX, sphere.x() - reach);
            minZ = Math.min(minZ, sphere.z() - reach);
            maxX = Math.max(maxX, sphere.x() + reach);
            maxY = Math.max(maxY, sphere.y() + reach);
            maxZ = Math.max(maxZ, sphere.z() + reach);
        }
        int sizeX = (int) Math.ceil((maxX - minX) / VOXEL) + 1;
        int sizeY = (int) Math.ceil(maxY / VOXEL) + 1;
        int sizeZ = (int) Math.ceil((maxZ - minZ) / VOXEL) + 1;
        boolean[] cells = new boolean[sizeX * sizeY * sizeZ];
        for (Sphere sphere : spheres) {
            double reach = sphere.radius();
            int x0 = Math.max(0, (int) Math.floor((sphere.x() - reach - minX) / VOXEL));
            int x1 = Math.min(sizeX - 1, (int) Math.ceil((sphere.x() + reach - minX) / VOXEL));
            int y0 = Math.max(0, (int) Math.floor((sphere.y() - reach) / VOXEL));
            int y1 = Math.min(sizeY - 1, (int) Math.ceil((sphere.y() + reach) / VOXEL));
            int z0 = Math.max(0, (int) Math.floor((sphere.z() - reach - minZ) / VOXEL));
            int z1 = Math.min(sizeZ - 1, (int) Math.ceil((sphere.z() + reach - minZ) / VOXEL));
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    for (int x = x0; x <= x1; x++) {
                        int i = (y * sizeZ + z) * sizeX + x;
                        if (cells[i]) {
                            continue;
                        }
                        double px = minX + (x + 0.5D) * VOXEL;
                        double py = (y + 0.5D) * VOXEL;
                        double pz = minZ + (z + 0.5D) * VOXEL;
                        double dx = px - sphere.x();
                        double dy = py - sphere.y();
                        double dz = pz - sphere.z();
                        if (dx * dx + dy * dy + dz * dz < sphere.radius() * sphere.radius()) {
                            cells[i] = true;
                        }
                    }
                }
            }
        }
        return new ElythiaGiantCloud(sizeX, sizeY, sizeZ, (float) minX, (float) minZ, cells);
    }
}
