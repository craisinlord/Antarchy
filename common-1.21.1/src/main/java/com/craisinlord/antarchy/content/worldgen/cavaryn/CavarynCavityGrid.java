package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public final class CavarynCavityGrid {
    private static final int MAX_CACHED_CELLS = 16384;
    private static final double BLEND = 2.5;
    private static final double WOBBLE = 0.08;
    private static final double REACH_PADDING = BLEND + 1.5;
    private static final double CLAW_RIDGES = 10.0;

    private static final Map<Long, CavarynCavityGrid> BY_SALT = new ConcurrentHashMap<>();

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
        CavarynCavityGrid grid = new CavarynCavityGrid(kind, salt, startFilter);
        BY_SALT.put(salt, grid);
        return grid;
    }

    public static CavarynCavityGrid forSalt(long salt) {
        return BY_SALT.get(salt);
    }

    public Kind kind() {
        return this.kind;
    }

    public double signedDistance(int x, int y, int z) {
        Shape[] candidates = this.candidates(x, z);
        double best = Double.MAX_VALUE;
        double px = x + 0.5;
        double py = y + 0.5;
        double pz = z + 0.5;
        for (Shape shape : candidates) {
            if (py < shape.minY || py > shape.maxY) {
                continue;
            }
            double value = shape.signedDistance(px, py, pz);
            best = best == Double.MAX_VALUE ? value : smoothMin(best, value);
        }
        return best;
    }

    public int[] verticalRange(int x, int z) {
        Shape[] candidates = this.candidates(x, z);
        if (candidates.length == 0) {
            return null;
        }
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (Shape shape : candidates) {
            min = Math.min(min, shape.minY);
            max = Math.max(max, shape.maxY);
        }
        return new int[]{Mth.floor(min), Mth.ceil(max)};
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
        double h = Math.max(BLEND - Math.abs(a - b), 0.0) / BLEND;
        return Math.min(a, b) - h * h * BLEND * 0.25;
    }

    private Shape[] candidates(int x, int z) {
        int qx = x >> 2;
        int qz = z >> 2;
        ColumnCache cache = this.columns.get();
        if (cache.valid && cache.qx == qx && cache.qz == qz) {
            return cache.shapes;
        }
        double minX = qx * 4.0;
        double minZ = qz * 4.0;
        double maxX = minX + 4.0;
        double maxZ = minZ + 4.0;
        int cellX = Math.floorDiv(qx * 4, this.kind.cellSize);
        int cellZ = Math.floorDiv(qz * 4, this.kind.cellSize);
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
        cache.qx = qx;
        cache.qz = qz;
        cache.shapes = found.toArray(new Shape[0]);
        cache.valid = true;
        return cache.shapes;
    }

    private Cavity[] cavities(int cellX, int cellZ) {
        long key = ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
        Cavity[] cached = this.cells.get(key);
        if (cached != null) {
            return cached;
        }
        Cavity[] generated = this.generate(cellX, cellZ);
        if (this.cells.size() > MAX_CACHED_CELLS) {
            this.cells.clear();
        }
        Cavity[] existing = this.cells.putIfAbsent(key, generated);
        return existing != null ? existing : generated;
    }

    private Cavity[] generate(int cellX, int cellZ) {
        Kind kind = this.kind;
        if (kind == Kind.BROOD_GALLERY) {
            return this.galleryCell(cellX, cellZ);
        }
        XoroshiroRandomSource random = new XoroshiroRandomSource(mix(this.salt ^ (cellX * 0x5DEECE66DL) ^ (cellZ * 0x9E3779B97F4A7C15L)), ~this.salt);
        int count = random.nextFloat() < kind.extraChance ? kind.baseCount + 1 : kind.baseCount;
        double margin = kind.cellSize / 7.0;
        List<Cavity> cavities = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double[] start = null;
            for (int attempt = 0; attempt < 4 && start == null; attempt++) {
                double sx = cellX * (double) kind.cellSize + margin + random.nextDouble() * (kind.cellSize - margin * 2.0);
                double sz = cellZ * (double) kind.cellSize + margin + random.nextDouble() * (kind.cellSize - margin * 2.0);
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
        double x = cellX * (double) size + 16.0 + random.nextDouble() * (size - 32.0);
        double z = cellZ * (double) size + 16.0 + random.nextDouble() * (size - 32.0);
        double y = layer == 0 ? 36.0 + random.nextDouble() * 10.0 : 56.0 + random.nextDouble() * 14.0;
        return this.startFilter.accepts(x, y, z) ? new double[]{x, y, z} : null;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public enum Kind implements StringRepresentable {
        BROOD_NEST("brood_nest", CavarynBiomeTable.BROODING_TUNNELS, true, 176, 2, 1, 0.45F, 34.0, 112.0, 40.0, 76.0),
        GLOWCAP_GROTTO("glowcap_grotto", CavarynBiomeTable.GLOWCAP_CAVES, true, 160, 3, 1, 0.6F, 50.0, 188.0, 96.0, 140.0),
        BROOD_GALLERY("brood_gallery", CavarynBiomeTable.BROODING_TUNNELS, true, 96, 2, 0, 0.0F, 30.0, 92.0, 36.0, 70.0),
        MOLEWORM_WARREN("moleworm_warren", CavarynBiomeTable.MOLEWORM_CAVES, false, 160, 3, 1, 0.5F, 40.0, 184.0, 66.0, 92.0),
        CARAPACE_RIFT("carapace_rift", CavarynBiomeTable.CAVARYN, true, 160, 4, 2, 0.5F, 100.0, 186.0, 130.0, 156.0),
        BIOWART_GUT("biowart_gut", CavarynBiomeTable.BIOWART_CAVES, true, 176, 3, 1, 0.5F, 120.0, 186.0, 150.0, 172.0);

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
            double hallRadius = 14.0 + random.nextDouble() * 6.0;
            double hallHeight = 8.0 + random.nextDouble() * 3.0;
            this.chamber(x, y, z, hallRadius, hallHeight, hallHeight * 0.55);
            int galleries = 5 + random.nextInt(4);
            int exits = 2 + random.nextInt(2);
            double baseYaw = random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < galleries; i++) {
                double yaw = baseYaw + i * (Math.PI * 2.0 / galleries) + random.nextGaussian() * 0.25;
                double sx = x + Math.cos(yaw) * hallRadius * 0.7;
                double sz = z + Math.sin(yaw) * hallRadius * 0.7;
                double sy = y - hallHeight * 0.2;
                if (i < exits) {
                    double radius = 3.2 + random.nextDouble() * 0.8;
                    this.walk(sx, sy, sz, yaw, 7 + random.nextInt(5), 10.0, 14.0, radius, 0.22, 0.0);
                    continue;
                }
                double radius = 2.4 + random.nextDouble() * 0.8;
                double[] end = this.walk(sx, sy, sz, yaw, 2 + random.nextInt(3), 7.0, 11.0, radius, 0.3, 0.0);
                this.broodCell(end);
                if (random.nextFloat() < 0.35F) {
                    double branchYaw = yaw + (random.nextBoolean() ? 1.0 : -1.0) * (0.7 + random.nextDouble() * 0.5);
                    double[] branchEnd = this.walk(end[0], end[1], end[2], branchYaw, 2, 7.0, 10.0, radius * 0.9, 0.3, 0.0);
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
            if (length < 1.0) {
                return;
            }
            double sideX = -dz / length;
            double sideZ = dx / length;
            double bend = (random.nextDouble() - 0.5) * 36.0;
            double wave = (random.nextDouble() - 0.5) * 16.0;
            double rise = (random.nextDouble() - 0.5) * 6.0;
            double phase = random.nextDouble() * Math.PI * 2.0;
            double base = 7.0 + random.nextDouble() * 3.0;
            int segments = Math.max(4, Mth.ceil(length / 12.0));
            double x = a[0];
            double y = a[1];
            double z = a[2];
            double travelled = 0.0;
            double nextNiche = 12.0 + random.nextDouble() * 20.0;
            for (int i = 1; i <= segments; i++) {
                double t = i / (double) segments;
                double offset = bend * Math.sin(Math.PI * t) + wave * Math.sin(Math.PI * 2.0 * t);
                double nx = a[0] + dx * t + sideX * offset;
                double nz = a[2] + dz * t + sideZ * offset;
                double ny = Mth.lerp(t, a[1], b[1]) + rise * Math.sin(Math.PI * 2.0 * t);
                double radius = base * (1.0 + 0.12 * Math.sin(Math.PI * 3.0 * t + phase));
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 0.85, 0.4, 0.0));
                travelled += Math.sqrt((nx - x) * (nx - x) + (ny - y) * (ny - y) + (nz - z) * (nz - z));
                while (travelled >= nextNiche) {
                    double side = random.nextBoolean() ? 1.0 : -1.0;
                    double reach = radius * 0.9 + 1.5;
                    double floor = ny - radius * 0.85 * 0.4;
                    this.chamber(nx + sideX * side * reach, floor + 2.5, nz + sideZ * side * reach, 3.5, 3.0, 2.0);
                    nextNiche += 25.0 + random.nextDouble() * 15.0;
                }
                x = nx;
                y = ny;
                z = nz;
            }
        }

        void junction(double[] point) {
            double radius = 9.0 + this.random.nextDouble() * 3.0;
            this.chamber(point[0], point[1], point[2], radius, 6.0 + this.random.nextDouble() * 2.0, 3.0);
        }

        void grotto(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double radius = 18.0 + random.nextDouble() * 8.0;
            double height = 18.0 + random.nextDouble() * 10.0;
            this.chamber(x, y, z, radius, height, 5.0 + random.nextDouble() * 2.0);
            int lobes = random.nextInt(3);
            double lobeYaw = random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < lobes; i++) {
                lobeYaw += Math.PI * (0.6 + random.nextDouble() * 0.8);
                double lobeRadius = radius * (0.55 + random.nextDouble() * 0.25);
                double lobeHeight = height * (0.55 + random.nextDouble() * 0.3);
                double offset = radius * 0.75 + lobeRadius * 0.45;
                double ly = Mth.clamp(y + (random.nextDouble() - 0.5) * 8.0, this.kind.minY + 8.0, this.kind.maxY - lobeHeight * 1.2);
                this.chamber(x + Math.cos(lobeYaw) * offset, ly, z + Math.sin(lobeYaw) * offset, lobeRadius, lobeHeight, 4.0 + random.nextDouble() * 2.0);
            }
            int tunnels = 3 + random.nextInt(2);
            double baseYaw = random.nextDouble() * Math.PI * 2.0;
            for (int i = 0; i < tunnels; i++) {
                double yaw = baseYaw + i * (Math.PI * 2.0 / tunnels) + random.nextGaussian() * 0.3;
                double sx = x + Math.cos(yaw) * radius * 0.8;
                double sz = z + Math.sin(yaw) * radius * 0.8;
                boolean dive = i == 0;
                double sy = dive ? y : y + random.nextDouble() * height * 0.3;
                this.walk(sx, sy, sz, yaw, 10 + random.nextInt(5), 10.0, 14.0, 3.6 + random.nextDouble() * 1.2, dive ? 0.5 : 0.3, dive ? -0.45 : 0.0);
            }
        }

        void gut(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double yaw = random.nextDouble() * Math.PI * 2.0;
            int bulbs = 8 + random.nextInt(7);
            double neck = 2.6 + random.nextDouble() * 0.6;
            double minY = this.kind.startMinY - 4.0;
            double maxY = this.kind.startMaxY + 4.0;
            double[] middle = null;
            for (int i = 0; i < bulbs; i++) {
                double radius = 5.0 + random.nextDouble() * 3.5;
                double height = radius * (0.8 + random.nextDouble() * 0.2);
                this.chamber(x, y, z, radius, height, height * 0.75);
                if (i == bulbs / 2) {
                    middle = new double[]{x, y, z, yaw};
                }
                if (i == bulbs - 1) {
                    break;
                }
                yaw += random.nextGaussian() * 0.35;
                double length = 11.0 + random.nextDouble() * 4.0;
                double nx = x + Math.cos(yaw) * length;
                double ny = Mth.clamp(y + random.nextGaussian() * 2.0, minY, maxY);
                double nz = z + Math.sin(yaw) * length;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, neck, 1.0));
                x = nx;
                y = ny;
                z = nz;
            }
            this.walk(x, y, z, yaw, 5 + random.nextInt(3), 10.0, 14.0, 3.4 + random.nextDouble() * 0.8, 0.5, -0.4);
            if (middle != null) {
                this.walk(middle[0], middle[1], middle[2], middle[3] + Math.PI * 0.5, 5 + random.nextInt(3), 10.0, 14.0, 3.4 + random.nextDouble() * 0.8, 0.5, -0.4);
            }
        }

        void rift(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double yaw = random.nextDouble() * Math.PI * 2.0;
            double[] mid = this.slot(x, y, z, yaw, 9 + random.nextInt(6));
            double[] end = this.slot(x, y, z, yaw + Math.PI, 9 + random.nextInt(6));
            if (random.nextFloat() < 0.35F) {
                double forkYaw = yaw + (random.nextBoolean() ? 1.0 : -1.0) * (0.6 + random.nextDouble() * 0.5);
                this.slot(mid[0], mid[1], mid[2], forkYaw, 5 + random.nextInt(5));
            }
            this.walk(end[0], end[1] - 8.0, end[2], yaw + Math.PI, 5 + random.nextInt(3), 10.0, 14.0, 3.6 + random.nextDouble() * 0.8, 0.5, -0.4);
            this.walk(mid[0], mid[1] - 8.0, mid[2], yaw, 5 + random.nextInt(3), 10.0, 14.0, 3.6 + random.nextDouble() * 0.8, 0.5, -0.4);
        }

        private double[] slot(double x, double y, double z, double yaw, int segments) {
            XoroshiroRandomSource random = this.random;
            double phase = random.nextDouble() * Math.PI * 2.0;
            double squash = 4.2 + random.nextDouble() * 1.0;
            for (int i = 0; i < segments; i++) {
                yaw += random.nextGaussian() * 0.22;
                double length = 10.0 + random.nextDouble() * 4.0;
                double nx = x + Math.cos(yaw) * length;
                double ny = Mth.clamp(y + random.nextGaussian() * 1.5, this.kind.startMinY - 6.0, this.kind.startMaxY + 6.0);
                double nz = z + Math.sin(yaw) * length;
                double taper = Math.min(1.0, Math.min(i + 1, segments - i) / 2.0);
                double radius = (4.2 + 0.9 * Math.sin(i * 0.7 + phase)) * (0.55 + 0.45 * taper);
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, squash));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x, y, z};
        }

        void warren(double x, double y, double z) {
            XoroshiroRandomSource random = this.random;
            double yaw = random.nextDouble() * Math.PI * 2.0;
            double[] hub = {x, y, z};
            for (int level = 0; level < 3 && hub != null; level++) {
                double radius = 13.0 + random.nextDouble() * 5.0;
                double height = radius * (0.75 + random.nextDouble() * 0.15);
                this.chamber(hub[0], hub[1], hub[2], radius, height, height * 0.45);
                int tunnels = 3 + random.nextInt(2);
                double baseYaw = random.nextDouble() * Math.PI * 2.0;
                for (int i = 0; i < tunnels; i++) {
                    double tunnelYaw = baseYaw + i * (Math.PI * 2.0 / tunnels) + random.nextGaussian() * 0.3;
                    double sx = hub[0] + Math.cos(tunnelYaw) * radius * 0.6;
                    double sz = hub[2] + Math.sin(tunnelYaw) * radius * 0.6;
                    double[] tunnelEnd = this.moleTunnel(sx, hub[1], sz, tunnelYaw, 6 + random.nextInt(4));
                    if (random.nextFloat() < 0.4F) {
                        double endRadius = 8.0 + random.nextDouble() * 2.0;
                        this.chamber(tunnelEnd[0], tunnelEnd[1], tunnelEnd[2], endRadius, endRadius * 0.8, endRadius * 0.35);
                    }
                }
                if (random.nextFloat() < 0.6F) {
                    double shaftRadius = 4.0 + random.nextDouble();
                    double top = hub[1] + height * 0.6;
                    double bottom = Math.max(this.kind.minY, hub[1] - 30.0);
                    this.shapes.add(new Tube(hub[0], top, hub[2], hub[0] + 0.01, bottom, hub[2], shaftRadius, 1.0, 1.0, 0.0));
                }
                yaw += Math.PI * (0.5 + random.nextDouble());
                hub = level < 2 ? this.ramp(hub[0] + Math.cos(yaw) * radius * 0.7, hub[1], hub[2] + Math.sin(yaw) * radius * 0.7, yaw, 26.0 + random.nextDouble() * 6.0) : null;
            }
        }

        private double[] moleTunnel(double x, double y, double z, double yaw, int segments) {
            XoroshiroRandomSource random = this.random;
            double radius = 7.0 + random.nextDouble() * 2.0;
            double pitch = 0.0;
            for (int i = 0; i < segments; i++) {
                yaw += random.nextGaussian() * 0.3;
                pitch = Mth.clamp(pitch * 0.6 + random.nextGaussian() * 0.05, -0.12, 0.12);
                double length = 10.0 + random.nextDouble() * 4.0;
                double horizontal = Math.cos(pitch) * length;
                double nx = x + Math.cos(yaw) * horizontal;
                double ny = Mth.clamp(y + Math.sin(pitch) * length, this.kind.minY + 8.0, this.kind.maxY - 12.0);
                double nz = z + Math.sin(yaw) * horizontal;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 1.0, 0.45, 0.5));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x, y, z};
        }

        private double[] ramp(double x, double y, double z, double yaw, double climb) {
            XoroshiroRandomSource random = this.random;
            double target = y + climb;
            if (target > this.kind.maxY - 20.0) {
                return null;
            }
            double radius = 7.0 + random.nextDouble() * 1.5;
            double pitch = 0.45 + random.nextDouble() * 0.25;
            while (y < target) {
                yaw += random.nextGaussian() * 0.2;
                double length = 10.0 + random.nextDouble() * 3.0;
                double horizontal = Math.cos(pitch) * length;
                double nx = x + Math.cos(yaw) * horizontal;
                double ny = Math.min(target, y + Math.sin(pitch) * length);
                double nz = z + Math.sin(yaw) * horizontal;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 1.0, 0.45, 0.5));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x + Math.cos(yaw) * 8.0, y, z + Math.sin(yaw) * 8.0};
        }

        private void broodCell(double[] center) {
            double radius = 4.5 + this.random.nextDouble() * 2.0;
            double height = 3.5 + this.random.nextDouble() * 1.5;
            this.chamber(center[0], center[1], center[2], radius, height, height * 0.7);
        }

        private void chamber(double x, double y, double z, double radius, double height, double depth) {
            Chamber chamber = new Chamber(x, y, z, radius, height, depth, this.random.nextDouble() * Math.PI * 2.0);
            this.shapes.add(chamber);
            this.chambers.add(chamber);
        }

        private double[] walk(double x, double y, double z, double yaw, int segments, double minLength, double maxLength, double radius, double maxPitch, double pitchBias) {
            return this.walk(x, y, z, yaw, segments, minLength, maxLength, radius, maxPitch, pitchBias, 0.3);
        }

        private double[] walk(double x, double y, double z, double yaw, int segments, double minLength, double maxLength, double radius, double maxPitch, double pitchBias, double biasWeight) {
            XoroshiroRandomSource random = this.random;
            double minY = this.kind.minY;
            double maxY = this.kind.maxY;
            double pitch = (random.nextDouble() - 0.5) * maxPitch;
            for (int i = 0; i < segments; i++) {
                yaw += random.nextGaussian() * 0.3;
                pitch = Mth.clamp(pitch * (1.0 - biasWeight) + pitchBias * biasWeight + random.nextGaussian() * 0.12, -maxPitch, maxPitch);
                if (y < minY + 4.0) {
                    pitch = Math.abs(pitch) + 0.05;
                } else if (y > maxY - 4.0) {
                    pitch = -Math.abs(pitch) - 0.05;
                }
                double length = minLength + random.nextDouble() * (maxLength - minLength);
                double horizontal = Math.cos(pitch) * length;
                double nx = x + Math.cos(yaw) * horizontal;
                double ny = Mth.clamp(y + Math.sin(pitch) * length, minY, maxY);
                double nz = z + Math.sin(yaw) * horizontal;
                this.shapes.add(new Tube(x, y, z, nx, ny, nz, radius, 1.0));
                x = nx;
                y = ny;
                z = nz;
            }
            return new double[]{x, y, z};
        }
    }

    private static final class ColumnCache {
        boolean valid;
        int qx;
        int qz;
        Shape[] shapes = new Shape[0];
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

        abstract double signedDistance(double px, double py, double pz);
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
            this(ax, ay, az, bx, by, bz, radius, squash, 1.0, 0.0);
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
            this.sideX = horizontal > 1.0E-6 ? -this.dz / horizontal : 1.0;
            this.sideZ = horizontal > 1.0E-6 ? this.dx / horizontal : 0.0;
            double reach = radius + claw + REACH_PADDING;
            double verticalReach = (radius + claw) * Math.max(1.0, squash) + REACH_PADDING;
            this.minX = Math.min(ax, bx) - reach;
            this.maxX = Math.max(ax, bx) + reach;
            this.minY = Math.min(ay, by) - verticalReach;
            this.maxY = Math.max(ay, by) + verticalReach;
            this.minZ = Math.min(az, bz) - reach;
            this.maxZ = Math.max(az, bz) + reach;
        }

        @Override
        double signedDistance(double px, double py, double pz) {
            double rx = px - this.ax;
            double ry = py - this.ay;
            double rz = pz - this.az;
            double t = this.lengthSq > 0.0 ? Mth.clamp((rx * this.dx + ry * this.dy + rz * this.dz) / this.lengthSq, 0.0, 1.0) : 0.0;
            double cx = rx - this.dx * t;
            double cy = ry - this.dy * t;
            double cz = rz - this.dz * t;
            double sy = cy < 0.0 ? cy / (this.squash * this.floor) : cy / this.squash;
            double radius = this.radius;
            if (this.claw > 0.0) {
                radius += this.claw * Math.sin(Math.atan2(cy, cx * this.sideX + cz * this.sideZ) * CLAW_RIDGES);
            }
            return Math.sqrt(cx * cx + sy * sy + cz * cz) - radius;
        }
    }

    public static final class Chamber extends Shape {
        final double cx;
        final double cy;
        final double cz;
        final double radius;
        final double height;
        final double depth;
        final double phase;

        Chamber(double cx, double cy, double cz, double radius, double height, double depth, double phase) {
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.radius = radius;
            this.height = height;
            this.depth = depth;
            this.phase = phase;
            double reach = radius * (1.0 + WOBBLE) + REACH_PADDING;
            this.minX = cx - reach;
            this.maxX = cx + reach;
            this.minY = cy - depth * (1.0 + WOBBLE) - REACH_PADDING;
            this.maxY = cy + height * (1.0 + WOBBLE) + REACH_PADDING;
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
        double signedDistance(double px, double py, double pz) {
            double offsetY = py - this.cy;
            double vertical = offsetY < 0.0 ? this.depth : this.height;
            double nx = (px - this.cx) / this.radius;
            double ny = offsetY / vertical;
            double nz = (pz - this.cz) / this.radius;
            double wobble = 1.0 + WOBBLE * (Math.sin(px * 0.31 + this.phase) + Math.sin(pz * 0.27 - this.phase) + Math.sin(py * 0.43 + this.phase * 2.0)) / 3.0;
            return (Math.sqrt(nx * nx + ny * ny + nz * nz) - wobble) * Math.min(this.radius, this.depth);
        }
    }
}
