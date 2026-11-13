package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWormBoreFunction;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public final class SkyIsleGrid {
    public static final int CELL_SIZE = 3520;
    public static final int CROWN_TOP_Y = 265;
    public static final int FLOOR_Y = 190;
    private static final int SATELLITE_MIN_TOP = 240;
    private static final int SATELLITE_MAX_TOP = 290;
    public static final int NO_HEIGHT = Integer.MIN_VALUE;
    public static final ResourceKey<NormalNoise.NoiseParameters> SEED_NOISE = ResourceKey.create(Registries.NOISE, Identifier.fromNamespaceAndPath(Antarchy.MODID, "elythia_sky_isle_seed"));
    private static final double CLUSTER_CHANCE = 0.75D;
    private static final double CENTER_MIN = 0.2D;
    private static final double CENTER_SPAN = 0.6D;
    private static final double SATELLITE_REACH = 340.0D;
    private static final double SATELLITE_GAP = 14.0D;
    private static final int PLACEMENT_ATTEMPTS = 12;
    private static final double CRATER_MARGIN = 4.0D;
    private static final double BASIN_RATIO = 0.55D;
    private static final double CHANNEL_HALF_WIDTH = 1.6D;
    private static final int MAX_CACHED_CELLS = 1024;
    private static final Cluster NONE = new Cluster(List.of(), 0, 0, 0, 0);

    private static final NormalNoise.NoiseParameters SEED_NOISE_PARAMETERS = new NormalNoise.NoiseParameters(-4, 1.0D);
    private static final Map<Long, WeakReference<SkyIsleGrid>> BY_SALT = new ConcurrentHashMap<>();
    @Nullable
    private static volatile SeededGrid lastSeeded;

    private final long salt;
    private final Map<Long, Cluster> cells = new ConcurrentHashMap<>();
    private volatile CellRef last = new CellRef(Integer.MIN_VALUE, Integer.MIN_VALUE, NONE);

    private SkyIsleGrid(long salt) {
        this.salt = salt;
    }

    public static SkyIsleGrid forSalt(long salt) {
        WeakReference<SkyIsleGrid> reference = BY_SALT.get(salt);
        SkyIsleGrid existing = reference != null ? reference.get() : null;
        if (existing != null) {
            return existing;
        }
        BY_SALT.values().removeIf(entry -> entry.get() == null);
        SkyIsleGrid grid = new SkyIsleGrid(salt);
        WeakReference<SkyIsleGrid> previous = BY_SALT.putIfAbsent(salt, new WeakReference<>(grid));
        SkyIsleGrid raced = previous != null ? previous.get() : null;
        return raced != null ? raced : grid;
    }

    public static SkyIsleGrid forNoise(NormalNoise noise) {
        return forSalt(CavarynWormBoreFunction.saltFor(noise.getValue(-271.913D, 61.337D, 503.177D), noise.getValue(887.251D, -39.613D, -142.809D)));
    }

    public static SkyIsleGrid forSeed(long seed) {
        SeededGrid last = lastSeeded;
        if (last != null && last.seed == seed) {
            return last.grid;
        }
        RandomSource random = WorldgenRandom.Algorithm.XOROSHIRO.newInstance(seed).forkPositional().fromHashOf(SEED_NOISE.identifier());
        SkyIsleGrid grid = forNoise(NormalNoise.create(random, SEED_NOISE_PARAMETERS));
        lastSeeded = new SeededGrid(seed, grid);
        return grid;
    }

    public static SkyIsleGrid forRandomState(RandomState randomState) {
        return forNoise(randomState.getOrCreateNoise(SEED_NOISE));
    }

    public static SkyIsleGrid forLevel(WorldGenLevel level) {
        return forRandomState(level.getLevel().getChunkSource().randomState());
    }

    public static final int FOOTPRINT_NONE = 0;
    public static final int FOOTPRINT_SKY = 1;
    public static final int FOOTPRINT_ROYAL = 2;

    public int footprint(int x, int z) {
        Cluster cluster = this.clusterAt(x, z);
        if (!cluster.contains(x, z)) {
            return FOOTPRINT_NONE;
        }
        double px = x + 0.5D;
        double pz = z + 0.5D;
        for (Island island : cluster.islands) {
            if (island.withinReach(px, pz)) {
                return island.isRoyal() ? FOOTPRINT_ROYAL : FOOTPRINT_SKY;
            }
        }
        return FOOTPRINT_NONE;
    }

    @Nullable
    public Island islandAt(int x, int z) {
        Cluster cluster = this.clusterAt(x, z);
        if (!cluster.contains(x, z)) {
            return null;
        }
        for (Island island : cluster.islands) {
            if (island.topAt(x, z) != NO_HEIGHT) {
                return island;
            }
        }
        return null;
    }

    public int craterDepth(int x, int z) {
        Cluster cluster = this.clusterAt(x, z);
        if (!cluster.contains(x, z)) {
            return 0;
        }
        int depth = 0;
        for (Island island : cluster.islands) {
            depth = Math.max(depth, island.craterDepthAt(x, z));
        }
        return depth;
    }

    public List<Island> islandsTouching(int minX, int minZ, int maxX, int maxZ) {
        int minCellX = Math.floorDiv(minX, CELL_SIZE);
        int maxCellX = Math.floorDiv(maxX, CELL_SIZE);
        int minCellZ = Math.floorDiv(minZ, CELL_SIZE);
        int maxCellZ = Math.floorDiv(maxZ, CELL_SIZE);
        List<Island> found = new ArrayList<>();
        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                Cluster cluster = this.cell(cellX, cellZ);
                if (!cluster.overlaps(minX, minZ, maxX, maxZ)) {
                    continue;
                }
                for (Island island : cluster.islands) {
                    if (island.overlaps(minX, minZ, maxX, maxZ)) {
                        found.add(island);
                    }
                }
            }
        }
        return found;
    }

    @Nullable
    public Island crownInCellOfChunk(int chunkX, int chunkZ) {
        Cluster cluster = this.clusterAt(chunkX << 4, chunkZ << 4);
        return cluster.islands.isEmpty() ? null : cluster.islands.get(0);
    }

    public static int cellOfChunk(int chunk) {
        return Math.floorDiv(chunk << 4, CELL_SIZE);
    }

    private Cluster clusterAt(int x, int z) {
        int cellX = Math.floorDiv(x, CELL_SIZE);
        int cellZ = Math.floorDiv(z, CELL_SIZE);
        CellRef ref = this.last;
        if (ref.cellX == cellX && ref.cellZ == cellZ) {
            return ref.cluster;
        }
        Cluster cluster = this.cell(cellX, cellZ);
        this.last = new CellRef(cellX, cellZ, cluster);
        return cluster;
    }

    private Cluster cell(int cellX, int cellZ) {
        long key = (long) cellX << 32 | (cellZ & 0xFFFFFFFFL);
        Cluster cached = this.cells.get(key);
        if (cached != null) {
            return cached;
        }
        if (this.cells.size() > MAX_CACHED_CELLS) {
            this.cells.clear();
        }
        Cluster created = this.build(cellX, cellZ);
        Cluster previous = this.cells.putIfAbsent(key, created);
        return previous != null ? previous : created;
    }

    private Cluster build(int cellX, int cellZ) {
        XoroshiroRandomSource random = new XoroshiroRandomSource(this.salt ^ (cellX * 0x9E3779B97F4A7C15L), cellZ * 0xC2B2AE3D27D4EB4FL + 0x2F7A1C93D58B6E41L);
        if (random.nextDouble() >= CLUSTER_CHANCE) {
            return NONE;
        }
        double centerX = (double) cellX * CELL_SIZE + CELL_SIZE * (CENTER_MIN + random.nextDouble() * CENTER_SPAN);
        double centerZ = (double) cellZ * CELL_SIZE + CELL_SIZE * (CENTER_MIN + random.nextDouble() * CENTER_SPAN);

        List<Island> islands = new ArrayList<>();
        Theme crownTheme = random.nextDouble() < 0.6D ? Theme.ROYAL_GROVE : Theme.FALLEN_REACH;
        double crownRadius = crownTheme == Theme.FALLEN_REACH ? 96.0D + random.nextDouble() * 8.0D : 55.0D + random.nextDouble() * 10.0D;
        islands.add(Island.create(random, centerX, centerZ, crownRadius, CROWN_TOP_Y, crownTheme, true));

        int satellites = 3 + random.nextInt(4);
        for (int i = 0; i < satellites; i++) {
            Theme theme = Theme.rollSatellite(random);
            if (theme == Theme.SHARK_SHOALS) {
                this.placeShoal(random, centerX, centerZ, islands);
            } else {
                double radius = theme == Theme.LAGOON ? 30.0D + random.nextDouble() * 15.0D : 25.0D + random.nextDouble() * 15.0D;
                this.placeSatellite(random, centerX, centerZ, radius, theme, islands);
            }
        }

        double minX = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (Island island : islands) {
            minX = Math.min(minX, island.centerX - island.reach);
            minZ = Math.min(minZ, island.centerZ - island.reach);
            maxX = Math.max(maxX, island.centerX + island.reach);
            maxZ = Math.max(maxZ, island.centerZ + island.reach);
        }
        return new Cluster(List.copyOf(islands), Mth.floor(minX), Mth.floor(minZ), Mth.ceil(maxX), Mth.ceil(maxZ));
    }

    @Nullable
    private Island placeSatellite(XoroshiroRandomSource random, double originX, double originZ, double radius, Theme theme, List<Island> islands) {
        Island crown = islands.get(0);
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double minDistance = (crown.radius + radius) * 1.55D + SATELLITE_GAP;
            double distance = minDistance + random.nextDouble() * Math.max(0.0D, SATELLITE_REACH - minDistance);
            double x = originX + Math.cos(angle) * distance;
            double z = originZ + Math.sin(angle) * distance;
            if (this.clear(x, z, radius, islands)) {
                int top = SATELLITE_MIN_TOP + random.nextInt(SATELLITE_MAX_TOP - SATELLITE_MIN_TOP + 1);
                Island island = Island.create(random, x, z, radius, top, theme, false);
                islands.add(island);
                return island;
            }
        }
        return null;
    }

    private void placeShoal(XoroshiroRandomSource random, double originX, double originZ, List<Island> islands) {
        Island anchor = this.placeSatellite(random, originX, originZ, 14.0D + random.nextDouble() * 8.0D, Theme.SHARK_SHOALS, islands);
        if (anchor == null) {
            return;
        }
        int fragments = 2 + random.nextInt(3);
        for (int i = 0; i < fragments; i++) {
            double radius = 8.0D + random.nextDouble() * 10.0D;
            for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = (anchor.radius + radius) * 1.55D + 6.0D + random.nextDouble() * 30.0D;
                double x = anchor.centerX + Math.cos(angle) * distance;
                double z = anchor.centerZ + Math.sin(angle) * distance;
                if (this.clear(x, z, radius, islands)) {
                    int top = Mth.clamp(anchor.topY - 12 + random.nextInt(25), SATELLITE_MIN_TOP, SATELLITE_MAX_TOP);
                    islands.add(Island.create(random, x, z, radius, top, Theme.SHARK_SHOALS, false));
                    break;
                }
            }
        }
    }

    private boolean clear(double x, double z, double radius, List<Island> islands) {
        for (Island other : islands) {
            double dx = x - other.centerX;
            double dz = z - other.centerZ;
            double gap = (radius + other.radius) * 1.55D + SATELLITE_GAP;
            if (dx * dx + dz * dz < gap * gap) {
                return false;
            }
        }
        return true;
    }

    static double hash(long seed, int x, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ (h >>> 31)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 29)) * 0x94D049BB133111EBL;
        h ^= h >>> 32;
        return (h >>> 11) * 0x1.0p-53;
    }

    public enum Theme {
        ROYAL_GROVE,
        FALLEN_REACH,
        LAGOON,
        SHARK_SHOALS,
        GROVE;

        static Theme rollSatellite(XoroshiroRandomSource random) {
            double roll = random.nextDouble();
            if (roll < 0.3D) {
                return LAGOON;
            }
            return roll < 0.7D ? SHARK_SHOALS : GROVE;
        }
    }

    private record SeededGrid(long seed, SkyIsleGrid grid) {
    }

    private record CellRef(int cellX, int cellZ, Cluster cluster) {
    }

    private record Cluster(List<Island> islands, int minX, int minZ, int maxX, int maxZ) {
        boolean contains(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }

        boolean overlaps(int minX, int minZ, int maxX, int maxZ) {
            return !this.islands.isEmpty() && maxX >= this.minX && minX <= this.maxX && maxZ >= this.minZ && minZ <= this.maxZ;
        }
    }

    static double smooth(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    static double valueNoise(long seed, double x, double z) {
        int cellX = Mth.floor(x);
        int cellZ = Mth.floor(z);
        double fx = smooth(x - cellX);
        double fz = smooth(z - cellZ);
        double a = hash(seed, cellX, cellZ);
        double b = hash(seed, cellX + 1, cellZ);
        double c = hash(seed, cellX, cellZ + 1);
        double d = hash(seed, cellX + 1, cellZ + 1);
        double north = a + (b - a) * fx;
        double south = c + (d - c) * fx;
        return (north + (south - north) * fz) * 2.0D - 1.0D;
    }

    static double fbm(long seed, double x, double z, int octaves) {
        double sum = 0.0D;
        double amplitude = 1.0D;
        double norm = 0.0D;
        double frequency = 1.0D;
        for (int i = 0; i < octaves; i++) {
            sum += amplitude * valueNoise(seed + i * 1013L, x * frequency, z * frequency);
            norm += amplitude;
            amplitude *= 0.5D;
            frequency *= 2.03D;
        }
        return sum / norm;
    }

    public static final class Island {
        public final double centerX;
        public final double centerZ;
        public final double radius;
        public final int topY;
        public final Theme theme;
        public final boolean crown;
        public final long shapeSeed;
        private final double[] lobeX;
        private final double[] lobeZ;
        private final double[] lobeRadius;
        private final double[] lobeDepth;
        private final double[] spikeX;
        private final double[] spikeZ;
        private final double[] spikeRadius;
        private final double[] spikeLength;
        private final double mainRadius;
        private final double padRadius;
        private final double relief;
        private final double cliff;
        private final double dipTan;
        private final double dipCos;
        private final double dipSin;
        private final double edgeNoise;
        private final double fineNoise;
        private final double keelRadius;
        private final int keelDepth;
        private final int craterDepth;
        private final double craterMargin;
        private final double reach;
        private final double reachSquared;
        private final double basinDepth;
        private final double waterfallAngle;
        private volatile int lakeLevel = NO_HEIGHT;

        private Island(XoroshiroRandomSource random, double centerX, double centerZ, double radius, int topY, Theme theme, boolean crown) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = radius;
            this.topY = topY;
            this.theme = theme;
            this.crown = crown;
            this.shapeSeed = random.nextLong();
            double room = topY - FLOOR_Y;
            int extra = crown ? 2 + random.nextInt(3) : 1 + random.nextInt(4);
            this.lobeX = new double[extra + 1];
            this.lobeZ = new double[extra + 1];
            this.lobeRadius = new double[extra + 1];
            this.lobeDepth = new double[extra + 1];
            this.mainRadius = radius * (crown ? 0.92D : 0.78D + random.nextDouble() * 0.1D);
            this.lobeX[0] = centerX;
            this.lobeZ[0] = centerZ;
            this.lobeRadius[0] = this.mainRadius;
            this.lobeDepth[0] = Math.min(room, radius * (crown ? 0.85D : 1.0D + random.nextDouble() * 0.6D));
            double far = 0.0D;
            for (int i = 1; i <= extra; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = radius * (crown ? 0.35D + random.nextDouble() * 0.35D : 0.45D + random.nextDouble() * 0.4D);
                double lobe = radius * (0.32D + random.nextDouble() * 0.33D);
                this.lobeX[i] = centerX + Math.cos(angle) * distance;
                this.lobeZ[i] = centerZ + Math.sin(angle) * distance;
                this.lobeRadius[i] = lobe;
                this.lobeDepth[i] = Math.min(room, lobe * (1.0D + random.nextDouble() * 0.9D));
                far = Math.max(far, distance + lobe);
            }
            int spikes = (int) (radius / 12.0D) + random.nextInt(3);
            this.spikeX = new double[spikes];
            this.spikeZ = new double[spikes];
            this.spikeRadius = new double[spikes];
            this.spikeLength = new double[spikes];
            for (int i = 0; i < spikes; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = radius * random.nextDouble() * 0.7D;
                this.spikeX[i] = centerX + Math.cos(angle) * distance;
                this.spikeZ[i] = centerZ + Math.sin(angle) * distance;
                this.spikeRadius[i] = 3.0D + random.nextDouble() * 4.0D;
                this.spikeLength[i] = 6.0D + random.nextDouble() * 14.0D;
            }
            this.padRadius = crown ? (theme == Theme.FALLEN_REACH ? 75.0D : 25.0D) : 0.0D;
            this.relief = crown ? 4.0D : 3.0D + random.nextDouble() * 5.0D;
            this.cliff = 6.0D + random.nextDouble() * 9.0D;
            double dip = Math.tan(Math.toRadians(8.0D + random.nextDouble() * 22.0D));
            double dipAngle = random.nextDouble() * Math.PI * 2.0D;
            this.dipTan = dip;
            this.dipCos = Math.cos(dipAngle);
            this.dipSin = Math.sin(dipAngle);
            this.edgeNoise = radius * 0.22D;
            this.fineNoise = Math.min(4.0D, radius * 0.08D);
            this.keelRadius = crown && theme == Theme.ROYAL_GROVE ? 24.0D : 0.0D;
            this.keelDepth = this.keelRadius > 0.0D ? Math.min(72, (int) room) : 0;
            double deepest = 0.0D;
            for (double depth : this.lobeDepth) {
                deepest = Math.max(deepest, depth);
            }
            this.craterDepth = Mth.clamp((int) Math.round(Math.max(deepest, this.keelDepth) * 0.5D), 6, 40);
            this.craterMargin = radius * 0.05D + CRATER_MARGIN;
            this.reach = Math.max(this.mainRadius, far) + this.edgeNoise + this.fineNoise + this.craterMargin;
            this.reachSquared = this.reach * this.reach;
            this.basinDepth = theme == Theme.LAGOON ? 6.0D + this.relief : 0.0D;
            this.waterfallAngle = random.nextDouble() * Math.PI * 2.0D - Math.PI;
        }

        static Island create(XoroshiroRandomSource random, double centerX, double centerZ, double radius, int topY, Theme theme, boolean crown) {
            return new Island(random, centerX, centerZ, radius, topY, theme, crown);
        }

        public boolean isLagoon() {
            return this.basinDepth > 0.0D;
        }

        public int waterLevel() {
            return this.isLagoon() ? this.topY - 2 : NO_HEIGHT;
        }

        public int craterDepth() {
            return this.craterDepth;
        }

        boolean withinReach(double x, double z) {
            double dx = x - this.centerX;
            double dz = z - this.centerZ;
            return dx * dx + dz * dz <= this.reachSquared;
        }

        boolean overlaps(int minX, int minZ, int maxX, int maxZ) {
            return maxX >= this.centerX - this.reach && minX <= this.centerX + this.reach
                    && maxZ >= this.centerZ - this.reach && minZ <= this.centerZ + this.reach;
        }

        private double inset(double x, double z) {
            double best = -Double.MAX_VALUE;
            for (int i = 0; i < this.lobeRadius.length; i++) {
                double dx = x - this.lobeX[i];
                double dz = z - this.lobeZ[i];
                best = Math.max(best, this.lobeRadius[i] - Math.sqrt(dx * dx + dz * dz));
            }
            double scale = this.radius * 0.35D + 8.0D;
            double wobble = fbm(this.shapeSeed ^ 0x51L, x / scale, z / scale, 3) * this.edgeNoise
                    + fbm(this.shapeSeed ^ 0x52L, x / 9.0D, z / 9.0D, 2) * this.fineNoise;
            if (this.padRadius > 0.0D) {
                double pad = Math.sqrt((x - this.centerX) * (x - this.centerX) + (z - this.centerZ) * (z - this.centerZ));
                wobble *= Mth.clamp((pad - this.padRadius) / 20.0D, 0.0D, 1.0D);
            }
            return best + wobble;
        }

        public double insetAt(int x, int z) {
            if (!this.withinReach(x + 0.5D, z + 0.5D)) {
                return -Double.MAX_VALUE;
            }
            return this.inset(x + 0.5D, z + 0.5D);
        }

        public double edgeDistance(int x, int z) {
            double inset = this.insetAt(x, z);
            return inset > 0.0D ? inset : Double.NaN;
        }

        public double centerRatio(int x, int z) {
            double dx = x + 0.5D - this.centerX;
            double dz = z + 0.5D - this.centerZ;
            return Math.sqrt(dx * dx + dz * dz) / this.mainRadius;
        }

        public double tiltAt(int x, int z) {
            double along = (x + 0.5D - this.centerX) * this.dipCos + (z + 0.5D - this.centerZ) * this.dipSin;
            return along * this.dipTan + fbm(this.shapeSeed ^ 0x66L, x / 60.0D, z / 60.0D, 2) * 10.0D;
        }

        public boolean holdsWater(int x, int z) {
            if (!this.isLagoon()) {
                return false;
            }
            double inset = this.insetAt(x, z);
            return inset > 0.0D && this.holdsWater(x, z, this.topFor(x, z, inset));
        }

        public boolean holdsWater(int x, int z, int top) {
            if (!this.isLagoon()) {
                return false;
            }
            double ratio = this.centerRatio(x, z);
            return (ratio < BASIN_RATIO + 0.05D || this.inChannel(x, z, ratio)) && top < this.waterLevel();
        }

        private boolean inChannel(int x, int z, double ratio) {
            if (!this.isLagoon() || ratio < 0.4D) {
                return false;
            }
            double dx = x + 0.5D - this.centerX;
            double dz = z + 0.5D - this.centerZ;
            double along = dx * Math.cos(this.waterfallAngle) + dz * Math.sin(this.waterfallAngle);
            double across = -dx * Math.sin(this.waterfallAngle) + dz * Math.cos(this.waterfallAngle);
            return along > 0.0D && Math.abs(across) <= CHANNEL_HALF_WIDTH;
        }

        public int topAt(int x, int z) {
            double inset = this.insetAt(x, z);
            return inset > 0.0D ? this.topFor(x, z, inset) : NO_HEIGHT;
        }

        public int topFor(int x, int z, double inset) {
            double rolling = fbm(this.shapeSeed ^ 0x77L, x / 38.0D, z / 38.0D, 3) * this.relief;
            double knoll = Math.max(0.0D, fbm(this.shapeSeed ^ 0x99L, x / 22.0D, z / 22.0D, 2) - 0.35D) * this.relief * 2.2D;
            double height = rolling + knoll;
            if (this.padRadius > 0.0D) {
                double dx = x + 0.5D - this.centerX;
                double dz = z + 0.5D - this.centerZ;
                double pad = Math.sqrt(dx * dx + dz * dz);
                height *= smooth(Mth.clamp((pad - this.padRadius) / 14.0D, 0.0D, 1.0D));
            }
            if (inset < 3.0D) {
                height -= (3.0D - inset) * 0.4D;
            }
            int top = this.topY + (int) Math.round(height);
            if (this.isLagoon()) {
                double ratio = this.centerRatio(x, z);
                if (ratio < BASIN_RATIO) {
                    top -= (int) Math.round(this.basinDepth * (1.0D - smooth(ratio / BASIN_RATIO)));
                }
                if (this.inChannel(x, z, ratio)) {
                    top = Math.min(top, this.waterLevel() - 1);
                }
            }
            return top;
        }

        public int bottomAt(int x, int z) {
            double inset = this.insetAt(x, z);
            return inset > 0.0D ? this.bottomFor(x, z, inset, this.topFor(x, z, inset)) : NO_HEIGHT;
        }

        public int bottomFor(int x, int z, double inset, int top) {
            double px = x + 0.5D;
            double pz = z + 0.5D;
            double depth = inset < 1.5D ? 2.5D : this.cliff;
            for (int i = 0; i < this.lobeRadius.length; i++) {
                double dx = px - this.lobeX[i];
                double dz = pz - this.lobeZ[i];
                double fill = 1.0D - Math.sqrt(dx * dx + dz * dz) / this.lobeRadius[i];
                if (fill > 0.0D) {
                    depth = Math.max(depth, this.cliff * 0.6D + this.lobeDepth[i] * Math.pow(fill, 1.35D));
                }
            }
            double angle = Math.atan2(pz - this.centerZ, px - this.centerX);
            double warp = fbm(this.shapeSeed ^ 0x33L, x / 30.0D, z / 30.0D, 2) * 2.0D;
            double rib = Math.abs(Math.sin(angle * 9.0D + warp)) * 2.0D - 1.0D;
            depth += rib * Math.min(depth * 0.12D, 6.0D);
            depth += fbm(this.shapeSeed ^ 0x44L, x / 14.0D, z / 14.0D, 2) * Math.min(depth * 0.1D, 4.0D);
            for (int i = 0; i < this.spikeRadius.length; i++) {
                double dx = px - this.spikeX[i];
                double dz = pz - this.spikeZ[i];
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance < this.spikeRadius[i]) {
                    depth += this.spikeLength[i] * Math.pow(1.0D - distance / this.spikeRadius[i], 1.4D);
                }
            }
            double room = this.topY - FLOOR_Y;
            depth = room * Math.tanh(depth / room);
            if (this.keelDepth > 0) {
                double dx = px - this.centerX;
                double dz = pz - this.centerZ;
                double keel = Mth.clamp(1.0D - (Math.sqrt(dx * dx + dz * dz) - this.keelRadius) / 10.0D, 0.0D, 1.0D);
                depth = Math.max(depth, this.keelDepth * smooth(keel));
            }
            int bottom = this.topY - (int) Math.round(depth);
            return Math.max(Math.min(bottom, top - 2), FLOOR_Y);
        }

        public double craterRatioAt(int x, int z) {
            return this.craterRatioFor(this.insetAt(x, z));
        }

        public double craterRatioFor(double inset) {
            double crater = inset + this.craterMargin;
            if (crater <= 0.0D) {
                return Double.NaN;
            }
            return 1.0D - Mth.clamp(crater / (this.radius * 0.6D), 0.0D, 1.0D);
        }

        public int craterDepthAt(int x, int z) {
            return this.craterDepthFor(this.insetAt(x, z));
        }

        public int craterDepthFor(double inset) {
            double ratio = this.craterRatioFor(inset);
            if (Double.isNaN(ratio)) {
                return 0;
            }
            double wall = 1.0D - smooth(Mth.clamp((ratio - 0.4D) / 0.6D, 0.0D, 1.0D));
            return (int) Math.round(this.craterDepth * wall);
        }

        public int cachedLakeLevel() {
            return this.lakeLevel;
        }

        public void cacheLakeLevel(int level) {
            this.lakeLevel = level;
        }

        public boolean isRoyal() {
            return this.theme == Theme.ROYAL_GROVE;
        }
    }
}
