package com.craisinlord.antarchy.content.worldgen.cavaryn;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public final class CavarynSpireGrid {
    public static final int CELL_SIZE = 128;
    private static final int MAX_CACHED_CELLS = 4096;
    private static final double BASE_Y = 214.0;
    private static final double MAX_TOP = 378.0;
    private static final float FOREST_CHANCE = 0.4F;
    private static final float LONE_CHANCE = 0.15F;
    private static final float FUSE_CHANCE = 0.3F;
    private static final double STRIPE_PITCH = 11.0;

    private static final Map<Long, WeakReference<CavarynSpireGrid>> BY_SALT = new ConcurrentHashMap<>();

    private final long salt;
    private final CavarynSurfaceMask.Filter filter;
    private final Map<Long, Spire[]> cells = new ConcurrentHashMap<>();
    private final ThreadLocal<ColumnCache> columns = ThreadLocal.withInitial(ColumnCache::new);

    private CavarynSpireGrid(long salt, CavarynSurfaceMask.Filter filter) {
        this.salt = salt;
        this.filter = filter;
    }

    public static CavarynSpireGrid create(long salt, CavarynSurfaceMask.Filter filter) {
        CavarynSpireGrid existing = forSalt(salt);
        if (existing != null) {
            return existing;
        }
        BY_SALT.values().removeIf(reference -> reference.get() == null);
        CavarynSpireGrid grid = new CavarynSpireGrid(salt, filter);
        BY_SALT.put(salt, new WeakReference<>(grid));
        return grid;
    }

    public static CavarynSpireGrid forSalt(long salt) {
        WeakReference<CavarynSpireGrid> reference = BY_SALT.get(salt);
        return reference != null ? reference.get() : null;
    }

    public double signedDistance(int x, int y, int z) {
        ColumnCache cache = this.candidates(x, z);
        double py = y + 0.5;
        if (py < cache.minY || py > cache.maxY) {
            return Double.MAX_VALUE;
        }
        double best = Double.MAX_VALUE;
        for (Spire spire : cache.spires) {
            double value = spire.signedDistance(x + 0.5, py, z + 0.5);
            if (value < best) {
                best = value;
            }
        }
        return best;
    }

    public boolean onStripe(int x, int y, int z, double shell) {
        ColumnCache cache = this.candidates(x, z);
        double py = y + 0.5;
        if (py < cache.minY || py > cache.maxY) {
            return false;
        }
        Spire nearest = null;
        double best = Double.MAX_VALUE;
        for (Spire spire : cache.spires) {
            double value = spire.signedDistance(x + 0.5, py, z + 0.5);
            if (value < best) {
                best = value;
                nearest = spire;
            }
        }
        if (nearest == null || best > 0.0 || best < -shell) {
            return false;
        }
        return nearest.stripe(x + 0.5, py, z + 0.5);
    }

    public int[] verticalRange(int x, int z) {
        ColumnCache cache = this.candidates(x, z);
        if (cache.spires.length == 0) {
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
        List<Spire> found = new ArrayList<>();
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (Spire spire : this.spires(cellX + dx, cellZ + dz)) {
                    if (spire.maxX < minX || spire.minX > maxX || spire.maxZ < minZ || spire.minZ > maxZ) {
                        continue;
                    }
                    found.add(spire);
                    minY = Math.min(minY, spire.baseY);
                    maxY = Math.max(maxY, spire.topY);
                }
            }
        }
        cache.qx = qx;
        cache.qz = qz;
        cache.spires = found.toArray(new Spire[0]);
        cache.minY = minY;
        cache.maxY = maxY;
        cache.valid = true;
        return cache;
    }

    private Spire[] spires(int cellX, int cellZ) {
        long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
        Spire[] cached = this.cells.get(key);
        if (cached != null) {
            return cached;
        }
        Spire[] generated = this.generate(cellX, cellZ);
        CavarynCavityGrid.trim(this.cells, MAX_CACHED_CELLS);
        Spire[] existing = this.cells.putIfAbsent(key, generated);
        return existing != null ? existing : generated;
    }

    private Spire[] generate(int cellX, int cellZ) {
        XoroshiroRandomSource random = new XoroshiroRandomSource(mix(this.salt ^ (cellX * 0x9E3779B97F4A7C15L) ^ (cellZ * 0xC2B2AE3D27D4EB4FL)), ~this.salt);
        int count;
        boolean forest = random.nextFloat() < FOREST_CHANCE;
        if (forest) {
            count = 4 + random.nextInt(4);
        } else {
            count = random.nextFloat() < LONE_CHANCE ? 1 : 0;
        }
        double centerX = cellX * (double) CELL_SIZE + CELL_SIZE * (0.3 + random.nextDouble() * 0.4);
        double centerZ = cellZ * (double) CELL_SIZE + CELL_SIZE * (0.3 + random.nextDouble() * 0.4);
        if (count > 0 && !this.filter.accepts(centerX, centerZ)) {
            return new Spire[0];
        }
        double spread = forest ? 30.0 + random.nextDouble() * 14.0 : 0.0;
        List<Spire> spires = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = spread * Math.sqrt(random.nextDouble());
            double bx = centerX + Math.cos(angle) * distance;
            double bz = centerZ + Math.sin(angle) * distance;
            if (!this.filter.accepts(bx, bz)) {
                continue;
            }
            double height = 120.0 + random.nextDouble() * 45.0;
            double top = Math.min(MAX_TOP, BASE_Y + height);
            double radius = 9.0 + random.nextDouble() * 6.0;
            int lobes = random.nextFloat() < 0.6F ? 3 : 2;
            double twist = (1.5 + random.nextDouble() * 2.0) * Math.PI * 2.0 / (top - BASE_Y);
            if (random.nextBoolean()) {
                twist = -twist;
            }
            double lobeAmount = 0.3 + random.nextDouble() * 0.25;
            double leanAngle = random.nextDouble() * Math.PI * 2.0;
            double leanAmount = random.nextDouble() * 22.0;
            double leanX = Math.cos(leanAngle) * leanAmount;
            double leanZ = Math.sin(leanAngle) * leanAmount;
            if (!spires.isEmpty() && random.nextFloat() < FUSE_CHANCE) {
                Spire other = spires.get(random.nextInt(spires.size()));
                double meetX = other.axisX(other.topY - 6.0);
                double meetZ = other.axisZ(other.topY - 6.0);
                leanX = (meetX - bx) * 0.95;
                leanZ = (meetZ - bz) * 0.95;
                top = Math.min(MAX_TOP, other.topY - 2.0);
            }
            double swayAmount = 1.5 + random.nextDouble() * 2.5;
            double swayPhase = random.nextDouble() * Math.PI * 2.0;
            double lobePhase = random.nextDouble() * Math.PI * 2.0;
            spires.add(new Spire(bx, bz, top, radius, lobes, twist, lobeAmount, leanX, leanZ, swayAmount, swayPhase, lobePhase));
        }
        return spires.toArray(new Spire[0]);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static final class ColumnCache {
        boolean valid;
        int qx;
        int qz;
        Spire[] spires = new Spire[0];
        double minY;
        double maxY;
    }

    private static final class Spire {
        final double baseX;
        final double baseZ;
        final double baseY;
        final double topY;
        final double height;
        final double radius;
        final int lobes;
        final double twist;
        final double lobeAmount;
        final double leanX;
        final double leanZ;
        final double swayAmount;
        final double swayPhase;
        final double lobePhase;
        final double minX;
        final double maxX;
        final double minZ;
        final double maxZ;

        Spire(double baseX, double baseZ, double topY, double radius, int lobes, double twist, double lobeAmount,
              double leanX, double leanZ, double swayAmount, double swayPhase, double lobePhase) {
            this.baseX = baseX;
            this.baseZ = baseZ;
            this.baseY = BASE_Y;
            this.topY = topY;
            this.height = topY - BASE_Y;
            this.radius = radius;
            this.lobes = lobes;
            this.twist = twist;
            this.lobeAmount = lobeAmount;
            this.leanX = leanX;
            this.leanZ = leanZ;
            this.swayAmount = swayAmount;
            this.swayPhase = swayPhase;
            this.lobePhase = lobePhase;
            double reach = radius * 1.7 * (1.0 + lobeAmount) + swayAmount + 2.0;
            this.minX = Math.min(baseX, baseX + leanX) - reach;
            this.maxX = Math.max(baseX, baseX + leanX) + reach;
            this.minZ = Math.min(baseZ, baseZ + leanZ) - reach;
            this.maxZ = Math.max(baseZ, baseZ + leanZ) + reach;
        }

        double progress(double y) {
            return (y - this.baseY) / this.height;
        }

        double axisX(double y) {
            double t = this.progress(y);
            return this.baseX + this.leanX * Math.pow(Math.max(0.0, t), 1.6) + this.swayAmount * Math.sin(t * Math.PI * 3.0 + this.swayPhase);
        }

        double axisZ(double y) {
            double t = this.progress(y);
            return this.baseZ + this.leanZ * Math.pow(Math.max(0.0, t), 1.6) + this.swayAmount * Math.cos(t * Math.PI * 3.0 + this.swayPhase);
        }

        double radiusAt(double t) {
            double taper = this.radius * (1.0 - 0.78 * Math.pow(t, 2.2));
            if (t < 0.12) {
                double flare = 1.0 - t / 0.12;
                taper *= 1.0 + 0.7 * flare * flare;
            }
            return Math.max(1.2, taper);
        }

        double signedDistance(double px, double py, double pz) {
            if (py < this.baseY || py > this.topY) {
                return Double.MAX_VALUE;
            }
            double t = this.progress(py);
            double dx = px - this.axisX(py);
            double dz = pz - this.axisZ(py);
            double distance = Math.sqrt(dx * dx + dz * dz);
            double theta = Math.atan2(dz, dx);
            double lobe = 1.0 + this.lobeAmount * Math.cos(this.lobes * (theta - this.twist * (py - this.baseY)) + this.lobePhase);
            double boundary = this.radiusAt(t) * lobe;
            double tip = this.topY - py;
            double inside = distance - boundary;
            if (tip < 3.0) {
                inside += (3.0 - tip) * 1.2;
            }
            return inside;
        }

        boolean stripe(double px, double py, double pz) {
            double dx = px - this.axisX(py);
            double dz = pz - this.axisZ(py);
            double theta = Math.atan2(dz, dx);
            double phase = (py - this.baseY) - (theta - this.twist * (py - this.baseY)) * STRIPE_PITCH / (Math.PI * 2.0) * this.lobes;
            return Math.floorMod((long) Math.floor(phase), (long) STRIPE_PITCH) < 4L;
        }
    }
}
