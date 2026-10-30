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
    private static final float SKELETON_CHANCE = 0.45F;
    private static final double SEGMENT_LENGTH = 12.0;
    private static final double RIB_SPACING = 10.0;
    private static final int RIB_SEGMENTS = 9;
    private static final double CAP_SLOPE = 1.4;

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
        double py = y + 0.5;
        if (py < cache.boneMinY || py > cache.boneMaxY) {
            return Double.MAX_VALUE;
        }
        double px = x + 0.5;
        double pz = z + 0.5;
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
        double py = y + 0.5;
        if (cache.trenches.length == 0 || py < cache.trenchMinY) {
            return Double.MAX_VALUE;
        }
        double px = x + 0.5;
        double pz = z + 0.5;
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
        double minX = qx * 4.0;
        double minZ = qz * 4.0;
        double maxX = minX + 4.0;
        double maxZ = minZ + 4.0;
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
        double x = cellX * (double) CELL_SIZE + CELL_SIZE * (0.25 + random.nextDouble() * 0.5);
        double z = cellZ * (double) CELL_SIZE + CELL_SIZE * (0.25 + random.nextDouble() * 0.5);
        double y = 238.0 + random.nextDouble() * 10.0;
        double yaw = random.nextDouble() * Math.PI * 2.0;
        double length = 150.0 + random.nextDouble() * 90.0;
        double ribWidth = 18.0 + random.nextDouble() * 8.0;
        double ribHeight = 30.0 + random.nextDouble() * 10.0;
        double sweep = 0.15 + random.nextDouble() * 0.2;
        if (!this.filter.accepts(x, z)) {
            return new Skeleton[0];
        }
        double inside = 0.0;
        while (inside < length && this.filter.accepts(x + Math.cos(yaw) * (inside + 40.0), z + Math.sin(yaw) * (inside + 40.0))) {
            inside += 20.0;
        }
        if (inside < 100.0) {
            return new Skeleton[0];
        }
        return new Skeleton[]{new Builder(random).build(x, y, z, yaw, Math.min(length, inside), ribWidth, ribHeight, sweep)};
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
                heading += this.random.nextGaussian() * 0.12;
                double ny = y + Math.sin(i * 0.45) * 1.5;
                x += Math.cos(heading) * SEGMENT_LENGTH;
                z += Math.sin(heading) * SEGMENT_LENGTH;
                spine[i] = new double[]{x, ny, z, heading};
            }
            for (int i = 0; i < segments; i++) {
                double s = i / (double) segments;
                double[] a = spine[i];
                double[] b = spine[i + 1];
                double radius = 4.2 * (1.0 - 0.55 * s);
                this.bones.add(new Bone(a[0], a[1], a[2], b[0], b[1], b[2], radius, 4.2 * (1.0 - 0.55 * (i + 1) / (double) segments)));
                double envelope = 0.55 + 0.45 * Math.pow(Math.sin(Math.PI * Math.min(1.0, s / 0.8)), 0.7);
                this.trenches.add(new Trench(a[0], a[1] - 1.0, a[2], b[0], b[1] - 1.0, b[2],
                        ribWidth * envelope * 1.3, ribHeight * envelope * 2.3, a[1] + ribHeight * envelope * 1.85));
            }
            for (double along = 0.0; along < length; along += 5.0) {
                double[] point = this.at(spine, along / SEGMENT_LENGTH);
                double knob = 5.0 * (1.0 - 0.55 * along / length);
                this.bones.add(new Bone(point[0], point[1] + 1.5, point[2], point[0], point[1] + 1.5 + knob * 0.6, point[2], knob, knob * 0.6));
            }
            double ribStart = length * 0.1;
            double ribEnd = length * 0.82;
            for (double along = ribStart; along < ribEnd; along += RIB_SPACING + this.random.nextDouble() * 2.0) {
                double s = (along - ribStart) / (ribEnd - ribStart);
                double scale = 0.55 + 0.45 * Math.pow(Math.sin(Math.PI * s), 0.7);
                double[] point = this.at(spine, along / SEGMENT_LENGTH);
                for (int side = -1; side <= 1; side += 2) {
                    this.rib(point, side, ribWidth * scale * (0.92 + this.random.nextDouble() * 0.16), ribHeight * scale, sweep);
                }
            }
            double[] head = spine[0];
            double hx = head[0] - Math.cos(head[3]) * 10.0;
            double hz = head[2] - Math.sin(head[3]) * 10.0;
            this.bones.add(new Skull(hx, head[1] + 6.0, hz, head[3], 13.0 + this.random.nextDouble() * 3.0));
            this.trenches.add(new Trench(head[0], head[1] - 1.0, head[2], hx - Math.cos(head[3]) * 8.0, head[1] - 1.0, hz - Math.sin(head[3]) * 8.0,
                    ribWidth * 0.7, ribHeight, head[1] + ribHeight * 1.3));
            return new Skeleton(this.bones.toArray(new Bone[0]), this.trenches.toArray(new Trench[0]));
        }

        private double[] at(double[][] spine, double index) {
            int i = Mth.clamp((int) Math.floor(index), 0, spine.length - 2);
            double f = Mth.clamp(index - i, 0.0, 1.0);
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
            double maxAngle = Math.toRadians(162.0 + this.random.nextDouble() * 12.0);
            double px = base[0];
            double py = base[1];
            double pz = base[2];
            for (int i = 1; i <= RIB_SEGMENTS; i++) {
                double phi = maxAngle * i / RIB_SEGMENTS;
                double out = width * Math.sin(phi);
                double up = height * (1.0 - Math.cos(phi));
                double back = -sweep * up;
                double qx = base[0] + nx * out + tx * back;
                double qy = base[1] + up;
                double qz = base[2] + nz * out + tz * back;
                double t0 = (i - 1) / (double) RIB_SEGMENTS;
                double t1 = i / (double) RIB_SEGMENTS;
                this.bones.add(new Bone(px, py, pz, qx, qy, qz, 3.4 - 1.9 * t0, 3.4 - 1.9 * t1));
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
            double reach = Math.max(ra, rb) + 1.0;
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
            double t = this.lengthSq > 0.0 ? Mth.clamp((rx * this.dx + ry * this.dy + rz * this.dz) / this.lengthSq, 0.0, 1.0) : 0.0;
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
            double reach = size * 1.3 + 1.0;
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
            double nf = forward / (this.size * 1.3);
            double ns = side / (this.size * 0.95);
            double nu = ry / (this.size * 0.8);
            double shell = (Math.sqrt(nf * nf + ns * ns + nu * nu) - 1.0) * this.size * 0.8;
            double hollow = -(shell + 3.0);
            double socket = Double.MAX_VALUE;
            for (int s = -1; s <= 1; s += 2) {
                double ex = forward - this.size * 0.75;
                double ey = ry - this.size * 0.15;
                double ez = side - s * this.size * 0.42;
                socket = Math.min(socket, Math.sqrt(ex * ex + ey * ey + ez * ez) - this.size * 0.32);
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
            double reach = halfWidth * 1.1 + Math.max(0.0, 384.0 - capY) / CAP_SLOPE;
            this.minX = Math.min(ax, bx) - reach;
            this.maxX = Math.max(ax, bx) + reach;
            this.minZ = Math.min(az, bz) - reach;
            this.maxZ = Math.max(az, bz) + reach;
        }

        double distance(double px, double py, double pz) {
            double rx = px - this.ax;
            double rz = pz - this.az;
            double t = this.lengthSq > 0.0 ? Mth.clamp((rx * this.dx + rz * this.dz) / this.lengthSq, 0.0, 1.0) : 0.0;
            double cx = rx - this.dx * t;
            double cz = rz - this.dz * t;
            double horizontal = Math.sqrt(cx * cx + cz * cz);
            double floor = Mth.lerp(t, this.ay, this.by);
            double halfHeight = this.height * 0.5;
            double nx = horizontal / this.halfWidth;
            double ny = (py - floor - halfHeight) / halfHeight;
            double hollow = (Math.sqrt(nx * nx + ny * ny) - 1.0) * Math.min(this.halfWidth, halfHeight);
            double cap = this.capY + CAP_SLOPE * Math.max(0.0, horizontal - this.halfWidth * 1.1) - py;
            return Math.min(hollow, cap);
        }
    }
}
