package com.craisinlord.antarchy.content.worldgen.cavaryn;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public final class CavarynWormBoreGrid {
    public static final int CELL_SIZE = 160;
    private static final int CELL_REACH = 4;
    private static final int MAX_CACHED_CELLS = 16384;
    private static final double MIN_Y = 26.0;
    private static final double MAX_Y = 150.0;
    private static final double START_MIN_Y = 36.0;
    private static final double START_MAX_Y = 92.0;
    private static final double RIB_SPACING = 5.5;
    private static final double RIB_DEPTH = 0.9;
    private static final double RIB_HALF_WIDTH = 0.12;
    private static final double SWELL_PERIOD = 40.0;
    private static final double SWELL_AMOUNT = 0.1;
    private static final double MAX_RADIUS = 12.5;

    private static final Map<Long, WeakReference<CavarynWormBoreGrid>> BY_SALT = new ConcurrentHashMap<>();

    private final long salt;
    private final StartFilter startFilter;
    private final Map<Long, Path[]> cells = new ConcurrentHashMap<>();
    private final ThreadLocal<ColumnCache> columns = ThreadLocal.withInitial(ColumnCache::new);

    private CavarynWormBoreGrid(long salt, StartFilter startFilter) {
        this.salt = salt;
        this.startFilter = startFilter;
    }

    public static CavarynWormBoreGrid create(long salt, StartFilter startFilter) {
        CavarynWormBoreGrid existing = forSalt(salt);
        if (existing != null) {
            return existing;
        }
        BY_SALT.values().removeIf(reference -> reference.get() == null);
        CavarynWormBoreGrid grid = new CavarynWormBoreGrid(salt, startFilter);
        BY_SALT.put(salt, new WeakReference<>(grid));
        return grid;
    }

    public static CavarynWormBoreGrid forSalt(long salt) {
        WeakReference<CavarynWormBoreGrid> reference = BY_SALT.get(salt);
        return reference != null ? reference.get() : null;
    }

    public static long salt(double first, double second) {
        long a = Double.doubleToLongBits(first);
        long b = Double.doubleToLongBits(second);
        return mix(a * 0x9E3779B97F4A7C15L ^ Long.rotateLeft(b, 29));
    }

    public double signedDistance(int x, int y, int z) {
        return this.signedDistance(x, y, z, -Double.MAX_VALUE);
    }

    public double signedDistance(int x, int y, int z, double floor) {
        ColumnCache cache = this.candidates(x, z);
        double py = y + 0.5;
        if (py < cache.minY || py > cache.maxY) {
            return Double.MAX_VALUE;
        }
        double best = Double.MAX_VALUE;
        double px = x + 0.5;
        double pz = z + 0.5;
        for (Segment segment : cache.segments) {
            if (py < segment.minY || py > segment.maxY) {
                continue;
            }
            double value = segment.signedDistance(px, py, pz);
            if (value < best) {
                best = value;
                if (best <= floor) {
                    return best;
                }
            }
        }
        return best;
    }

    public int[] verticalRange(int x, int z) {
        ColumnCache cache = this.candidates(x, z);
        if (cache.segments.length == 0) {
            return null;
        }
        return new int[]{Mth.floor(cache.minY), Mth.ceil(cache.maxY)};
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
        List<Segment> found = new ArrayList<>();
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int dx = -CELL_REACH; dx <= CELL_REACH; dx++) {
            for (int dz = -CELL_REACH; dz <= CELL_REACH; dz++) {
                for (Path path : this.paths(cellX + dx, cellZ + dz)) {
                    if (path.maxX < minX || path.minX > maxX || path.maxZ < minZ || path.minZ > maxZ) {
                        continue;
                    }
                    for (Segment segment : path.segments) {
                        if (segment.maxX < minX || segment.minX > maxX || segment.maxZ < minZ || segment.minZ > maxZ) {
                            continue;
                        }
                        found.add(segment);
                        minY = Math.min(minY, segment.minY);
                        maxY = Math.max(maxY, segment.maxY);
                    }
                }
            }
        }
        cache.qx = qx;
        cache.qz = qz;
        cache.segments = found.toArray(new Segment[0]);
        cache.minY = minY;
        cache.maxY = maxY;
        cache.valid = true;
        return cache;
    }

    private Path[] paths(int cellX, int cellZ) {
        long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
        Path[] cached = this.cells.get(key);
        if (cached != null) {
            return cached;
        }
        Path[] generated = this.generate(cellX, cellZ);
        CavarynCavityGrid.trim(this.cells, MAX_CACHED_CELLS);
        Path[] existing = this.cells.putIfAbsent(key, generated);
        return existing != null ? existing : generated;
    }

    private Path[] generate(int cellX, int cellZ) {
        XoroshiroRandomSource random = new XoroshiroRandomSource(mix(this.salt ^ (cellX * 0x632BE59BD9B4E019L) ^ (cellZ * 0x85157AF5L)), this.salt);
        float roll = random.nextFloat();
        int count = roll < 0.1F ? 2 : roll < 0.6F ? 3 : 4;
        List<Path> paths = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double[] start = null;
            for (int attempt = 0; attempt < 4 && start == null; attempt++) {
                double sx = cellX * (double) CELL_SIZE + 16.0 + random.nextDouble() * (CELL_SIZE - 32.0);
                double sz = cellZ * (double) CELL_SIZE + 16.0 + random.nextDouble() * (CELL_SIZE - 32.0);
                double sy = START_MIN_Y + random.nextDouble() * (START_MAX_Y - START_MIN_Y);
                if (this.startFilter.accepts(sx, sy, sz)) {
                    start = new double[]{sx, sy, sz};
                }
            }
            if (start != null) {
                paths.add(walk(random, start[0], start[1], start[2]));
            }
        }
        return paths.toArray(new Path[0]);
    }

    private static Path walk(XoroshiroRandomSource random, double x, double y, double z) {
        int segmentCount = 24 + random.nextInt(11);
        double baseRadius = 6.5 + random.nextDouble() * 2.0;
        double yaw = random.nextDouble() * Math.PI * 2.0;
        double pitch = (random.nextDouble() - 0.5) * 0.3;
        double phase = random.nextDouble() * Math.PI * 2.0;
        int coilStart = random.nextFloat() < 0.28F ? 4 + random.nextInt(Math.max(1, segmentCount - 8)) : -1;
        int coilLength = 10;
        double coilTurn = random.nextBoolean() ? 0.62 : -0.62;
        List<Segment> segments = new ArrayList<>(segmentCount + coilLength);
        double distance = 0.0;
        int total = coilStart >= 0 ? segmentCount + coilLength : segmentCount;
        for (int i = 0; i < total; i++) {
            boolean coiling = coilStart >= 0 && i >= coilStart && i < coilStart + coilLength;
            double length;
            double radius = baseRadius;
            if (coiling) {
                yaw += coilTurn;
                pitch = -0.12;
                length = 9.0;
                radius += 2.5;
            } else {
                yaw += random.nextGaussian() * 0.28;
                pitch = Mth.clamp(pitch * 0.75 + random.nextGaussian() * 0.1, -0.32, 0.32);
                length = 10.0 + random.nextDouble() * 5.0;
            }
            if (y < MIN_Y + 6.0) {
                pitch = Math.abs(pitch) + 0.05;
            } else if (y > MAX_Y - 6.0) {
                pitch = -Math.abs(pitch) - 0.05;
            }
            double horizontal = Math.cos(pitch) * length;
            double nx = x + Math.cos(yaw) * horizontal;
            double ny = Mth.clamp(y + Math.sin(pitch) * length, MIN_Y, MAX_Y);
            double nz = z + Math.sin(yaw) * horizontal;
            segments.add(new Segment(x, y, z, nx, ny, nz, distance, radius, phase));
            distance += Math.sqrt((nx - x) * (nx - x) + (ny - y) * (ny - y) + (nz - z) * (nz - z));
            x = nx;
            y = ny;
            z = nz;
        }
        return new Path(segments.toArray(new Segment[0]));
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    @FunctionalInterface
    public interface StartFilter {
        boolean accepts(double x, double y, double z);
    }

    private static final class ColumnCache {
        boolean valid;
        int qx;
        int qz;
        Segment[] segments = new Segment[0];
        double minY;
        double maxY;
    }

    private static final class Path {
        final Segment[] segments;
        final double minX;
        final double maxX;
        final double minZ;
        final double maxZ;

        Path(Segment[] segments) {
            this.segments = segments;
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            for (Segment segment : segments) {
                minX = Math.min(minX, segment.minX);
                maxX = Math.max(maxX, segment.maxX);
                minZ = Math.min(minZ, segment.minZ);
                maxZ = Math.max(maxZ, segment.maxZ);
            }
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }

    private static final class Segment {
        final double ax;
        final double ay;
        final double az;
        final double dx;
        final double dy;
        final double dz;
        final double lengthSq;
        final double length;
        final double start;
        final double radius;
        final double phase;
        final double minX;
        final double maxX;
        final double minY;
        final double maxY;
        final double minZ;
        final double maxZ;

        Segment(double ax, double ay, double az, double bx, double by, double bz, double start, double radius, double phase) {
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.dx = bx - ax;
            this.dy = by - ay;
            this.dz = bz - az;
            this.lengthSq = this.dx * this.dx + this.dy * this.dy + this.dz * this.dz;
            this.length = Math.sqrt(this.lengthSq);
            this.start = start;
            this.radius = radius;
            this.phase = phase;
            double reach = Math.min(MAX_RADIUS, radius * (1.0 + SWELL_AMOUNT)) + 2.5;
            this.minX = Math.min(ax, bx) - reach;
            this.maxX = Math.max(ax, bx) + reach;
            this.minY = Math.min(ay, by) - reach;
            this.maxY = Math.max(ay, by) + reach;
            this.minZ = Math.min(az, bz) - reach;
            this.maxZ = Math.max(az, bz) + reach;
        }

        double signedDistance(double px, double py, double pz) {
            double rx = px - this.ax;
            double ry = py - this.ay;
            double rz = pz - this.az;
            double t = this.lengthSq > 0.0 ? Mth.clamp((rx * this.dx + ry * this.dy + rz * this.dz) / this.lengthSq, 0.0, 1.0) : 0.0;
            double cx = rx - this.dx * t;
            double cy = ry - this.dy * t;
            double cz = rz - this.dz * t;
            double distance = Math.sqrt(cx * cx + cy * cy + cz * cz);
            double along = this.start + this.length * t;
            double swell = 1.0 + SWELL_AMOUNT * Math.sin(along * (Math.PI * 2.0 / SWELL_PERIOD) + this.phase);
            double ribPhase = along / RIB_SPACING;
            double ribOffset = Math.abs(ribPhase - Math.floor(ribPhase) - 0.5);
            double rib = Math.max(0.0, 1.0 - (0.5 - ribOffset) / RIB_HALF_WIDTH);
            double radius = Math.min(MAX_RADIUS, this.radius * swell) - RIB_DEPTH * rib;
            return distance - radius;
        }
    }
}
