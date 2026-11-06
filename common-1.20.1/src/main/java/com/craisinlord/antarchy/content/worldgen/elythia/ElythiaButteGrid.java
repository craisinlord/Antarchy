package com.craisinlord.antarchy.content.worldgen.elythia;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public final class ElythiaButteGrid {
    public static final int CELL_SIZE = 320;
    static final double BLOCKS_PER_OFFSET = 63.4325D;
    private static final int MAX_CACHED_CELLS = 8192;
    private static final int REACH = 104;
    private static final double PROXIMITY_RANGE = 128.0D;
    private static final int PROXIMITY_REACH = 224;
    private static final double TIER_WALL = 6.0D;
    private static final double TALUS = 16.0D;
    private static final double TALUS_SHARE = 0.16D;
    private static final double DOME = 2.5D;
    private static final double CASTLE_CHANCE = 0.35D;
    private static final double BUTTE_CHANCE = 0.45D;
    private static final double CENTER_MASK = 0.9D;
    private static final double RIM_MASK = 0.6D;
    private static final int RIM_SAMPLES = 8;
    private static final double RIM_MARGIN = 24.0D;
    private static final Butte NONE = new Butte(0, 0, 0, 0, 0, 0, 0, 0, new double[0], new double[0], 0, 0, 0, 0, 0);

    private static final Map<Long, WeakReference<ElythiaButteGrid>> BY_SALT = new ConcurrentHashMap<>();

    private final long salt;
    private final DensityFunction ground;
    private final DensityFunction mask;
    private final Map<Long, Butte> cells = new ConcurrentHashMap<>();

    private ElythiaButteGrid(long salt, DensityFunction ground, DensityFunction mask) {
        this.salt = salt;
        this.ground = ground;
        this.mask = mask;
    }

    public static ElythiaButteGrid create(long salt, DensityFunction ground, DensityFunction mask) {
        WeakReference<ElythiaButteGrid> reference = BY_SALT.get(salt);
        ElythiaButteGrid existing = reference != null ? reference.get() : null;
        if (existing != null) {
            return existing;
        }
        BY_SALT.values().removeIf(entry -> entry.get() == null);
        ElythiaButteGrid grid = new ElythiaButteGrid(salt, ground, mask);
        BY_SALT.put(salt, new WeakReference<>(grid));
        return grid;
    }

    public double offset(int x, int z, double offset) {
        double best = offset;
        int minX = Math.floorDiv(x - REACH, CELL_SIZE);
        int maxX = Math.floorDiv(x + REACH, CELL_SIZE);
        int minZ = Math.floorDiv(z - REACH, CELL_SIZE);
        int maxZ = Math.floorDiv(z + REACH, CELL_SIZE);
        for (int cellX = minX; cellX <= maxX; cellX++) {
            for (int cellZ = minZ; cellZ <= maxZ; cellZ++) {
                Butte butte = this.cell(cellX, cellZ);
                if (butte != NONE) {
                    best = Math.max(best, butte.offset(x + 0.5D, z + 0.5D, offset));
                }
            }
        }
        return best;
    }

    public double presence(int x, int z) {
        double best = 0.0D;
        int minX = Math.floorDiv(x - REACH, CELL_SIZE);
        int maxX = Math.floorDiv(x + REACH, CELL_SIZE);
        int minZ = Math.floorDiv(z - REACH, CELL_SIZE);
        int maxZ = Math.floorDiv(z + REACH, CELL_SIZE);
        for (int cellX = minX; cellX <= maxX; cellX++) {
            for (int cellZ = minZ; cellZ <= maxZ; cellZ++) {
                Butte butte = this.cell(cellX, cellZ);
                if (butte != NONE) {
                    best = Math.max(best, butte.presence(x + 0.5D, z + 0.5D));
                }
            }
        }
        return best;
    }

    public double proximity(int x, int z) {
        double best = 0.0D;
        int minX = Math.floorDiv(x - PROXIMITY_REACH, CELL_SIZE);
        int maxX = Math.floorDiv(x + PROXIMITY_REACH, CELL_SIZE);
        int minZ = Math.floorDiv(z - PROXIMITY_REACH, CELL_SIZE);
        int maxZ = Math.floorDiv(z + PROXIMITY_REACH, CELL_SIZE);
        for (int cellX = minX; cellX <= maxX; cellX++) {
            for (int cellZ = minZ; cellZ <= maxZ; cellZ++) {
                Butte butte = this.cell(cellX, cellZ);
                if (butte != NONE) {
                    best = Math.max(best, butte.proximity(x + 0.5D, z + 0.5D));
                }
            }
        }
        return best;
    }

    private Butte cell(int cellX, int cellZ) {
        long key = (long) cellX << 32 | (cellZ & 0xFFFFFFFFL);
        Butte cached = this.cells.get(key);
        if (cached != null) {
            return cached;
        }
        if (this.cells.size() > MAX_CACHED_CELLS) {
            this.cells.clear();
        }
        Butte created = this.build(cellX, cellZ);
        Butte previous = this.cells.putIfAbsent(key, created);
        return previous != null ? previous : created;
    }

    private Butte build(int cellX, int cellZ) {
        XoroshiroRandomSource random = new XoroshiroRandomSource(this.salt ^ (cellX * 0x9E3779B97F4A7C15L), cellZ * 0xC2B2AE3D27D4EB4FL + 0x165667B19E3779F9L);
        boolean castle = random.nextDouble() < CASTLE_CHANCE;
        if (!castle && random.nextDouble() >= BUTTE_CHANCE) {
            return NONE;
        }
        double centerX;
        double centerZ;
        double radius;
        double height;
        if (castle) {
            centerX = (double) cellX * CELL_SIZE;
            centerZ = (double) cellZ * CELL_SIZE;
            radius = 67.0D + random.nextDouble() * 5.0D;
            height = 70.0D + random.nextDouble() * 25.0D;
        } else {
            centerX = (double) cellX * CELL_SIZE + 100.0D + random.nextDouble() * 120.0D;
            centerZ = (double) cellZ * CELL_SIZE + 100.0D + random.nextDouble() * 120.0D;
            radius = 22.0D + random.nextDouble() * 18.0D;
            height = 60.0D + random.nextDouble() * 50.0D;
        }
        if (this.maskAt(centerX, centerZ) < CENTER_MASK) {
            return NONE;
        }
        double rim = radius * 1.2D + RIM_MARGIN;
        for (int i = 0; i < RIM_SAMPLES; i++) {
            double angle = Math.PI * 2.0D * i / RIM_SAMPLES;
            if (this.maskAt(centerX + Math.cos(angle) * rim, centerZ + Math.sin(angle) * rim) < RIM_MASK) {
                return NONE;
            }
        }
        double wobble = castle ? 0.25D : 1.0D;
        double[] amplitudes = {0.09D * wobble, 0.05D * wobble, 0.025D * wobble, 0.03D * wobble, 0.015D * wobble};
        double[] phases = new double[amplitudes.length];
        for (int i = 0; i < phases.length; i++) {
            phases[i] = random.nextDouble() * Math.PI * 2.0D;
        }
        double wallWidth = castle ? 10.0D : 10.0D + random.nextDouble() * 6.0D;
        int strata = 3 + random.nextInt(3);
        double relief = castle ? 0.25D : 0.55D;
        double tierRadius = 0.0D;
        double tierX = centerX;
        double tierZ = centerZ;
        double tierHeight = 0.0D;
        if (!castle && random.nextDouble() < 0.35D) {
            tierRadius = radius * (0.4D + random.nextDouble() * 0.15D);
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double shift = radius * 0.3D * random.nextDouble();
            tierX = centerX + Math.cos(angle) * shift;
            tierZ = centerZ + Math.sin(angle) * shift;
            tierHeight = height * (0.2D + random.nextDouble() * 0.15D);
        }
        double ground = this.ground.compute(new DensityFunction.SinglePointContext(Mth.floor(centerX), 0, Mth.floor(centerZ)));
        return new Butte(centerX, centerZ, radius, height, ground, wallWidth, strata, relief, amplitudes, phases, tierRadius, tierX, tierZ, tierHeight, random.nextDouble() * Math.PI * 2.0D);
    }

    private double maskAt(double x, double z) {
        return this.mask.compute(new DensityFunction.SinglePointContext(Mth.floor(x), 0, Mth.floor(z)));
    }

    private record Butte(
            double centerX,
            double centerZ,
            double radius,
            double height,
            double ground,
            double wallWidth,
            int strata,
            double relief,
            double[] amplitudes,
            double[] phases,
            double tierRadius,
            double tierX,
            double tierZ,
            double tierHeight,
            double tierPhase
    ) {
        private static final int[] HARMONICS = {3, 5, 8, 13, 21};

        double edge(double angle, double radius, double phaseShift) {
            double scale = 1.0D;
            for (int i = 0; i < HARMONICS.length; i++) {
                scale += this.amplitudes[i] * Math.sin(HARMONICS[i] * angle + this.phases[i] + phaseShift);
            }
            return radius * scale;
        }

        double offset(double x, double z, double offset) {
            double dx = x - this.centerX;
            double dz = z - this.centerZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > this.radius * 1.25D + TALUS) {
                return offset;
            }
            double edge = this.edge(Math.atan2(dz, dx), this.radius, 0.0D);
            double talus = Mth.clamp(1.0D - (distance - edge) / TALUS, 0.0D, 1.0D);
            double result = offset + TALUS_SHARE * this.height * talus * talus / BLOCKS_PER_OFFSET;
            double fill = Mth.clamp((edge - distance) / this.wallWidth, 0.0D, 1.0D);
            if (fill > 0.0D) {
                double plateau = Math.max(edge - this.wallWidth, 1.0D);
                double ratio = Math.min(1.0D, distance / plateau);
                double body = this.height * this.profile(fill) + DOME * (1.0D - ratio * ratio) * fill;
                if (this.tierHeight > 0.0D) {
                    double tx = x - this.tierX;
                    double tz = z - this.tierZ;
                    double tierDistance = Math.sqrt(tx * tx + tz * tz);
                    double tierEdge = this.edge(Math.atan2(tz, tx), this.tierRadius, this.tierPhase);
                    body += this.tierHeight * Mth.smoothstep(Mth.clamp((tierEdge - tierDistance) / TIER_WALL, 0.0D, 1.0D));
                }
                double top = this.ground + body / BLOCKS_PER_OFFSET + this.relief * (offset - this.ground) * fill;
                result = Math.max(result, top);
            }
            return result;
        }

        double profile(double fill) {
            double shape = 0.15D * fill + 0.85D * fill * fill * fill;
            double t = shape * this.strata;
            double band = Math.floor(t);
            if (band >= this.strata) {
                return 1.0D;
            }
            double fraction = t - band;
            double rise = fraction < 0.55D ? 0.3D * fraction / 0.55D : 0.3D + 0.7D * Mth.smoothstep((fraction - 0.55D) / 0.45D);
            return (band + rise) / this.strata;
        }

        double proximity(double x, double z) {
            double dx = x - this.centerX;
            double dz = z - this.centerZ;
            double distance = Math.sqrt(dx * dx + dz * dz) - this.radius * 1.2D;
            return Mth.clamp(1.0D - distance / PROXIMITY_RANGE, 0.0D, 1.0D);
        }

        double presence(double x, double z) {
            double dx = x - this.centerX;
            double dz = z - this.centerZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > this.radius * 1.25D + TALUS) {
                return 0.0D;
            }
            double edge = this.edge(Math.atan2(dz, dx), this.radius, 0.0D);
            return Mth.clamp((edge + TALUS * 0.5D - distance) / 4.0D, 0.0D, 1.0D);
        }
    }
}
