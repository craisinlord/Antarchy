package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public final class CavarynCavityGrid {
    private static final int MAX_CACHED_CELLS = 16384;
    private static final double BLEND = 2.5D;
    private static final double WOBBLE = 0.08D;
    private static final double REACH_PADDING = BLEND + 1.5D;
    private static final Shape[] NO_SHAPES = new Shape[0];
    private static final int WOBBLE_Y_TABLE_SIZE = 512;
    private static final double[] SIN_Y = new double[WOBBLE_Y_TABLE_SIZE];
    private static final double[] COS_Y = new double[WOBBLE_Y_TABLE_SIZE];

    static {
        for (int y = 0; y < WOBBLE_Y_TABLE_SIZE; y++) {
            double angle = (y + 0.5D) * 0.43D;
            SIN_Y[y] = Math.sin(angle);
            COS_Y[y] = Math.cos(angle);
        }
    }

    private static final Map<Long, WeakReference<CavarynCavityGrid>> BY_SALT = new ConcurrentHashMap<>();

    private final Kind kind;
    private final long salt;
    private final CavarynWormBoreGrid.StartFilter startFilter;
    private final Map<Long, Cavity[]> cells = new ConcurrentHashMap<>();
    private final ThreadLocal<ColumnCache> columns = ThreadLocal.withInitial(ColumnCache::new);

    private CavarynCavityGrid(Kind kind, long salt, CavarynWormBoreGrid.StartFilter startFilter) {
        this.kind = kind;
        this.salt = salt;
        this.startFilter = startFilter;
    }

    public static CavarynCavityGrid create(Kind kind, long salt, CavarynWormBoreGrid.StartFilter startFilter) {
        CavarynCavityGrid existing = forSalt(salt);
        if (existing != null && existing.kind == kind) {
            return existing;
        }
        BY_SALT.values().removeIf(reference -> reference.get() == null);
        CavarynCavityGrid grid = new CavarynCavityGrid(kind, salt, startFilter);
        BY_SALT.put(salt, new WeakReference<>(grid));
        return grid;
    }

    public static CavarynCavityGrid forSalt(long salt) {
        WeakReference<CavarynCavityGrid> reference = BY_SALT.get(salt);
        return reference != null ? reference.get() : null;
    }

    public Kind kind() {
        return this.kind;
    }

    public double signedDistance(int x, int y, int z) {
        return this.signedDistance(x, y, z, -Double.MAX_VALUE);
    }

    public double signedDistance(int x, int y, int z, double floor) {
        return this.candidates(x, z).signedDistance(x, y, z, floor);
    }

    public int[] verticalRange(int x, int z) {
        ColumnCache cache = this.candidates(x, z);
        if (cache.count == 0) {
            return null;
        }
        return new int[]{Mth.floor(cache.minY), Mth.ceil(cache.maxY)};
    }

    public boolean hasCandidates(int x, int z) {
        ColumnCache cache = this.columns.get();
        cache.loadChunk(x >> 4, z >> 4);
        return cache.chunkShapes.length > 0;
    }

    CavarynColumn newColumn() {
        return new ColumnCache();
    }

    public List<Chamber> chambersCenteredIn(int minX, int minZ, int maxX, int maxZ) {
        List<Chamber> found = new ArrayList<>();
        int cellSize = this.kind.cellSize;
        int reach = this.kind.cellReach;
        int cellMinX = Math.floorDiv(minX, cellSize);
        int cellMinZ = Math.floorDiv(minZ, cellSize);
        int cellMaxX = Math.floorDiv(maxX, cellSize);
        int cellMaxZ = Math.floorDiv(maxZ, cellSize);
        for (int cellX = cellMinX - reach; cellX <= cellMaxX + reach; cellX++) {
            for (int cellZ = cellMinZ - reach; cellZ <= cellMaxZ + reach; cellZ++) {
                for (Cavity cavity : this.cavities(cellX, cellZ)) {
                    for (Chamber chamber : cavity.chambers) {
                        if (chamber.cx >= minX && chamber.cx < maxX + 1 && chamber.cz >= minZ && chamber.cz < maxZ + 1) {
                            found.add(chamber);
                        }
                    }
                }
            }
        }
        return found;
    }

    private static double smoothMin(double a, double b) {
        double h = Math.max(BLEND - Math.abs(a - b), 0.0D) / BLEND;
        return Math.min(a, b) - h * h * BLEND * 0.25D;
    }

    private ColumnCache candidates(int x, int z) {
        ColumnCache cache = this.columns.get();
        cache.load(x >> 2, z >> 2);
        return cache;
    }

    private Shape[] collectChunk(int chunkX, int chunkZ) {
        double minX = chunkX * 16.0D;
        double minZ = chunkZ * 16.0D;
        double maxX = minX + 16.0D;
        double maxZ = minZ + 16.0D;
        int cellX = Math.floorDiv(chunkX * 16, this.kind.cellSize);
        int cellZ = Math.floorDiv(chunkZ * 16, this.kind.cellSize);
        int reach = this.kind.cellReach;
        List<Shape> found = new ArrayList<>();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                for (Cavity cavity : this.cavities(cellX + dx, cellZ + dz)) {
                    if (cavity.maxX < minX || cavity.minX > maxX || cavity.maxZ < minZ || cavity.minZ > maxZ) {
                        continue;
                    }
                    for (Shape shape : cavity.shapes) {
                        if (shape.maxX < minX || shape.minX > maxX || shape.maxZ < minZ || shape.minZ > maxZ) {
                            continue;
                        }
                        found.add(shape);
                    }
                }
            }
        }
        return found.isEmpty() ? NO_SHAPES : found.toArray(new Shape[0]);
    }

    private Cavity[] cavities(int cellX, int cellZ) {
        Long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
        Cavity[] cached = this.cells.get(key);
        if (cached != null) {
            return cached;
        }
        trim(this.cells, MAX_CACHED_CELLS);
        return this.cells.computeIfAbsent(key, ignored -> this.generate(cellX, cellZ));
    }

    private Cavity[] generate(int cellX, int cellZ) {
        Kind kind = this.kind;
        if (kind == Kind.BROOD_GALLERY) {
            return this.galleryCell(cellX, cellZ);
        }
        XoroshiroRandomSource random = new XoroshiroRandomSource(mix(this.salt ^ (cellX * 0x5DEECE66DL) ^ (cellZ * 0x9E3779B97F4A7C15L)), ~this.salt);
        int count = random.nextFloat() < kind.extraChance ? kind.baseCount + 1 : kind.baseCount;
        double margin = kind.cellSize / 7.0D;
        List<Cavity> cavities = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double[] start = null;
            for (int attempt = 0; attempt < 4 && start == null; attempt++) {
                double sx = cellX * (double) kind.cellSize + margin + random.nextDouble() * (kind.cellSize - margin * 2.0D);
                double sz = cellZ * (double) kind.cellSize + margin + random.nextDouble() * (kind.cellSize - margin * 2.0D);
                double sy = kind.startMinY + random.nextDouble() * (kind.startMaxY - kind.startMinY);
                if (this.startFilter.accepts(sx, sy, sz)) {
                    start = new double[]{sx, sy, sz};
                }
            }
            if (start != null) {
                Builder builder = new Builder(kind, random);
                if (kind == Kind.GLOWCAP_GROTTO) {
                    builder.grotto(start[0], start[1], start[2]);
                } else if (kind == Kind.MOLEWORM_WARREN) {
                    builder.warren(start[0], start[1], start[2]);
                } else if (kind == Kind.CARAPACE_RIFT) {
                    builder.rift(start[0], start[1], start[2]);
                } else if (kind == Kind.BIOWART_GUT) {
                    builder.gut(start[0], start[1], start[2]);
                } else if (kind == Kind.CRUST_SHAFT) {
                    builder.shaft(start[0], start[1], start[2]);
                } else {
                    builder.nest(start[0], start[1], start[2]);
                }
                cavities.add(builder.finish());
            }
        }
        return cavities.toArray(new Cavity[0]);
    }

    private Cavity[] galleryCell(int cellX, int cellZ) {
        Builder builder = new Builder(this.kind, new XoroshiroRandomSource(mix(this.salt ^ (cellX * 0x2545F4914F6CDD1DL) ^ (cellZ * 0x9E3779B97F4A7C15L)), this.salt));
        double[] low = this.anchor(cellX, cellZ, 0);
        double[] high = this.anchor(cellX, cellZ, 1);
        for (int layer = 0; layer < 2; layer++) {
            double[] own = layer == 0 ? low : high;
            if (own == null) {
                continue;
            }
            int before = builder.shapes.size();
            builder.gallery(own, this.anchor(cellX + 1, cellZ, layer), 0.9F);
            builder.gallery(own, this.anchor(cellX, cellZ + 1, layer), 0.9F);
            builder.gallery(own, this.anchor(cellX + 1, cellZ + (layer == 0 ? 1 : -1), layer), 0.5F);
            if (builder.shapes.size() > before) {
                builder.junction(own);
                if (layer == 1 && builder.random.nextFloat() < 0.4F) {
                    builder.chimney(own);
                }
            }
        }
        if (low != null && high != null && builder.random.nextFloat() < 0.45F) {
            builder.gallery(low, high, 1.0F);
        }
        return builder.shapes.isEmpty() ? new Cavity[0] : new Cavity[]{builder.finish()};
    }

    private double[] anchor(int cellX, int cellZ, int layer) {
        XoroshiroRandomSource random = new XoroshiroRandomSource(mix(this.salt ^ (cellX * 0x632BE59BD9B4E019L) ^ (cellZ * 0x85157AF5L) ^ (layer * 0x7F4A7C15L)), ~this.salt);
        int size = this.kind.cellSize;
        double x = cellX * (double) size + 16.0D + random.nextDouble() * (size - 32.0D);
        double z = cellZ * (double) size + 16.0D + random.nextDouble() * (size - 32.0D);
        double y = layer == 0 ? 36.0D + random.nextDouble() * 10.0D : 56.0D + random.nextDouble() * 14.0D;
        return this.startFilter.accepts(x, y, z) ? new double[]{x, y, z} : null;
    }

    static <V> void trim(Map<Long, V> cells, int limit) {
        int excess = cells.size() - limit;
        if (excess <= 0) {
            return;
        }
        excess += limit / 2;
        Iterator<Long> keys = cells.keySet().iterator();
        while (excess-- > 0 && keys.hasNext()) {
            keys.next();
            keys.remove();
        }
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public enum Kind implements StringRepresentable {
        BROOD_NEST("brood_nest", CavarynBiomeTable.BROODING_TUNNELS, true, 176, 2, 1, 0.45F, 34.0D, 112.0D, 40.0D, 76.0D),
        GLOWCAP_GROTTO("glowcap_grotto", CavarynBiomeTable.GLOWCAP_CAVES, true, 160, 3, 1, 0.6F, 50.0D, 188.0D, 96.0D, 140.0D),
        BROOD_GALLERY("brood_gallery", CavarynBiomeTable.BROODING_TUNNELS, true, 96, 2, 0, 0.0F, 30.0D, 124.0D, 36.0D, 70.0D),
        MOLEWORM_WARREN("moleworm_warren", CavarynBiomeTable.MOLEWORM_CAVES, false, 160, 3, 1, 0.5F, 40.0D, 184.0D, 66.0D, 92.0D),
        CARAPACE_RIFT("carapace_rift", CavarynBiomeTable.CAVARYN, true, 160, 4, 2, 0.5F, 100.0D, 186.0D, 130.0D, 156.0D),
        BIOWART_GUT("biowart_gut", CavarynBiomeTable.BIOWART_CAVES, true, 160, 3, 1, 0.6F, 100.0D, 186.0D, 112.0D, 170.0D),
        CRUST_SHAFT("crust_shaft", null, false, 96, 1, 2, 0.5F, 120.0D, 346.0D, 326.0D, 334.0D);

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;
        private final String biome;
        private final boolean strict;
        private final int cellSize;
        private final int cellReach;
        private final int baseCount;
        private final float extraChance;
        private final double minY;
        private final double maxY;
        private final double startMinY;
        private final double startMaxY;

        Kind(String name, String biome, boolean strict, int cellSize, int cellReach, int baseCount, float extraChance, double minY, double maxY, double startMinY, double startMaxY) {
            this.name = name;
            this.biome = biome;
            this.strict = strict;
            this.cellSize = cellSize;
            this.cellReach = cellReach;
            this.baseCount = baseCount;
            this.extraChance = extraChance;
            this.minY = minY;
            this.maxY = maxY;
            this.startMinY = startMinY;
            this.startMaxY = startMaxY;
        }

        public String biome() {
            return this.biome;
        }

        public boolean strict() {
            return this.strict;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    private static final class Builder {
        final Kind kind;
        final XoroshiroRandomSource random;
        final List<Shape> shapes = new ArrayList<>();
        final List<Chamber> chambers = new ArrayList<>();

        Builder(Kind kind, XoroshiroRandomSource random) {
            this.kind = kind;
            this.random = random;
        }

        Cavity finish() {
            return new Cavity(this.shapes.toArray(new Shape[0]), this.chambers.toArray(new Chamber[0]));
        }

        void nest(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double hallRadius = 14.0D + random.nextDouble() * 6.0D;
            double hallHeight = 8.0D + random.nextDouble() * 3.0D;
            this.chamber(x, y, z, hallRadius, hallHeight, hallHeight * 0.55D);
            int galleries = 5 + random.nextInt(4);
            int exits = 2 + random.nextInt(2);
            double baseYaw = random.nextDouble() * Math.PI * 2.0D;
            for (int i = 0; i < galleries; i++) {
                double yaw = baseYaw + i * (Math.PI * 2.0D / galleries) + random.nextGaussian() * 0.25D;
                double sx = x + Math.cos(yaw) * hallRadius * 0.7D;
                double sz = z + Math.sin(yaw) * hallRadius * 0.7D;
                double sy = y - hallHeight * 0.2D;
                if (i < exits) {
                    double radius = 3.2D + random.nextDouble() * 0.8D;
                    this.walk(sx, sy, sz, yaw, 7 + random.nextInt(5), 10.0D, 14.0D, radius, 0.22D, 0.0D);
                    continue;
                }
                double radius = 2.4D + random.nextDouble() * 0.8D;
                double[] end = this.walk(sx, sy, sz, yaw, 2 + random.nextInt(3), 7.0D, 11.0D, radius, 0.3D, 0.0D);
                this.broodCell(end);
                if (random.nextFloat() < 0.35F) {
                    double branchYaw = yaw + (random.nextBoolean() ? 1.0D : -1.0D) * (0.7D + random.nextDouble() * 0.5D);
                    double[] branchEnd = this.walk(end[0], end[1], end[2], branchYaw, 2, 7.0D, 10.0D, radius * 0.9D, 0.3D, 0.0D);
                    this.broodCell(branchEnd);
                }
            }
        }

        void gallery(double[] a, double[] b, float chance) {
            XoroshiroRandomSource random = this.random;
            if (b == null || random.nextFloat() >= chance) {
                return;
            }
            double dx = b[0] - a[0];
            double dz = b[2] - a[2];
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length < 1.0D) {
                return;
            }
            double sideX = -dz / length;
            double sideZ = dx / length;
            double bend = (random.nextDouble() - 0.5D) * 36.0D;
            double wave = (random.nextDouble() - 0.5D) * 16.0D;
            double rise = (random.nextDouble() - 0.5D) * 6.0D;
            double phase = random.nextDouble() * Math.PI * 2.0D;
            double base = 7.0D + random.nextDouble() * 3.0D;
            int segments = Math.max(4, Mth.ceil(length / 12.0D));
            double x = a[0];
            double y = a[1];
            double z = a[2];
            double travelled = 0.0D;
            double nextNiche = 12.0D + random.nextDouble() * 20.0D;
            for (int i = 1; i <= segments; i++) {
                double t = i / (double) segments;
                double offset = bend * Math.sin(Math.PI * t) + wave * Math.sin(Math.PI * 2.0D * t);
                double nx = a[0] + dx * t + sideX * offset;
                double nz = a[2] + dz * t + sideZ * offset;
                double ny = Mth.lerp(t, a[1], b[1]) + rise * Math.sin(Math.PI * 2.0D * t);
                double radius = base * (1.0D + 0.12D * Math.sin(Math.PI * 3.0D * t + phase));
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 0.85D, 0.4D, 0.0D));
                travelled += Math.sqrt((nx - x) * (nx - x) + (ny - y) * (ny - y) + (nz - z) * (nz - z));
                while (travelled >= nextNiche) {
                    double side = random.nextBoolean() ? 1.0D : -1.0D;
                    double reach = radius * 0.9D + 1.5D;
                    double floor = ny - radius * 0.85D * 0.4D;
                    this.chamber(nx + sideX * side * reach, floor + 2.5D, nz + sideZ * side * reach, 3.5D, 3.0D, 2.0D);
                    nextNiche += 25.0D + random.nextDouble() * 15.0D;
                }
                x = nx;
                y = ny;
                z = nz;
            }
        }

        void chimney(double[] point) {
            double yaw = this.random.nextDouble() * Math.PI * 2.0D;
            this.walk(point[0], point[1], point[2], yaw, 4 + this.random.nextInt(2), 10.0D, 13.0D, 4.0D + this.random.nextDouble(), 1.1D, 0.95D, 0.8D);
        }

        void junction(double[] point) {
            double radius = 9.0D + this.random.nextDouble() * 3.0D;
            this.chamber(point[0], point[1], point[2], radius, 6.0D + this.random.nextDouble() * 2.0D, 3.0D);
        }

        void grotto(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double radius = 18.0D + random.nextDouble() * 8.0D;
            double height = 18.0D + random.nextDouble() * 10.0D;
            this.chamber(x, y, z, radius, height, 5.0D + random.nextDouble() * 2.0D);
            int lobes = random.nextInt(3);
            double lobeYaw = random.nextDouble() * Math.PI * 2.0D;
            for (int i = 0; i < lobes; i++) {
                lobeYaw += Math.PI * (0.6D + random.nextDouble() * 0.8D);
                double lobeRadius = radius * (0.55D + random.nextDouble() * 0.25D);
                double lobeHeight = height * (0.55D + random.nextDouble() * 0.3D);
                double offset = radius * 0.75D + lobeRadius * 0.45D;
                double ly = Mth.clamp(y + (random.nextDouble() - 0.5D) * 8.0D, this.kind.minY + 8.0D, this.kind.maxY - lobeHeight * 1.2D);
                this.chamber(x + Math.cos(lobeYaw) * offset, ly, z + Math.sin(lobeYaw) * offset, lobeRadius, lobeHeight, 4.0D + random.nextDouble() * 2.0D);
            }
            int tunnels = 3 + random.nextInt(2);
            double baseYaw = random.nextDouble() * Math.PI * 2.0D;
            for (int i = 0; i < tunnels; i++) {
                double yaw = baseYaw + i * (Math.PI * 2.0D / tunnels) + random.nextGaussian() * 0.3D;
                double sx = x + Math.cos(yaw) * radius * 0.8D;
                double sz = z + Math.sin(yaw) * radius * 0.8D;
                boolean dive = i == 0;
                double sy = dive ? y : y + random.nextDouble() * height * 0.3D;
                this.walk(sx, sy, sz, yaw, 10 + random.nextInt(5), 10.0D, 14.0D, 3.6D + random.nextDouble() * 1.2D, dive ? 0.5D : 0.3D, dive ? -0.45D : 0.0D);
            }
        }

        void gut(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double yaw = random.nextDouble() * Math.PI * 2.0D;
            int bulbs = 8 + random.nextInt(7);
            double neck = 2.6D + random.nextDouble() * 0.6D;
            double minY = this.kind.startMinY - 4.0D;
            double maxY = this.kind.startMaxY + 4.0D;
            double[] middle = null;
            for (int i = 0; i < bulbs; i++) {
                double radius = 5.0D + random.nextDouble() * 3.5D;
                double height = radius * (0.8D + random.nextDouble() * 0.2D);
                this.chamber(x, y, z, radius, height, height * 0.75D);
                if (i == bulbs / 2) {
                    middle = new double[]{x, y, z, yaw};
                }
                if (i == bulbs - 1) {
                    break;
                }
                yaw += random.nextGaussian() * 0.35D;
                double length = 11.0D + random.nextDouble() * 4.0D;
                double nx = x + Math.cos(yaw) * length;
                double ny = Mth.clamp(y + random.nextGaussian() * 2.0D, minY, maxY);
                double nz = z + Math.sin(yaw) * length;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, neck, 1.0D));
                x = nx;
                y = ny;
                z = nz;
            }
            this.walk(x, y, z, yaw, 5 + random.nextInt(3), 10.0D, 14.0D, 3.4D + random.nextDouble() * 0.8D, 0.5D, -0.4D);
            if (middle != null) {
                this.walk(middle[0], middle[1], middle[2], middle[3] + Math.PI * 0.5D, 5 + random.nextInt(3), 10.0D, 14.0D, 3.4D + random.nextDouble() * 0.8D, 0.5D, -0.4D);
            }
        }

        void rift(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double yaw = random.nextDouble() * Math.PI * 2.0D;
            double[] mid = this.slot(x, y, z, yaw, 9 + random.nextInt(6));
            double[] end = this.slot(x, y, z, yaw + Math.PI, 9 + random.nextInt(6));
            if (random.nextFloat() < 0.35F) {
                double forkYaw = yaw + (random.nextBoolean() ? 1.0D : -1.0D) * (0.6D + random.nextDouble() * 0.5D);
                this.slot(mid[0], mid[1], mid[2], forkYaw, 5 + random.nextInt(5));
            }
            this.walk(end[0], end[1] - 8.0D, end[2], yaw + Math.PI, 5 + random.nextInt(3), 10.0D, 14.0D, 3.6D + random.nextDouble() * 0.8D, 0.5D, -0.4D);
            this.walk(mid[0], mid[1] - 8.0D, mid[2], yaw, 5 + random.nextInt(3), 10.0D, 14.0D, 3.6D + random.nextDouble() * 0.8D, 0.5D, -0.4D);
        }

        private double[] slot(double x, double y, double z, double yaw, int segments) {
            XoroshiroRandomSource random = this.random;
            double phase = random.nextDouble() * Math.PI * 2.0D;
            double squash = 4.2D + random.nextDouble() * 1.0D;
            for (int i = 0; i < segments; i++) {
                yaw += random.nextGaussian() * 0.22D;
                double length = 10.0D + random.nextDouble() * 4.0D;
                double nx = x + Math.cos(yaw) * length;
                double ny = Mth.clamp(y + random.nextGaussian() * 1.5D, this.kind.startMinY - 6.0D, this.kind.startMaxY + 6.0D);
                double nz = z + Math.sin(yaw) * length;
                double taper = Math.min(1.0D, Math.min(i + 1, segments - i) / 2.0D);
                double radius = (4.2D + 0.9D * Math.sin(i * 0.7D + phase)) * (0.55D + 0.45D * taper);
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, squash));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x, y, z};
        }

        void warren(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double yaw = random.nextDouble() * Math.PI * 2.0D;
            double[] hub = {x, y, z};
            for (int level = 0; level < 3 && hub != null; level++) {
                double radius = 13.0D + random.nextDouble() * 5.0D;
                double height = radius * (0.75D + random.nextDouble() * 0.15D);
                this.chamber(hub[0], hub[1], hub[2], radius, height, height * 0.45D);
                int tunnels = 3 + random.nextInt(2);
                double baseYaw = random.nextDouble() * Math.PI * 2.0D;
                for (int i = 0; i < tunnels; i++) {
                    double tunnelYaw = baseYaw + i * (Math.PI * 2.0D / tunnels) + random.nextGaussian() * 0.3D;
                    double sx = hub[0] + Math.cos(tunnelYaw) * radius * 0.6D;
                    double sz = hub[2] + Math.sin(tunnelYaw) * radius * 0.6D;
                    double[] tunnelEnd = this.moleTunnel(sx, hub[1], sz, tunnelYaw, 6 + random.nextInt(4));
                    if (random.nextFloat() < 0.4F) {
                        double endRadius = 8.0D + random.nextDouble() * 2.0D;
                        this.chamber(tunnelEnd[0], tunnelEnd[1], tunnelEnd[2], endRadius, endRadius * 0.8D, endRadius * 0.35D);
                    }
                }
                if (random.nextFloat() < 0.6F) {
                    double shaftRadius = 4.0D + random.nextDouble();
                    double top = hub[1] + height * 0.6D;
                    double bottom = Math.max(this.kind.minY, hub[1] - 30.0D);
                    this.shapes.add(new Tube(hub[0], top, hub[2], hub[0] + 0.01D, bottom, hub[2], shaftRadius, 1.0D, 1.0D, 0.0D));
                }
                yaw += Math.PI * (0.5D + random.nextDouble());
                hub = level < 2 ? this.ramp(hub[0] + Math.cos(yaw) * radius * 0.7D, hub[1], hub[2] + Math.sin(yaw) * radius * 0.7D, yaw, 26.0D + random.nextDouble() * 6.0D) : null;
            }
        }

        void shaft(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double radius = 3.0D + random.nextDouble() * 3.0D;
            double throatY = 268.0D + random.nextDouble() * 8.0D;
            double throatX = x + random.nextGaussian() * 3.0D;
            double throatZ = z + random.nextGaussian() * 3.0D;
            this.shapes.add(new Tube(x, y, z, throatX, throatY, throatZ, radius * 1.9D, 1.0D));
            double mouthBottom = 226.0D + random.nextDouble() * 8.0D;
            x = throatX + random.nextGaussian() * 2.0D;
            z = throatZ + random.nextGaussian() * 2.0D;
            this.shapes.add(new Tube(throatX, throatY, throatZ, x, mouthBottom, z, radius * 1.5D, 1.0D));
            double bottom = 140.0D + random.nextDouble() * 40.0D;
            double driftX = random.nextGaussian() * 0.4D;
            double driftZ = random.nextGaussian() * 0.4D;
            y = mouthBottom + 4.0D;
            while (y > bottom) {
                double length = 10.0D + random.nextDouble() * 4.0D;
                driftX = Mth.clamp(driftX * 0.7D + random.nextGaussian() * 0.25D, -0.6D, 0.6D);
                driftZ = Mth.clamp(driftZ * 0.7D + random.nextGaussian() * 0.25D, -0.6D, 0.6D);
                double nx = x + driftX * length;
                double ny = Math.max(bottom, y - length);
                double nz = z + driftZ * length;
                double segmentRadius = radius * (0.85D + random.nextDouble() * 0.3D);
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, segmentRadius, 1.0D));
                x = nx;
                y = ny;
                z = nz;
            }
            this.shapes.add(new Tube(x, y, z, x + 0.01D, y - 6.0D, z, radius * 1.4D, 1.0D));
        }

        private double[] moleTunnel(double x, double y, double z, double yaw, int segments) {
            XoroshiroRandomSource random = this.random;
            double radius = 7.0D + random.nextDouble() * 2.0D;
            double pitch = 0.0D;
            for (int i = 0; i < segments; i++) {
                yaw += random.nextGaussian() * 0.3D;
                pitch = Mth.clamp(pitch * 0.6D + random.nextGaussian() * 0.05D, -0.12D, 0.12D);
                double length = 10.0D + random.nextDouble() * 4.0D;
                double horizontal = Math.cos(pitch) * length;
                double nx = x + Math.cos(yaw) * horizontal;
                double ny = Mth.clamp(y + Math.sin(pitch) * length, this.kind.minY + 8.0D, this.kind.maxY - 12.0D);
                double nz = z + Math.sin(yaw) * horizontal;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 1.0D, 0.45D, 0.3D));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x, y, z};
        }

        private double[] ramp(double x, double y, double z, double yaw, double climb) {
            XoroshiroRandomSource random = this.random;
            double target = y + climb;
            if (target > this.kind.maxY - 20.0D) {
                return null;
            }
            double radius = 7.0D + random.nextDouble() * 1.5D;
            double pitch = 0.45D + random.nextDouble() * 0.25D;
            while (y < target) {
                yaw += random.nextGaussian() * 0.2D;
                double length = 10.0D + random.nextDouble() * 3.0D;
                double horizontal = Math.cos(pitch) * length;
                double nx = x + Math.cos(yaw) * horizontal;
                double ny = Math.min(target, y + Math.sin(pitch) * length);
                double nz = z + Math.sin(yaw) * horizontal;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 1.0D, 0.45D, 0.3D));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x + Math.cos(yaw) * 8.0D, y, z + Math.sin(yaw) * 8.0D};
        }

        private void broodCell(double[] center) {
            double radius = 4.5D + this.random.nextDouble() * 2.0D;
            double height = 3.5D + this.random.nextDouble() * 1.5D;
            this.chamber(center[0], center[1], center[2], radius, height, height * 0.7D);
        }

        private void chamber(double x, double y, double z, double radius, double height, double depth) {
            Chamber chamber = new Chamber(x, y, z, radius, height, depth, this.random.nextDouble() * Math.PI * 2.0D);
            this.shapes.add(chamber);
            this.chambers.add(chamber);
        }

        private double[] walk(double x, double y, double z, double yaw, int segments, double minLength, double maxLength, double radius, double maxPitch, double pitchBias) {
            return this.walk(x, y, z, yaw, segments, minLength, maxLength, radius, maxPitch, pitchBias, 0.3D);
        }

        private double[] walk(double x, double y, double z, double yaw, int segments, double minLength, double maxLength, double radius, double maxPitch, double pitchBias, double biasWeight) {
            XoroshiroRandomSource random = this.random;
            double minY = this.kind.minY;
            double maxY = this.kind.maxY;
            double pitch = (random.nextDouble() - 0.5D) * maxPitch;
            for (int i = 0; i < segments; i++) {
                yaw += random.nextGaussian() * 0.3D;
                pitch = Mth.clamp(pitch * (1.0D - biasWeight) + pitchBias * biasWeight + random.nextGaussian() * 0.12D, -maxPitch, maxPitch);
                if (y < minY + 4.0D) {
                    pitch = Math.abs(pitch) + 0.05D;
                } else if (y > maxY - 4.0D) {
                    pitch = -Math.abs(pitch) - 0.05D;
                }
                double length = minLength + random.nextDouble() * (maxLength - minLength);
                double horizontal = Math.cos(pitch) * length;
                double nx = x + Math.cos(yaw) * horizontal;
                double ny = Mth.clamp(y + Math.sin(pitch) * length, minY, maxY);
                double nz = z + Math.sin(yaw) * horizontal;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 1.0D));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x, y, z};
        }
    }

    private final class ColumnCache implements CavarynColumn {
        boolean valid;
        int qx;
        int qz;
        boolean chunkValid;
        int chunkX;
        int chunkZ;
        Shape[] chunkShapes = NO_SHAPES;
        Shape[] shapes = NO_SHAPES;
        int count;
        double minY;
        double maxY;
        boolean wobbleReady;
        double[] sinX;
        double[] cosX;
        double[] sinZ;
        double[] cosZ;
        int blockX;
        int blockY;
        int blockZ;

        void loadChunk(int chunkX, int chunkZ) {
            if (this.chunkValid && this.chunkX == chunkX && this.chunkZ == chunkZ) {
                return;
            }
            this.chunkShapes = CavarynCavityGrid.this.collectChunk(chunkX, chunkZ);
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.chunkValid = true;
        }

        @Override
        public void load(int qx, int qz) {
            if (this.valid && this.qx == qx && this.qz == qz) {
                return;
            }
            this.loadChunk(qx >> 2, qz >> 2);
            Shape[] source = this.chunkShapes;
            if (this.shapes.length < source.length) {
                this.shapes = new Shape[source.length];
            }
            double minX = qx * 4.0D;
            double minZ = qz * 4.0D;
            double maxX = minX + 4.0D;
            double maxZ = minZ + 4.0D;
            int found = 0;
            double minY = Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            for (Shape shape : source) {
                if (shape.maxX < minX || shape.minX > maxX || shape.maxZ < minZ || shape.minZ > maxZ) {
                    continue;
                }
                this.shapes[found++] = shape;
                minY = Math.min(minY, shape.minY);
                maxY = Math.max(maxY, shape.maxY);
            }
            this.qx = qx;
            this.qz = qz;
            this.count = found;
            this.minY = minY;
            this.maxY = maxY;
            this.wobbleReady = false;
            this.valid = true;
        }

        @Override
        public boolean isEmpty() {
            return this.count == 0;
        }

        @Override
        public double minY() {
            return this.minY;
        }

        @Override
        public double maxY() {
            return this.maxY;
        }

        @Override
        public double signedDistance(int x, int y, int z, double floor) {
            double py = y + 0.5D;
            if (py < this.minY || py > this.maxY) {
                return Double.MAX_VALUE;
            }
            this.blockX = x;
            this.blockY = y;
            this.blockZ = z;
            double best = Double.MAX_VALUE;
            double px = x + 0.5D;
            double pz = z + 0.5D;
            Shape[] current = this.shapes;
            for (int i = 0; i < this.count; i++) {
                Shape shape = current[i];
                if (py < shape.minY || py > shape.maxY) {
                    continue;
                }
                double value = shape.signedDistance(px, py, pz, this);
                best = best == Double.MAX_VALUE ? value : smoothMin(best, value);
                if (best <= floor) {
                    return best;
                }
            }
            return best;
        }

        void prepareWobble() {
            if (this.wobbleReady) {
                return;
            }
            if (this.sinX == null) {
                this.sinX = new double[4];
                this.cosX = new double[4];
                this.sinZ = new double[4];
                this.cosZ = new double[4];
            }
            for (int i = 0; i < 4; i++) {
                double angleX = (this.qx * 4 + i + 0.5D) * 0.31D;
                double angleZ = (this.qz * 4 + i + 0.5D) * 0.27D;
                this.sinX[i] = Math.sin(angleX);
                this.cosX[i] = Math.cos(angleX);
                this.sinZ[i] = Math.sin(angleZ);
                this.cosZ[i] = Math.cos(angleZ);
            }
            this.wobbleReady = true;
        }

        double sinY() {
            int y = this.blockY;
            return y >= 0 && y < WOBBLE_Y_TABLE_SIZE ? SIN_Y[y] : Math.sin((y + 0.5D) * 0.43D);
        }

        double cosY() {
            int y = this.blockY;
            return y >= 0 && y < WOBBLE_Y_TABLE_SIZE ? COS_Y[y] : Math.cos((y + 0.5D) * 0.43D);
        }
    }

    private static final class Cavity {
        final Shape[] shapes;
        final Chamber[] chambers;
        final double minX;
        final double maxX;
        final double minZ;
        final double maxZ;

        Cavity(Shape[] shapes, Chamber[] chambers) {
            this.shapes = shapes;
            this.chambers = chambers;
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            for (Shape shape : shapes) {
                minX = Math.min(minX, shape.minX);
                maxX = Math.max(maxX, shape.maxX);
                minZ = Math.min(minZ, shape.minZ);
                maxZ = Math.max(maxZ, shape.maxZ);
            }
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }

    private abstract static class Shape {
        double minX;
        double maxX;
        double minY;
        double maxY;
        double minZ;
        double maxZ;

        abstract double signedDistance(double px, double py, double pz, ColumnCache cache);
    }

    private static final class Tube extends Shape {
        final double ax;
        final double ay;
        final double az;
        final double dx;
        final double dy;
        final double dz;
        final double lengthSq;
        final double radius;
        final double squash;
        final double floor;
        final double claw;
        final double sideX;
        final double sideZ;

        Tube(double ax, double ay, double az, double bx, double by, double bz, double radius, double squash) {
            this(ax, ay, az, bx, by, bz, radius, squash, 1.0D, 0.0D);
        }

        Tube(double ax, double ay, double az, double bx, double by, double bz, double radius, double squash, double floor, double claw) {
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.dx = bx - ax;
            this.dy = by - ay;
            this.dz = bz - az;
            this.lengthSq = this.dx * this.dx + this.dy * this.dy + this.dz * this.dz;
            this.radius = radius;
            this.squash = squash;
            this.floor = floor;
            this.claw = claw;
            double horizontal = Math.sqrt(this.dx * this.dx + this.dz * this.dz);
            this.sideX = horizontal > 1.0E-6D ? -this.dz / horizontal : 1.0D;
            this.sideZ = horizontal > 1.0E-6D ? this.dx / horizontal : 0.0D;
            double reach = radius + claw + REACH_PADDING;
            double verticalReach = (radius + claw) * Math.max(1.0D, squash) + REACH_PADDING;
            this.minX = Math.min(ax, bx) - reach;
            this.maxX = Math.max(ax, bx) + reach;
            this.minY = Math.min(ay, by) - verticalReach;
            this.maxY = Math.max(ay, by) + verticalReach;
            this.minZ = Math.min(az, bz) - reach;
            this.maxZ = Math.max(az, bz) + reach;
        }

        @Override
        double signedDistance(double px, double py, double pz, ColumnCache cache) {
            double rx = px - this.ax;
            double ry = py - this.ay;
            double rz = pz - this.az;
            double t = this.lengthSq > 0.0D ? Mth.clamp((rx * this.dx + ry * this.dy + rz * this.dz) / this.lengthSq, 0.0D, 1.0D) : 0.0D;
            double cx = rx - this.dx * t;
            double cy = ry - this.dy * t;
            double cz = rz - this.dz * t;
            double sy = cy < 0.0D ? cy / (this.squash * this.floor) : cy / this.squash;
            double radius = this.radius;
            if (this.claw > 0.0D) {
                double across = cx * this.sideX + cz * this.sideZ;
                double lengthSq = across * across + cy * cy;
                if (lengthSq > 0.0D) {
                    double inverse = 1.0D / Math.sqrt(lengthSq);
                    radius += this.claw * ridge(across * inverse, cy * inverse);
                }
            }
            return Math.sqrt(cx * cx + sy * sy + cz * cz) - radius;
        }

        private static double ridge(double cos, double sin) {
            double re2 = cos * cos - sin * sin;
            double im2 = 2.0D * cos * sin;
            double re4 = re2 * re2 - im2 * im2;
            double im4 = 2.0D * re2 * im2;
            double re8 = re4 * re4 - im4 * im4;
            double im8 = 2.0D * re4 * im4;
            return re8 * im2 + im8 * re2;
        }
    }

    public static final class Chamber extends Shape {
        final double cx;
        final double cy;
        final double cz;
        final double radius;
        final double height;
        final double depth;
        final double sinPhase;
        final double cosPhase;
        final double sinDoublePhase;
        final double cosDoublePhase;

        Chamber(double cx, double cy, double cz, double radius, double height, double depth, double phase) {
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.radius = radius;
            this.height = height;
            this.depth = depth;
            this.sinPhase = Math.sin(phase);
            this.cosPhase = Math.cos(phase);
            this.sinDoublePhase = Math.sin(phase * 2.0D);
            this.cosDoublePhase = Math.cos(phase * 2.0D);
            double reach = radius * (1.0D + WOBBLE) + REACH_PADDING;
            this.minX = cx - reach;
            this.maxX = cx + reach;
            this.minY = cy - depth * (1.0D + WOBBLE) - REACH_PADDING;
            this.maxY = cy + height * (1.0D + WOBBLE) + REACH_PADDING;
            this.minZ = cz - reach;
            this.maxZ = cz + reach;
        }

        public int x() {
            return Mth.floor(this.cx);
        }

        public int y() {
            return Mth.floor(this.cy);
        }

        public int z() {
            return Mth.floor(this.cz);
        }

        public double radius() {
            return this.radius;
        }

        public double depth() {
            return this.depth;
        }

        @Override
        double signedDistance(double px, double py, double pz, ColumnCache cache) {
            double offsetY = py - this.cy;
            double vertical = offsetY < 0.0D ? this.depth : this.height;
            double nx = (px - this.cx) / this.radius;
            double ny = offsetY / vertical;
            double nz = (pz - this.cz) / this.radius;
            cache.prepareWobble();
            int indexX = cache.blockX & 3;
            int indexZ = cache.blockZ & 3;
            double waveX = cache.sinX[indexX] * this.cosPhase + cache.cosX[indexX] * this.sinPhase;
            double waveZ = cache.sinZ[indexZ] * this.cosPhase - cache.cosZ[indexZ] * this.sinPhase;
            double waveY = cache.sinY() * this.cosDoublePhase + cache.cosY() * this.sinDoublePhase;
            double wobble = 1.0D + WOBBLE * (waveX + waveZ + waveY) / 3.0D;
            return (Math.sqrt(nx * nx + ny * ny + nz * nz) - wobble) * Math.min(this.radius, this.depth);
        }
    }
}
