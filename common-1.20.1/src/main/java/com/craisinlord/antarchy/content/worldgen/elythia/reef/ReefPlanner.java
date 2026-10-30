package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import com.craisinlord.antarchy.Antarchy;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

final class ReefPlanner {
    static final int CELL = 224;
    static final int MARGIN = 12;
    static final ResourceKey<Biome> ELYTHIA_OCEAN = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(Antarchy.MODID, "elythia_ocean"));
    private static final ResourceKey<Biome> ELYTHIA_CORAL_SPIKES = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(Antarchy.MODID, "elythia_coral_spikes"));

    private static final int MAX_RADIUS = CELL / 2 - MARGIN;
    private static final double[] SCALES = {1.0D, 0.8D, 0.62D, 0.48D, 0.36D};
    private static final double MIN_OCEAN_SHARE = 0.85D;
    private static final int EXTRA_MIN = 2;
    private static final int EXTRA_RANGE = 3;
    private static final int EXTRA_TRIES = 6;
    private static final double EXTRA_SCALE_MIN = 0.32D;
    private static final double EXTRA_SCALE_RANGE = 0.3D;
    private static final int CLAIM_GAP = 10;
    private static final int KINDS = 20;
    private static final int SMALL_MIN = 6;
    private static final int SMALL_RANGE = 7;
    private static final int SMALL_TRIES = 5;
    private static final double SMALL_SCALE_MIN = 0.16D;
    private static final double SMALL_SCALE_RANGE = 0.14D;
    private static final int SMALL_GAP = 4;
    private static final int CACHE_LIMIT = 2048;
    private static final int SURVEY = 5;
    private static final int SURVEY_MIN_OCEAN = 8;
    private static final Map<CellKey, CellPlan> PLANS = new ConcurrentHashMap<>();

    private record CellKey(long seed, int cellX, int cellZ) {
    }

    private static final class CellPlan {
        private volatile List<ReefPiece> pieces;

        List<ReefPiece> resolve(WorldGenLevel level, ChunkGenerator generator, CellKey key) {
            List<ReefPiece> resolved = this.pieces;
            if (resolved != null) {
                return resolved;
            }
            synchronized (this) {
                if (this.pieces == null) {
                    this.pieces = build(level, generator, key.seed(), key.cellX(), key.cellZ());
                }
                return this.pieces;
            }
        }
    }

    private interface PieceFactory {
        void create(ReefSite site, int x, int z, List<ReefPiece> out);
    }

    private record Part(int offsetX, int offsetZ, int radius, PieceFactory factory) {
        Part at(int x, int z) {
            return new Part(x, z, this.radius, this.factory);
        }
    }

    private record Blueprint(List<Part> parts, int radius, int minDepth) {
    }

    private ReefPlanner() {
    }

    static List<ReefPiece> pieces(WorldGenLevel level, ChunkGenerator generator, int cellX, int cellZ) {
        CellKey key = new CellKey(level.getSeed(), cellX, cellZ);
        CellPlan plan = PLANS.get(key);
        if (plan == null) {
            if (PLANS.size() > CACHE_LIMIT) {
                PLANS.clear();
            }
            plan = PLANS.computeIfAbsent(key, ignored -> new CellPlan());
        }
        return plan.resolve(level, generator, key);
    }

    private static List<ReefPiece> build(WorldGenLevel level, ChunkGenerator generator, long seed, int cellX, int cellZ) {
        RandomSource random = RandomSource.create(seed ^ cellX * 0x9E3779B97F4A7C15L ^ cellZ * 0xC2B2AE3D27D4EB4FL ^ 0x5EEDC0A1L);
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        int sea = generator.getSeaLevel();
        ReefFloorGrid grid = new ReefFloorGrid(cellX * CELL, cellZ * CELL, CELL, sea);
        ReefFloorGrid.Sampler sampler = new ReefFloorGrid.Sampler(generator, level, randomState);
        if (!survey(grid, sampler)) {
            return List.of();
        }
        ReefSite site = new ReefSite(grid, sea, random);
        List<ReefPiece> pieces = new ArrayList<>();
        List<int[]> claimed = new ArrayList<>();
        int kind = random.nextInt(KINDS);
        for (double scale : SCALES) {
            Blueprint blueprint = blueprint(random, kind, scale);
            if (blueprint.radius() > MAX_RADIUS) {
                continue;
            }
            int slack = MAX_RADIUS - blueprint.radius();
            int centerX = cellX * CELL + CELL / 2 + (slack > 0 ? random.nextInt(slack * 2 + 1) - slack : 0);
            int centerZ = cellZ * CELL + CELL / 2 + (slack > 0 ? random.nextInt(slack * 2 + 1) - slack : 0);
            if (place(site, sampler, blueprint, centerX, centerZ, claimed, pieces, CLAIM_GAP)) {
                break;
            }
        }
        int extras = EXTRA_MIN + random.nextInt(EXTRA_RANGE);
        int cellMinX = cellX * CELL + MARGIN;
        int cellMinZ = cellZ * CELL + MARGIN;
        int span = CELL - MARGIN * 2;
        for (int attempt = 0; attempt < extras * EXTRA_TRIES && extras > 0; attempt++) {
            double scale = EXTRA_SCALE_MIN + random.nextDouble() * EXTRA_SCALE_RANGE;
            Blueprint blueprint = blueprint(random, random.nextInt(KINDS), scale);
            int room = span - blueprint.radius() * 2;
            if (room <= 0) {
                continue;
            }
            int centerX = cellMinX + blueprint.radius() + random.nextInt(room + 1);
            int centerZ = cellMinZ + blueprint.radius() + random.nextInt(room + 1);
            if (place(site, sampler, blueprint, centerX, centerZ, claimed, pieces, CLAIM_GAP)) {
                extras--;
            }
        }
        int smalls = SMALL_MIN + random.nextInt(SMALL_RANGE);
        for (int attempt = 0; attempt < smalls * SMALL_TRIES && smalls > 0; attempt++) {
            Blueprint blueprint = small(random, SMALL_SCALE_MIN + random.nextDouble() * SMALL_SCALE_RANGE);
            int room = span - blueprint.radius() * 2;
            if (room <= 0) {
                continue;
            }
            int centerX = cellMinX + blueprint.radius() + random.nextInt(room + 1);
            int centerZ = cellMinZ + blueprint.radius() + random.nextInt(room + 1);
            if (place(site, sampler, blueprint, centerX, centerZ, claimed, pieces, SMALL_GAP)) {
                smalls--;
            }
        }
        return List.copyOf(pieces);
    }

    private static Blueprint small(RandomSource random, double scale) {
        float roll = random.nextFloat();
        if (roll < 0.45F) {
            return single(organ(random, scale * 1.4D), 8);
        }
        if (roll < 0.75F) {
            return single(starburst(random, scale, false), 8);
        }
        return single(singleArch(random, scale * 1.4D), 12);
    }

    private static boolean place(ReefSite site, ReefFloorGrid.Sampler sampler, Blueprint blueprint,
                                 int centerX, int centerZ, List<int[]> claimed, List<ReefPiece> pieces, int claimGap) {
        for (int[] other : claimed) {
            long dx = other[0] - centerX;
            long dz = other[1] - centerZ;
            long gap = other[2] + blueprint.radius() + claimGap;
            if (dx * dx + dz * dz < gap * gap) {
                return false;
            }
        }
        int sea = site.seaLevel();
        if (!fits(site.grid(), sampler, centerX, centerZ, blueprint.radius(), sea - blueprint.minDepth(), sea)) {
            return false;
        }
        site.grid().prefetch(sampler, centerX, centerZ, blueprint.radius());
        for (Part part : blueprint.parts()) {
            part.factory().create(site, centerX + part.offsetX(), centerZ + part.offsetZ(), pieces);
        }
        claimed.add(new int[]{centerX, centerZ, blueprint.radius()});
        return true;
    }

    private static Blueprint blueprint(RandomSource random, int kind, double scale) {
        if (kind < 2) {
            return single(atoll(random, scale), 12);
        }
        if (kind < 8) {
            return single(archGroup(random, scale), 18);
        }
        if (kind < 11) {
            return single(starburst(random, scale, random.nextFloat() < 0.5F), 10);
        }
        if (kind < 16) {
            return single(organ(random, scale), 14);
        }
        if (kind < 18) {
            return garden(random, scale);
        }
        return single(spikeForest(random, scale), 16);
    }

    private static Blueprint single(Part part, int minDepth) {
        return new Blueprint(List.of(part), part.radius(), minDepth);
    }

    private static Blueprint garden(RandomSource random, double scale) {
        int pick = random.nextInt(4);
        Part central = switch (pick) {
            case 0 -> starburst(random, scale * 0.65D, false);
            case 1 -> archGroup(random, scale * 0.5D);
            case 2 -> organ(random, scale * 0.8D);
            default -> atoll(random, scale * 0.45D);
        };
        List<Part> parts = new ArrayList<>();
        parts.add(central);
        int outer = central.radius();
        int satellites = 4 + random.nextInt(5);
        for (int i = 0; i < satellites; i++) {
            int type = random.nextInt(3);
            Part satellite = switch (type) {
                case 0 -> starburst(random, scale * 0.32D, false);
                case 1 -> organ(random, scale * 0.35D);
                default -> singleArch(random, scale * 0.55D);
            };
            for (int attempt = 0; attempt < 12; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = central.radius() + satellite.radius() + 2 + random.nextInt(10);
                if (distance + satellite.radius() > MAX_RADIUS) {
                    break;
                }
                int ox = Mth.floor(Math.cos(angle) * distance + 0.5D);
                int oz = Mth.floor(Math.sin(angle) * distance + 0.5D);
                if (overlaps(parts, ox, oz, satellite.radius())) {
                    continue;
                }
                parts.add(satellite.at(ox, oz));
                outer = Math.max(outer, Mth.ceil(distance + satellite.radius()));
                break;
            }
        }
        return new Blueprint(List.copyOf(parts), outer, 14);
    }

    private static boolean overlaps(List<Part> parts, int x, int z, int radius) {
        for (Part part : parts) {
            int dx = part.offsetX() - x;
            int dz = part.offsetZ() - z;
            int gap = part.radius() + radius + 2;
            if (dx * dx + dz * dz < gap * gap) {
                return true;
            }
        }
        return false;
    }

    private static int scaled(int value, double scale, int minimum) {
        return Math.max(minimum, Mth.floor(value * scale + 0.5D));
    }

    private static Part atoll(RandomSource random, double scale) {
        int ringRadius = scaled(36 + random.nextInt(26), scale, 12);
        int width = scaled(6 + random.nextInt(4), scale, 3);
        int flank = scaled(14 + random.nextInt(8), scale, 6);
        return new Part(0, 0, AtollPiece.radiusFor(ringRadius, width, flank),
                (site, x, z, out) -> AtollPiece.create(site, x, z, ringRadius, width, flank, out));
    }

    private static Part archGroup(RandomSource random, double scale) {
        ArchPatterns.Layout layout = ArchPatterns.pick(random, scale);
        return new Part(0, 0, layout.radius(), (site, x, z, out) -> ArchPatterns.build(site, x, z, layout, out));
    }

    private static Part singleArch(RandomSource random, double scale) {
        ArchPatterns.Layout layout = ArchPatterns.single(random, scale);
        return new Part(0, 0, layout.radius(), (site, x, z, out) -> ArchPatterns.build(site, x, z, layout, out));
    }

    private static Part starburst(RandomSource random, double scale, boolean forks) {
        int core = scaled(8 + random.nextInt(5), scale, 2);
        int length = scaled(30 + random.nextInt(22), scale, 4);
        return new Part(0, 0, StarburstPiece.radiusFor(core, length, forks),
                (site, x, z, out) -> StarburstPiece.create(site, x, z, core, length, forks, out));
    }

    private static Part organ(RandomSource random, double scale) {
        int field = scaled(12 + random.nextInt(11), scale, 3);
        return new Part(0, 0, OrganPipePiece.radiusFor(field),
                (site, x, z, out) -> OrganPipePiece.create(site, x, z, field, out));
    }

    private static Part spikeForest(RandomSource random, double scale) {
        int spread = scaled(44 + random.nextInt(20), scale, 10);
        int count = Math.max(2, Mth.floor((5 + random.nextInt(7)) * scale + 0.5D));
        List<int[]> offsets = new ArrayList<>();
        for (int attempt = 0; attempt < count * 10 && offsets.size() < count; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(random.nextDouble()) * spread;
            int ox = Mth.floor(Math.cos(angle) * distance);
            int oz = Mth.floor(Math.sin(angle) * distance);
            boolean clear = true;
            for (int[] other : offsets) {
                int dx = other[0] - ox;
                int dz = other[1] - oz;
                if (dx * dx + dz * dz < 24 * 24) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                offsets.add(new int[]{ox, oz});
            }
        }
        return new Part(0, 0, spread + SpikePiece.RADIUS, (site, x, z, out) -> {
            LongSet used = new LongOpenHashSet();
            for (int[] offset : offsets) {
                int px = x + offset[0];
                int pz = z + offset[1];
                if (used.add(ChunkPos.asLong(px >> 4, pz >> 4))) {
                    SpikePiece.create(site, px, pz, out);
                }
            }
        });
    }

    private static boolean survey(ReefFloorGrid grid, ReefFloorGrid.Sampler sampler) {
        int ocean = 0;
        int last = grid.size() - 1;
        for (int a = 0; a < SURVEY; a++) {
            for (int b = 0; b < SURVEY; b++) {
                int i = Math.round(a * last / (SURVEY - 1.0F));
                int j = Math.round(b * last / (SURVEY - 1.0F));
                if (grid.biome(sampler, i, j, ELYTHIA_OCEAN, ELYTHIA_CORAL_SPIKES) == ReefFloorGrid.OCEAN) {
                    ocean++;
                }
            }
        }
        return ocean >= SURVEY_MIN_OCEAN;
    }

    private static boolean fits(ReefFloorGrid grid, ReefFloorGrid.Sampler sampler,
                                int centerX, int centerZ, int radius, int maxFloor, int sea) {
        int ci = grid.indexX(centerX);
        int cj = grid.indexZ(centerZ);
        if (grid.biome(sampler, ci, cj, ELYTHIA_OCEAN, ELYTHIA_CORAL_SPIKES) != ReefFloorGrid.OCEAN
                || grid.height(sampler, ci, cj) > maxFloor) {
            return false;
        }
        double reach = radius * 0.85D;
        for (int k = 0; k < 8; k++) {
            double angle = k * Math.PI / 4.0D;
            int i = grid.indexX(centerX + Math.cos(angle) * reach);
            int j = grid.indexZ(centerZ + Math.sin(angle) * reach);
            if (grid.biome(sampler, i, j, ELYTHIA_OCEAN, ELYTHIA_CORAL_SPIKES) == ReefFloorGrid.SPIKES
                    || grid.height(sampler, i, j) > sea - 3) {
                return false;
            }
        }
        long limit = (long) radius * radius;
        long inner = (long) (radius * 0.6D) * (long) (radius * 0.6D);
        int total = 0;
        int ocean = 0;
        for (int i = 0; i < grid.size(); i++) {
            for (int j = 0; j < grid.size(); j++) {
                long dx = grid.pointX(i) - centerX;
                long dz = grid.pointZ(j) - centerZ;
                long distance = dx * dx + dz * dz;
                if (distance > limit) {
                    continue;
                }
                byte biome = grid.biome(sampler, i, j, ELYTHIA_OCEAN, ELYTHIA_CORAL_SPIKES);
                if (biome == ReefFloorGrid.SPIKES) {
                    return false;
                }
                int floor = grid.height(sampler, i, j);
                if (floor > sea - 3 || (distance <= inner && floor > maxFloor)) {
                    return false;
                }
                total++;
                if (biome == ReefFloorGrid.OCEAN) {
                    ocean++;
                }
            }
        }
        return total > 0 && ocean >= total * MIN_OCEAN_SHARE;
    }
}
