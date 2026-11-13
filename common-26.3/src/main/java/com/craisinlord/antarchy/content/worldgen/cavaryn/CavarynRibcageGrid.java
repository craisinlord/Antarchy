package com.craisinlord.antarchy.content.worldgen.cavaryn;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public final class CavarynRibcageGrid {
    public static final int CELL_SIZE = 320;
    private static final int MAX_CACHED_CELLS = 1024;
    private static final float SKELETON_CHANCE = 0.8F;
    private static final int START_ATTEMPTS = 4;
    private static final int HEADING_ATTEMPTS = 6;
    private static final double MIN_LENGTH = 100.0D;
    private static final double SEGMENT_LENGTH = 12.0D;
    private static final double RIB_SPACING = 10.0D;
    private static final int RIB_SEGMENTS = 9;
    private static final double CAP_SLOPE = 1.4D;

    private static final Map<Long, WeakReference<CavarynRibcageGrid>> BY_SALT = new ConcurrentHashMap<>();

    private final long salt;
    private final CavarynSurfaceMask.Filter filter;
    private final Map<Long, Skeleton[]> cells = new ConcurrentHashMap<>();
    private final ThreadLocal<ColumnCache> columns = ThreadLocal.withInitial(ColumnCache::new);

    private CavarynRibcageGrid(long salt, CavarynSurfaceMask.Filter filter) {
        this.salt = salt;
        this.filter = filter;
    }

    public static CavarynRibcageGrid create(long salt, CavarynSurfaceMask.Filter filter) {
        CavarynRibcageGrid existing = forSalt(salt);
        if (existing != null) {
            return existing;
        }
        BY_SALT.values().removeIf(reference -> reference.get() == null);
        CavarynRibcageGrid grid = new CavarynRibcageGrid(salt, filter);
        BY_SALT.put(salt, new WeakReference<>(grid));
        return grid;
    }

    public static CavarynRibcageGrid forSalt(long salt) {
        WeakReference<CavarynRibcageGrid> reference = BY_SALT.get(salt);
        return reference != null ? reference.get() : null;
    }

    public double boneDistance(int x, int y, int z) {
        ColumnCache cache = this.candidates(x, z);
        double py = y + 0.5D;
        if (py < cache.boneMinY || py > cache.boneMaxY) {
            return Double.MAX_VALUE;
        }
        double px = x + 0.5D;
        double pz = z + 0.5D;
        double best = Double.MAX_VALUE;
        for (Bone bone : cache.bones) {
            if (py < bone.minY || py > bone.maxY) {
                continue;
            }
            double value = bone.distance(px, py, pz);
            if (value < best) {
                best = value;
            }
        }
        return best;
    }

    public double trenchDistance(int x, int y, int z) {
        ColumnCache cache = this.candidates(x, z);
        double py = y + 0.5D;
        if (cache.trenches.length == 0 || py < cache.trenchMinY) {
            return Double.MAX_VALUE;
        }
        double px = x + 0.5D;
        double pz = z + 0.5D;
        double best = Double.MAX_VALUE;
        for (Trench trench : cache.trenches) {
            double value = trench.distance(px, py, pz);
            if (value < best) {
                best = value;
            }
        }
        return best;
    }

    public int[] boneRange(int x, int z) {
        ColumnCache cache = this.candidates(x, z);
        if (cache.bones.length == 0) {
            return null;
        }
        return new int[]{Mth.floor(cache.boneMinY), Mth.ceil(cache.boneMaxY)};
    }

    private ColumnCache candidates(int x, int z) {
        int qx = x >> 2;
        int qz = z >> 2;
        ColumnCache cache = this.columns.get();
        if (cache.valid && cache.qx == qx && cache.qz == qz) {
            return cache;
        }
        double minX = qx * 4.0D;
        double minZ = qz * 4.0D;
        double maxX = minX + 4.0D;
        double maxZ = minZ + 4.0D;
        int cellX = Math.floorDiv(qx * 4, CELL_SIZE);
        int cellZ = Math.floorDiv(qz * 4, CELL_SIZE);
        List<Bone> bones = new ArrayList<>();
        List<Trench> trenches = new ArrayList<>();
        double boneMinY = Double.MAX_VALUE;
        double boneMaxY = -Double.MAX_VALUE;
        double trenchMinY = Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (Skeleton skeleton : this.skeletons(cellX + dx, cellZ + dz)) {
                    if (skeleton.maxX < minX || skeleton.minX > maxX || skeleton.maxZ < minZ || skeleton.minZ > maxZ) {
                        continue;
                    }
                    for (Bone bone : skeleton.bones) {
                        if (bone.maxX < minX || bone.minX > maxX || bone.maxZ < minZ || bone.minZ > maxZ) {
                            continue;
                        }
                        bones.add(bone);
                        boneMinY = Math.min(boneMinY, bone.minY);
                        boneMaxY = Math.max(boneMaxY, bone.maxY);
                    }
                    for (Trench trench : skeleton.trenches) {
                        if (trench.maxX < minX || trench.minX > maxX || trench.maxZ < minZ || trench.minZ > maxZ) {
                            continue;
                        }
                        trenches.add(trench);
                        trenchMinY = Math.min(trenchMinY, Math.min(trench.ay, trench.by));
                    }
                }
            }
        }
        cache.qx = qx;
        cache.qz = qz;
        cache.bones = bones.toArray(new Bone[0]);
        cache.trenches = trenches.toArray(new Trench[0]);
        cache.boneMinY = boneMinY;
        cache.boneMaxY = boneMaxY;
        cache.trenchMinY = trenchMinY;
        cache.valid = true;
        return cache;
    }

    private Skeleton[] skeletons(int cellX, int cellZ) {
        long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
        Skeleton[] cached = this.cells.get(key);
        if (cached != null) {
            return cached;
        }
        Skeleton[] generated = this.generate(cellX, cellZ);
        CavarynCavityGrid.trim(this.cells, MAX_CACHED_CELLS);
        Skeleton[] existing = this.cells.putIfAbsent(key, generated);
        return existing != null ? existing : generated;
    }

    private Skeleton[] generate(int cellX, int cellZ) {
        XoroshiroRandomSource random = new XoroshiroRandomSource(mix(this.salt ^ (cellX * 0x632BE59BD9B4E019L) ^ (cellZ * 0x9E3779B97F4A7C15L)), ~this.salt);
        if (random.nextFloat() >= SKELETON_CHANCE) {
            return new Skeleton[0];
        }
        double y = 238.0D + random.nextDouble() * 10.0D;
        double length = 150.0D + random.nextDouble() * 90.0D;
        double ribWidth = 18.0D + random.nextDouble() * 8.0D;
        double ribHeight = 30.0D + random.nextDouble() * 10.0D;
        double sweep = 0.15D + random.nextDouble() * 0.2D;
        double bestX = 0.0D;
        double bestZ = 0.0D;
        double bestYaw = 0.0D;
        double bestInside = 0.0D;
        for (int attempt = 0; attempt < START_ATTEMPTS; attempt++) {
            double x = cellX * (double) CELL_SIZE + CELL_SIZE * (0.2D + random.nextDouble() * 0.6D);
            double z = cellZ * (double) CELL_SIZE + CELL_SIZE * (0.2D + random.nextDouble() * 0.6D);
            if (!this.filter.accepts(x, z)) {
                continue;
            }
            double baseYaw = random.nextDouble() * Math.PI * 2.0D;
            for (int turn = 0; turn < HEADING_ATTEMPTS; turn++) {
                double yaw = baseYaw + turn * Math.PI * 2.0D / HEADING_ATTEMPTS;
                double inside = 0.0D;
                while (inside < length && this.filter.accepts(x + Math.cos(yaw) * (inside + 40.0D), z + Math.sin(yaw) * (inside + 40.0D))) {
                    inside += 20.0D;
                }
                if (inside > bestInside) {
                    bestInside = inside;
                    bestX = x;
                    bestZ = z;
                    bestYaw = yaw;
                }
            }
        }
        if (bestInside < MIN_LENGTH) {
            return new Skeleton[0];
        }
        return new Skeleton[]{new Builder(random).build(bestX, y, bestZ, bestYaw, Math.min(length, bestInside), ribWidth, ribHeight, sweep)};
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static final class Builder {
        final XoroshiroRandomSource random;
        final List<Bone> bones = new ArrayList<>();
        final List<Trench> trenches = new ArrayList<>();

        Builder(XoroshiroRandomSource random) {
            this.random = random;
        }

        Skeleton build(double x, double y, double z, double yaw, double length, double ribWidth, double ribHeight, double sweep) {
            int segments = Math.max(4, (int) Math.ceil(length / SEGMENT_LENGTH));
            double[][] spine = new double[segments + 1][];
            double heading = yaw;
            spine[0] = new double[]{x, y, z, heading};
            for (int i = 1; i <= segments; i++) {
                heading += this.random.nextGaussian() * 0.12D;
                double ny = y + Math.sin(i * 0.45D) * 1.5D;
                x += Math.cos(heading) * SEGMENT_LENGTH;
                z += Math.sin(heading) * SEGMENT_LENGTH;
                spine[i] = new double[]{x, ny, z, heading};
            }
            for (int i = 0; i < segments; i++) {
                double s = i / (double) segments;
                double[] a = spine[i];
                double[] b = spine[i + 1];
                double radius = 4.2D * (1.0D - 0.55D * s);
                this.bones.add(new Bone(a[0], a[1], a[2], b[0], b[1], b[2], radius, 4.2D * (1.0D - 0.55D * (i + 1) / (double) segments)));
                double envelope = 0.55D + 0.45D * Math.pow(Math.sin(Math.PI * Math.min(1.0D, s / 0.8D)), 0.7D);
                this.trenches.add(new Trench(a[0], a[1] - 1.0D, a[2], b[0], b[1] - 1.0D, b[2],
                        ribWidth * envelope * 1.3D, ribHeight * envelope * 2.3D, a[1] + ribHeight * envelope * 1.85D));
            }
            for (double along = 0.0D; along < length; along += 5.0D) {
                double[] point = this.at(spine, along / SEGMENT_LENGTH);
                double knob = 5.0D * (1.0D - 0.55D * along / length);
                this.bones.add(new Bone(point[0], point[1] + 1.5D, point[2], point[0], point[1] + 1.5D + knob * 0.6D, point[2], knob, knob * 0.6D));
            }
            double ribStart = length * 0.1D;
            double ribEnd = length * 0.82D;
            for (double along = ribStart; along < ribEnd; along += RIB_SPACING + this.random.nextDouble() * 2.0D) {
                double s = (along - ribStart) / (ribEnd - ribStart);
                double scale = 0.55D + 0.45D * Math.pow(Math.sin(Math.PI * s), 0.7D);
                double[] point = this.at(spine, along / SEGMENT_LENGTH);
                for (int side = -1; side <= 1; side += 2) {
                    this.rib(point, side, ribWidth * scale * (0.92D + this.random.nextDouble() * 0.16D), ribHeight * scale, sweep);
                }
            }
            double[] head = spine[0];
            double hx = head[0] - Math.cos(head[3]) * 10.0D;
            double hz = head[2] - Math.sin(head[3]) * 10.0D;
            this.bones.add(new Skull(hx, head[1] + 6.0D, hz, head[3], 13.0D + this.random.nextDouble() * 3.0D));
            this.trenches.add(new Trench(head[0], head[1] - 1.0D, head[2], hx - Math.cos(head[3]) * 8.0D, head[1] - 1.0D, hz - Math.sin(head[3]) * 8.0D,
                    ribWidth * 0.7D, ribHeight, head[1] + ribHeight * 1.3D));
            return new Skeleton(this.bones.toArray(new Bone[0]), this.trenches.toArray(new Trench[0]));
        }

        private double[] at(double[][] spine, double index) {
            int i = Mth.clamp((int) Math.floor(index), 0, spine.length - 2);
            double f = Mth.clamp(index - i, 0.0D, 1.0D);
            double[] a = spine[i];
            double[] b = spine[i + 1];
            return new double[]{
                    Mth.lerp(f, a[0], b[0]),
                    Mth.lerp(f, a[1], b[1]),
                    Mth.lerp(f, a[2], b[2]),
                    Mth.lerp(f, a[3], b[3])
            };
        }

        private void rib(double[] base, int side, double width, double height, double sweep) {
            double heading = base[3];
            double tx = Math.cos(heading);
            double tz = Math.sin(heading);
            double nx = -tz * side;
            double nz = tx * side;
            double maxAngle = Math.toRadians(162.0D + this.random.nextDouble() * 12.0D);
            double px = base[0];
            double py = base[1];
            double pz = base[2];
            for (int i = 1; i <= RIB_SEGMENTS; i++) {
                double phi = maxAngle * i / RIB_SEGMENTS;
                double out = width * Math.sin(phi);
                double up = height * (1.0D - Math.cos(phi));
                double back = -sweep * up;
                double qx = base[0] + nx * out + tx * back;
                double qy = base[1] + up;
                double qz = base[2] + nz * out + tz * back;
                double t0 = (i - 1) / (double) RIB_SEGMENTS;
                double t1 = i / (double) RIB_SEGMENTS;
                this.bones.add(new Bone(px, py, pz, qx, qy, qz, 3.4D - 1.9D * t0, 3.4D - 1.9D * t1));
                px = qx;
                py = qy;
                pz = qz;
            }
        }
    }

    private static final class ColumnCache {
        boolean valid;
        int qx;
        int qz;
        Bone[] bones = new Bone[0];
        Trench[] trenches = new Trench[0];
        double boneMinY;
        double boneMaxY;
        double trenchMinY;
    }

    private static final class Skeleton {
        final Bone[] bones;
        final Trench[] trenches;
        final double minX;
        final double maxX;
        final double minZ;
        final double maxZ;

        Skeleton(Bone[] bones, Trench[] trenches) {
            this.bones = bones;
            this.trenches = trenches;
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            for (Bone bone : bones) {
                minX = Math.min(minX, bone.minX);
                maxX = Math.max(maxX, bone.maxX);
                minZ = Math.min(minZ, bone.minZ);
                maxZ = Math.max(maxZ, bone.maxZ);
            }
            for (Trench trench : trenches) {
                minX = Math.min(minX, trench.minX);
                maxX = Math.max(maxX, trench.maxX);
                minZ = Math.min(minZ, trench.minZ);
                maxZ = Math.max(maxZ, trench.maxZ);
            }
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }

    private static class Bone {
        final double ax;
        final double ay;
        final double az;
        final double dx;
        final double dy;
        final double dz;
        final double lengthSq;
        final double ra;
        final double rb;
        double minX;
        double maxX;
        double minY;
        double maxY;
        double minZ;
        double maxZ;

        Bone(double ax, double ay, double az, double bx, double by, double bz, double ra, double rb) {
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.dx = bx - ax;
            this.dy = by - ay;
            this.dz = bz - az;
            this.lengthSq = this.dx * this.dx + this.dy * this.dy + this.dz * this.dz;
            this.ra = ra;
            this.rb = rb;
            double reach = Math.max(ra, rb) + 1.0D;
            this.minX = Math.min(ax, bx) - reach;
            this.maxX = Math.max(ax, bx) + reach;
            this.minY = Math.min(ay, by) - reach;
            this.maxY = Math.max(ay, by) + reach;
            this.minZ = Math.min(az, bz) - reach;
            this.maxZ = Math.max(az, bz) + reach;
        }

        double distance(double px, double py, double pz) {
            double rx = px - this.ax;
            double ry = py - this.ay;
            double rz = pz - this.az;
            double t = this.lengthSq > 0.0D ? Mth.clamp((rx * this.dx + ry * this.dy + rz * this.dz) / this.lengthSq, 0.0D, 1.0D) : 0.0D;
            double cx = rx - this.dx * t;
            double cy = ry - this.dy * t;
            double cz = rz - this.dz * t;
            return Math.sqrt(cx * cx + cy * cy + cz * cz) - Mth.lerp(t, this.ra, this.rb);
        }
    }

    private static final class Skull extends Bone {
        final double cx;
        final double cy;
        final double cz;
        final double size;
        final double fx;
        final double fz;

        Skull(double cx, double cy, double cz, double heading, double size) {
            super(cx, cy, cz, cx, cy, cz, size, size);
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.size = size;
            this.fx = -Math.cos(heading);
            this.fz = -Math.sin(heading);
            double reach = size * 1.3D + 1.0D;
            this.minX = cx - reach;
            this.maxX = cx + reach;
            this.minY = cy - size;
            this.maxY = cy + size;
            this.minZ = cz - reach;
            this.maxZ = cz + reach;
        }

        @Override
        double distance(double px, double py, double pz) {
            double rx = px - this.cx;
            double ry = py - this.cy;
            double rz = pz - this.cz;
            double forward = rx * this.fx + rz * this.fz;
            double side = rx * -this.fz + rz * this.fx;
            double nf = forward / (this.size * 1.3D);
            double ns = side / (this.size * 0.95D);
            double nu = ry / (this.size * 0.8D);
            double shell = (Math.sqrt(nf * nf + ns * ns + nu * nu) - 1.0D) * this.size * 0.8D;
            double hollow = -(shell + 3.0D);
            double socket = Double.MAX_VALUE;
            for (int s = -1; s <= 1; s += 2) {
                double ex = forward - this.size * 0.75D;
                double ey = ry - this.size * 0.15D;
                double ez = side - s * this.size * 0.42D;
                socket = Math.min(socket, Math.sqrt(ex * ex + ey * ey + ez * ez) - this.size * 0.32D);
            }
            return Math.max(Math.max(shell, hollow), -socket);
        }
    }

    private static final class Trench {
        final double ax;
        final double ay;
        final double az;
        final double by;
        final double dx;
        final double dz;
        final double lengthSq;
        final double halfWidth;
        final double height;
        final double capY;
        final double minX;
        final double maxX;
        final double minZ;
        final double maxZ;

        Trench(double ax, double ay, double az, double bx, double by, double bz, double halfWidth, double height, double capY) {
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.by = by;
            this.dx = bx - ax;
            this.dz = bz - az;
            this.lengthSq = this.dx * this.dx + this.dz * this.dz;
            this.halfWidth = halfWidth;
            this.height = height;
            this.capY = capY;
            double reach = halfWidth * 1.1D + Math.max(0.0D, 384.0D - capY) / CAP_SLOPE;
            this.minX = Math.min(ax, bx) - reach;
            this.maxX = Math.max(ax, bx) + reach;
            this.minZ = Math.min(az, bz) - reach;
            this.maxZ = Math.max(az, bz) + reach;
        }

        double distance(double px, double py, double pz) {
            double rx = px - this.ax;
            double rz = pz - this.az;
            double t = this.lengthSq > 0.0D ? Mth.clamp((rx * this.dx + rz * this.dz) / this.lengthSq, 0.0D, 1.0D) : 0.0D;
            double cx = rx - this.dx * t;
            double cz = rz - this.dz * t;
            double horizontal = Math.sqrt(cx * cx + cz * cz);
            double floor = Mth.lerp(t, this.ay, this.by);
            double halfHeight = this.height * 0.5D;
            double nx = horizontal / this.halfWidth;
            double ny = (py - floor - halfHeight) / halfHeight;
            double hollow = (Math.sqrt(nx * nx + ny * ny) - 1.0D) * Math.min(this.halfWidth, halfHeight);
            double cap = this.capY + CAP_SLOPE * Math.max(0.0D, horizontal - this.halfWidth * 1.1D) - py;
            return Math.min(hollow, cap);
        }
    }
}
