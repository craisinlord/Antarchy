package com.craisinlord.antarchy.content.client.renderer.clouds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class ElythiaGiantCloud {
    private static final float TOWER_VOXEL = 12.0F;
    private static final float SWIRL_VOXEL = 5.0F;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final int[][] NEIGHBOURS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final float voxel;
    private final float originX;
    private final float originZ;
    private final boolean[] cells;

    private ElythiaGiantCloud(int sizeX, int sizeY, int sizeZ, float voxel, float originX, float originZ, boolean[] cells) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.voxel = voxel;
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

    public float voxel() {
        return this.voxel;
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
        double girth = random.nextDouble();
        double height = (420.0D + random.nextDouble() * 110.0D) * (1.0D - 0.2D * girth) * scale;
        double baseRadius = (72.0D + random.nextDouble() * 20.0D) * (1.15D + 0.4D * girth) * scale;
        double taper = 0.12D + random.nextDouble() * 0.16D;
        double lean = random.nextDouble() * TWO_PI;
        double leanAmount = height * (0.06D + random.nextDouble() * 0.08D);
        tower(spheres, random, 0.0D, 0.0D, height, baseRadius, taper, 7 + random.nextInt(2),
                Math.cos(lean) * leanAmount, Math.sin(lean) * leanAmount, true);

        int shoulders = 2 + random.nextInt(2);
        double shoulderAngle = random.nextDouble() * TWO_PI;
        for (int i = 0; i < shoulders; i++) {
            shoulderAngle += TWO_PI / shoulders * (0.6D + random.nextDouble() * 0.5D);
            double distance = baseRadius * (0.8D + random.nextDouble() * 0.45D);
            double shoulderHeight = height * (0.25D + random.nextDouble() * 0.3D);
            double shoulderRadius = baseRadius * (0.6D + random.nextDouble() * 0.25D);
            tower(spheres, random, Math.cos(shoulderAngle) * distance, Math.sin(shoulderAngle) * distance,
                    shoulderHeight, shoulderRadius, taper, 4 + random.nextInt(2),
                    Math.cos(lean) * leanAmount * 0.4D, Math.sin(lean) * leanAmount * 0.4D, false);
        }

        int skirt = 9 + random.nextInt(4);
        for (int i = 0; i < skirt; i++) {
            double angle = random.nextDouble() * TWO_PI;
            double distance = baseRadius * (1.0D + random.nextDouble() * 1.0D);
            double radius = baseRadius * (0.25D + random.nextDouble() * 0.17D);
            spheres.add(new Sphere(Math.cos(angle) * distance, -radius * 0.55D, Math.sin(angle) * distance, radius));
        }

        return rasterize(spheres, TOWER_VOXEL);
    }

    public static ElythiaGiantCloud generateSwirl(long seed) {
        Random random = new Random(seed);
        List<Sphere> spheres = new ArrayList<>();
        double coil = 26.0D + random.nextDouble() * 12.0D;
        double tube = 7.0D + random.nextDouble() * 3.0D;
        double pitch = tube * (3.3D + random.nextDouble() * 0.5D);
        int turns = 4 + random.nextInt(4);
        double length = turns * pitch;
        double bend = (random.nextDouble() - 0.5D) * Math.toRadians(80.0D);
        double yaw = random.nextDouble() * TWO_PI;
        double phase = random.nextDouble() * TWO_PI;
        double yawCos = Math.cos(yaw);
        double yawSin = Math.sin(yaw);
        double total = turns * TWO_PI;
        double step = tube * 0.45D / coil;
        for (double theta = 0.0D; theta <= total; theta += step) {
            double t = theta / total;
            double edge = Math.min(1.0D, Math.min(t, 1.0D - t) * turns);
            double radius = coil * (0.72D + 0.28D * edge * edge * (3.0D - 2.0D * edge))
                    * (1.0D + 0.07D * Math.sin(theta * 0.45D + phase));
            double along = t * length - length * 0.5D;
            double axisX;
            double axisZ;
            double normalX;
            double normalZ;
            if (Math.abs(bend) < 1.0E-3D) {
                axisX = along;
                axisZ = 0.0D;
                normalX = 0.0D;
                normalZ = 1.0D;
            } else {
                double arc = length / bend;
                double angle = along / arc;
                axisX = arc * Math.sin(angle);
                axisZ = arc * (1.0D - Math.cos(angle));
                normalX = -Math.sin(angle);
                normalZ = Math.cos(angle);
            }
            double side = Math.sin(theta);
            double rise = 1.0D - Math.cos(theta);
            double lx = axisX + normalX * side * radius;
            double lz = axisZ + normalZ * side * radius;
            double y = tube + rise * radius;
            double size = tube * (0.9D + random.nextDouble() * 0.2D);
            spheres.add(new Sphere(lx * yawCos - lz * yawSin, y, lx * yawSin + lz * yawCos, size));
            if (random.nextDouble() < 0.15D) {
                double outY = -Math.cos(theta);
                double puff = tube * (0.45D + random.nextDouble() * 0.15D);
                double reach = tube * (0.55D + random.nextDouble() * 0.15D);
                double px = lx + normalX * side * reach;
                double pz = lz + normalZ * side * reach;
                spheres.add(new Sphere(px * yawCos - pz * yawSin, y + outY * reach, px * yawSin + pz * yawCos, puff));
            }
        }
        return rasterize(spheres, SWIRL_VOXEL);
    }

    private static void tower(List<Sphere> spheres, Random random, double baseX, double baseZ, double height,
                              double baseRadius, double taper, int levels, double leanX, double leanZ, boolean crown) {
        double topX = baseX;
        double topZ = baseZ;
        double topY = 0.0D;
        double topRadius = baseRadius;
        for (int level = 0; level < levels; level++) {
            double t = level / (double) (levels - 1);
            double radius = baseRadius * (1.0D - taper * t) * (0.9D + random.nextDouble() * 0.2D);
            double cy = radius * 0.3D + t * Math.max(0.0D, height - radius * 1.3D);
            double cx = baseX + leanX * t + (random.nextDouble() - 0.5D) * radius * 0.25D;
            double cz = baseZ + leanZ * t + (random.nextDouble() - 0.5D) * radius * 0.25D;
            spheres.add(new Sphere(cx, cy, cz, radius * 0.9D));
            int lobes = 4 + random.nextInt(3);
            double start = random.nextDouble() * TWO_PI;
            for (int i = 0; i < lobes; i++) {
                double angle = start + TWO_PI * i / lobes + (random.nextDouble() - 0.5D) * 0.6D;
                double distance = radius * (0.5D + random.nextDouble() * 0.25D);
                double lobeRadius = radius * (0.55D + random.nextDouble() * 0.2D);
                double ly = cy + (random.nextDouble() - 0.4D) * radius * 0.5D;
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
        double dome = random.nextDouble();
        int turrets = 2 + random.nextInt(3);
        double start = random.nextDouble() * TWO_PI;
        for (int i = 0; i < turrets; i++) {
            double angle = start + TWO_PI * i / turrets + (random.nextDouble() - 0.5D) * 0.8D;
            double distance = topRadius * (0.2D + random.nextDouble() * 0.35D + 0.25D * (1.0D - dome));
            double radius = topRadius * (0.6D + random.nextDouble() * 0.25D);
            double ty = topY + topRadius * (0.05D + random.nextDouble() * 0.15D + 0.3D * dome);
            double tx = topX + Math.cos(angle) * distance;
            double tz = topZ + Math.sin(angle) * distance;
            spheres.add(new Sphere(tx, ty, tz, radius));
            billow(spheres, random, tx, ty, tz, radius, Math.cos(angle), Math.sin(angle), 1 + random.nextInt(2));
        }
    }

    private static void billow(List<Sphere> spheres, Random random, double x, double y, double z, double radius,
                               double outX, double outZ, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.atan2(outZ, outX) + (random.nextDouble() - 0.5D) * 2.2D;
            double up = 0.05D + random.nextDouble() * 0.45D;
            double flat = Math.sqrt(1.0D - up * up);
            double distance = radius * (0.5D + random.nextDouble() * 0.2D);
            double child = radius * (0.6D + random.nextDouble() * 0.15D);
            spheres.add(new Sphere(
                    x + Math.cos(angle) * flat * distance,
                    y + up * distance,
                    z + Math.sin(angle) * flat * distance,
                    child));
        }
    }

    private static ElythiaGiantCloud rasterize(List<Sphere> spheres, float voxel) {
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
        int sizeX = (int) Math.ceil((maxX - minX) / voxel) + 1;
        int sizeY = (int) Math.ceil(maxY / voxel) + 1;
        int sizeZ = (int) Math.ceil((maxZ - minZ) / voxel) + 1;
        boolean[] cells = new boolean[sizeX * sizeY * sizeZ];
        for (Sphere sphere : spheres) {
            double reach = sphere.radius();
            int x0 = Math.max(0, (int) Math.floor((sphere.x() - reach - minX) / voxel));
            int x1 = Math.min(sizeX - 1, (int) Math.ceil((sphere.x() + reach - minX) / voxel));
            int y0 = Math.max(0, (int) Math.floor((sphere.y() - reach) / voxel));
            int y1 = Math.min(sizeY - 1, (int) Math.ceil((sphere.y() + reach) / voxel));
            int z0 = Math.max(0, (int) Math.floor((sphere.z() - reach - minZ) / voxel));
            int z1 = Math.min(sizeZ - 1, (int) Math.ceil((sphere.z() + reach - minZ) / voxel));
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    for (int x = x0; x <= x1; x++) {
                        int i = (y * sizeZ + z) * sizeX + x;
                        if (cells[i]) {
                            continue;
                        }
                        double px = minX + (x + 0.5D) * voxel;
                        double py = (y + 0.5D) * voxel;
                        double pz = minZ + (z + 0.5D) * voxel;
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
        cells = smooth(cells, sizeX, sizeY, sizeZ, true);
        cells = smooth(cells, sizeX, sizeY, sizeZ, false);
        return new ElythiaGiantCloud(sizeX, sizeY, sizeZ, voxel, (float) minX, (float) minZ, cells);
    }

    private static boolean[] smooth(boolean[] cells, int sizeX, int sizeY, int sizeZ, boolean erode) {
        boolean[] result = Arrays.copyOf(cells, cells.length);
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    int i = (y * sizeZ + z) * sizeX + x;
                    if (cells[i] != erode) {
                        continue;
                    }
                    int neighbours = 0;
                    for (int[] offset : NEIGHBOURS) {
                        int nx = x + offset[0];
                        int ny = y + offset[1];
                        int nz = z + offset[2];
                        if (ny < 0) {
                            neighbours += cells[i] ? 1 : 0;
                        } else if (nx >= 0 && nz >= 0 && nx < sizeX && ny < sizeY && nz < sizeZ
                                && cells[(ny * sizeZ + nz) * sizeX + nx]) {
                            neighbours++;
                        }
                    }
                    if (erode ? neighbours <= 2 : neighbours >= 5) {
                        result[i] = !erode;
                    }
                }
            }
        }
        return result;
    }
}
