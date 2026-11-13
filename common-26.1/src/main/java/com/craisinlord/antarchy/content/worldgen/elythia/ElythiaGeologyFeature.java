package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

public final class ElythiaGeologyFeature extends Feature<NoneFeatureConfiguration> {
    public static final TagKey<Block> NATURAL_STONE = TagKey.create(Registries.BLOCK, id("elythia_natural_stone"));
    private static final TagKey<Biome> ARKROSE_BIOMES = TagKey.create(Registries.BIOME, id("arkrose_biomes"));
    private static final TagKey<Biome> CONGLOMERATE_BIOMES = TagKey.create(Registries.BIOME, id("conglomerate_biomes"));
    private static final TagKey<Biome> STRATA_BIOMES = TagKey.create(Registries.BIOME, id("elythia_strata_biomes"));
    private static final int LENS_CELL = 32;
    private static final int LENS_HEIGHT = 24;
    private static final int MAX_RADIUS = 11;
    private static final int GRID_MARGIN = ElythiaStrata.TIDE_RADIUS;
    private static final int GRID_SIZE = 16 + GRID_MARGIN * 2;
    private static final int NO_WATER = Integer.MIN_VALUE;
    private static final int PEACH_SEARCH = 48;

    public ElythiaGeologyFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int startX = context.origin().getX() & ~15;
        int startZ = context.origin().getZ() & ~15;
        BlockState rockite = state("rockite");
        BlockState arkrose = state("arkrose");
        removeLooseRock(level, startX, startZ);
        int[] tops = new int[GRID_SIZE * GRID_SIZE];
        int[] waterlines = new int[GRID_SIZE * GRID_SIZE];
        boolean anyWater = readSurfaceGrid(level, startX, startZ, tops, waterlines);
        StrataPalette palette = new StrataPalette(rockite, state("zebra_stone"), state("conglomerate"), arkrose, state("tidestone"));
        boolean placed = placeStrata(level, context.chunkGenerator(), startX, startZ, tops, anyWater ? waterlines : null, palette);
        placed |= placeLenses(level, startX, startZ, rockite, palette.zebraStone(), 96, 256, 0x6D1C7B3AL, biome -> !biome.is(STRATA_BIOMES));
        if (hasSurfaceBiome(level, startX, startZ, tops, ARKROSE_BIOMES)) {
            placed |= placeLenses(level, startX, startZ, rockite, arkrose, 40, level.getMaxY(), 0x473A2D91L, biome -> biome.is(ARKROSE_BIOMES));
        }
        placed |= placeBeds(level, startX, startZ, tops, rockite, palette.tidestone(), palette.conglomerate(), state("shellstone"), state("loam"));
        return placed;
    }

    private record StrataPalette(BlockState rockite, BlockState zebraStone, BlockState conglomerate, BlockState arkrose, BlockState tidestone) {
        BlockState of(ElythiaStrata.Layer layer) {
            return switch (layer) {
                case ZEBRA -> this.zebraStone;
                case CONGLOMERATE -> this.conglomerate;
                case ARKROSE -> this.arkrose;
                case TIDESTONE -> this.tidestone;
                case BASE -> this.rockite;
            };
        }
    }

    private static boolean placeStrata(WorldGenLevel level, ChunkGenerator generator, int startX, int startZ, int[] tops,
                                       @Nullable int[] waterlines, StrataPalette palette) {
        long seed = level.getSeed();
        int minY = level.getMinY();
        Block rockite = palette.rockite().getBlock();
        Holder<Biome> lastBiome = null;
        ElythiaStrataProfile lastProfile = null;
        Boolean nearPeach = null;
        boolean placed = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                int localX = x - startX;
                int localZ = z - startZ;
                int top = tops[gridIndex(localX, localZ)];
                int waterline = waterlines == null ? NO_WATER : waterlineNear(waterlines, localX, localZ, top);
                if (waterline != NO_WATER) {
                    int upper = Math.min(top, waterline + ElythiaStrata.MAX_TIDE);
                    int lower = Math.max(minY, waterline - ElythiaStrata.TIDE_BELOW);
                    for (int y = upper; y >= lower; y--) {
                        cursor.set(x, y, z);
                        if (level.getBlockState(cursor).is(rockite) && ElythiaStrata.isTide(seed, x, y, z, waterline)) {
                            level.setBlock(cursor, palette.of(ElythiaStrata.tideLayer(seed, x, y, z)), 2);
                            placed = true;
                        }
                    }
                }
                Holder<Biome> biome = level.getNoiseBiome(
                        QuartPos.fromBlock(x + ElythiaStrata.biomeJitterX(seed, x, z)),
                        QuartPos.fromBlock(top),
                        QuartPos.fromBlock(z + ElythiaStrata.biomeJitterZ(seed, x, z)));
                if (biome != lastBiome) {
                    lastBiome = biome;
                    lastProfile = biome.is(STRATA_BIOMES)
                            ? biome.unwrapKey().flatMap(key -> ElythiaStrataProfile.forBiome(key.identifier().toString())).orElse(null)
                            : null;
                }
                ElythiaStrataProfile profile = lastProfile;
                if (profile == null) {
                    continue;
                }
                if (nearPeach == null) {
                    nearPeach = nearPeach(level, generator, startX, startZ);
                }
                int capDepth = ElythiaStrata.capDepth(seed, profile, x, z);
                boolean veins = ElythiaStrata.veinRegion(seed, profile, x, z, nearPeach);
                int nearby = lowestNearby(tops, localX, localZ);
                int bottom = Math.max(minY, ElythiaStrata.paintBottom(top, nearby, capDepth));
                double bedOffset = ElythiaStrata.bedOffset(seed, profile, x, z);
                int runTop = top;
                int gapStart = NO_WATER;
                for (int y = top; y >= minY && (y >= bottom || gapStart != NO_WATER); y--) {
                    cursor.set(x, y, z);
                    BlockState current = level.getBlockState(cursor);
                    if (current.isAir() || !current.getFluidState().isEmpty()) {
                        if (gapStart == NO_WATER && runTop - y <= ElythiaStrata.MAX_LIP_RUN && nearby < y) {
                            gapStart = y;
                        }
                        if (gapStart != NO_WATER && gapStart - y > ElythiaStrata.MAX_LIP_GAP) {
                            break;
                        }
                        continue;
                    }
                    if (gapStart != NO_WATER) {
                        gapStart = NO_WATER;
                        runTop = y;
                        bottom = Math.max(minY, Math.min(bottom, ElythiaStrata.paintBottom(y, y, capDepth)));
                    }
                    if (!current.is(rockite)) {
                        continue;
                    }
                    ElythiaStrata.Layer layer = veins && ElythiaStrata.inVein(seed, x, y, z)
                            ? ElythiaStrata.Layer.ARKROSE : ElythiaStrata.capLayer(runTop - y, capDepth);
                    if (layer == ElythiaStrata.Layer.BASE) {
                        layer = ElythiaStrata.layerAt(seed, profile, y, bedOffset);
                    }
                    if (layer != ElythiaStrata.Layer.BASE) {
                        level.setBlock(cursor, palette.of(layer), 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private static boolean hasSurfaceBiome(WorldGenLevel level, int startX, int startZ, int[] tops, TagKey<Biome> tag) {
        for (int localX = 0; localX < 16; localX += 4) {
            for (int localZ = 0; localZ < 16; localZ += 4) {
                int top = tops[gridIndex(localX, localZ)];
                if (level.getNoiseBiome(QuartPos.fromBlock(startX + localX), QuartPos.fromBlock(top), QuartPos.fromBlock(startZ + localZ)).is(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean readSurfaceGrid(WorldGenLevel level, int startX, int startZ, int[] tops, int[] waterlines) {
        boolean anyWater = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Block lumen = state("lumen").getBlock();
        for (int dx = 0; dx < GRID_SIZE; dx++) {
            for (int dz = 0; dz < GRID_SIZE; dz++) {
                int x = startX - GRID_MARGIN + dx;
                int z = startZ - GRID_MARGIN + dz;
                int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                int index = dx * GRID_SIZE + dz;
                tops[index] = top;
                waterlines[index] = NO_WATER;
                if (surface > top) {
                    cursor.set(x, surface, z);
                    if (level.getFluidState(cursor).is(FluidTags.WATER) || level.getBlockState(cursor).is(lumen)) {
                        waterlines[index] = surface;
                        anyWater = true;
                    }
                }
            }
        }
        return anyWater;
    }

    private static int gridIndex(int localX, int localZ) {
        return (localX + GRID_MARGIN) * GRID_SIZE + localZ + GRID_MARGIN;
    }

    private static int waterlineNear(int[] waterlines, int localX, int localZ, int top) {
        int radius = ElythiaStrata.TIDE_RADIUS;
        int best = NO_WATER;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int waterline = waterlines[gridIndex(localX + dx, localZ + dz)];
                if (waterline != NO_WATER && waterline <= top + 1 && waterline > best) {
                    best = waterline;
                }
            }
        }
        return best;
    }

    private static int lowestNearby(int[] tops, int localX, int localZ) {
        int radius = ElythiaStrata.EXPOSURE_RADIUS;
        int lowest = Integer.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                lowest = Math.min(lowest, tops[gridIndex(localX + dx, localZ + dz)]);
            }
        }
        return lowest;
    }

    private static boolean nearPeach(WorldGenLevel level, ChunkGenerator generator, int startX, int startZ) {
        Climate.Sampler sampler = level.getLevel().getChunkSource().randomState().sampler();
        int quartY = QuartPos.fromBlock(level.getSeaLevel());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int quartX = QuartPos.fromBlock(startX + 8 + dx * PEACH_SEARCH);
                int quartZ = QuartPos.fromBlock(startZ + 8 + dz * PEACH_SEARCH);
                if (generator.getBiomeSource().getNoiseBiome(quartX, quartY, quartZ, sampler).is(ARKROSE_BIOMES)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean placeLenses(WorldGenLevel level, int startX, int startZ, BlockState rockite,
                                       BlockState replacement, int lowerY, int upperY, long salt, Predicate<Holder<Biome>> allowed) {
        int minY = Math.max(level.getMinY(), lowerY);
        int maxY = Math.min(level.getMaxY(), upperY);
        if (minY > maxY) {
            return false;
        }
        long seed = level.getSeed() ^ salt;
        boolean placed = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cellX = Math.floorDiv(startX - MAX_RADIUS, LENS_CELL); cellX <= Math.floorDiv(startX + 15 + MAX_RADIUS, LENS_CELL); cellX++) {
            for (int cellZ = Math.floorDiv(startZ - MAX_RADIUS, LENS_CELL); cellZ <= Math.floorDiv(startZ + 15 + MAX_RADIUS, LENS_CELL); cellZ++) {
                for (int cellY = Math.floorDiv(minY - 4, LENS_HEIGHT); cellY <= Math.floorDiv(maxY + 4, LENS_HEIGHT); cellY++) {
                    long key = hash(seed, cellX, cellY, cellZ);
                    if (unit(key) >= 0.55D) {
                        continue;
                    }
                    int centerX = cellX * LENS_CELL + (int) (unit(mix(key + 1)) * LENS_CELL);
                    int centerY = cellY * LENS_HEIGHT + (int) (unit(mix(key + 2)) * LENS_HEIGHT);
                    int centerZ = cellZ * LENS_CELL + (int) (unit(mix(key + 3)) * LENS_CELL);
                    int radiusX = 6 + (int) (unit(mix(key + 4)) * 6);
                    int radiusY = 2 + (int) (unit(mix(key + 5)) * 3);
                    int radiusZ = 6 + (int) (unit(mix(key + 6)) * 6);
                    for (int x = Math.max(startX, centerX - radiusX); x <= Math.min(startX + 15, centerX + radiusX); x++) {
                        double dx = (x - centerX) / (double) radiusX;
                        for (int z = Math.max(startZ, centerZ - radiusZ); z <= Math.min(startZ + 15, centerZ + radiusZ); z++) {
                            double dz = (z - centerZ) / (double) radiusZ;
                            double horizontal = dx * dx + dz * dz;
                            if (horizontal > 1.1D) {
                                continue;
                            }
                            for (int y = Math.max(minY, centerY - radiusY); y <= Math.min(maxY, centerY + radiusY); y++) {
                                double dy = (y - centerY) / (double) radiusY;
                                if (horizontal + dy * dy > 0.92D + unit(hash(key, x, y, z)) * 0.16D) {
                                    continue;
                                }
                                cursor.set(x, y, z);
                                if (level.getBlockState(cursor).is(rockite.getBlock()) && allowed.test(level.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)))) {
                                    level.setBlock(cursor, replacement, 2);
                                    placed = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return placed;
    }

    private static boolean placeBeds(WorldGenLevel level, int startX, int startZ, int[] tops, BlockState rockite,
                                     BlockState tidestone, BlockState conglomerate, BlockState shellstone, BlockState loam) {
        boolean placed = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        long seed = level.getSeed();
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                int floorY = tops[gridIndex(x - startX, z - startZ)];
                if (floorY <= level.getMinY() || floorY >= level.getMaxY()) {
                    continue;
                }
                cursor.set(x, floorY + 1, z);
                if (level.getFluidState(cursor).is(FluidTags.WATER)) {
                    placed |= placeTidestoneColumn(level, x, floorY, z, rockite, tidestone, shellstone, cursor);
                }
                if (!level.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(floorY), QuartPos.fromBlock(z)).is(CONGLOMERATE_BIOMES)) {
                    continue;
                }
                int bandStart = -1;
                int thickness = 2 + (int) (unit(hash(seed ^ 0x38A1BC5DL, x, 0, z)) * 4);
                for (int depth = 1; depth <= 24; depth++) {
                    int y = floorY - depth;
                    if (y < level.getMinY()) {
                        break;
                    }
                    cursor.set(x, y, z);
                    BlockState current = level.getBlockState(cursor);
                    boolean substrate = current.is(rockite.getBlock()) || current.is(loam.getBlock()) || current.is(Blocks.DIRT)
                            || current.is(Blocks.COARSE_DIRT) || current.is(Blocks.ROOTED_DIRT);
                    if (bandStart < 0 && substrate) {
                        bandStart = depth;
                    }
                    if (bandStart >= 0 && depth >= bandStart + thickness) {
                        break;
                    }
                    if (substrate) {
                        level.setBlock(cursor, conglomerate, 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    public static void placePondBed(WorldGenLevel level, BlockPos floor) {
        if (floor.getY() <= level.getMinY() || floor.getY() >= level.getMaxY()
                || !level.getFluidState(floor.above()).is(FluidTags.WATER)) {
            return;
        }
        placeTidestoneColumn(level, floor.getX(), floor.getY(), floor.getZ(), state("rockite"), state("tidestone"),
                state("shellstone"), new BlockPos.MutableBlockPos());
    }

    private static boolean placeTidestoneColumn(WorldGenLevel level, int x, int floorY, int z, BlockState rockite,
                                                BlockState tidestone, BlockState shellstone, BlockPos.MutableBlockPos cursor) {
        double patch = patchDistance(level.getSeed() ^ 0x7C4E21B9L, x, z);
        if (patch >= 1.0D) {
            return false;
        }
        boolean placed = false;
        int halfDepth = Math.max(1, (int) (5 * Math.sqrt(1.0D - patch)));
        for (int depth = 7 - halfDepth; depth <= 7 + halfDepth; depth++) {
            int y = floorY - depth;
            if (y < level.getMinY()) {
                break;
            }
            cursor.set(x, y, z);
            BlockState current = level.getBlockState(cursor);
            if (current.is(rockite.getBlock()) || current.is(shellstone.getBlock())) {
                level.setBlock(cursor, tidestone, 2);
                placed = true;
            }
        }
        return placed;
    }

    private static double patchDistance(long seed, int x, int z) {
        double best = Double.POSITIVE_INFINITY;
        int cellX = Math.floorDiv(x, LENS_CELL);
        int cellZ = Math.floorDiv(z, LENS_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long key = hash(seed, cellX + dx, 0, cellZ + dz);
                if (unit(key) > 0.7D) {
                    continue;
                }
                double centerX = (cellX + dx) * LENS_CELL + unit(mix(key + 1)) * LENS_CELL;
                double centerZ = (cellZ + dz) * LENS_CELL + unit(mix(key + 2)) * LENS_CELL;
                double radius = 8 + unit(mix(key + 3)) * 6;
                double distanceX = (x - centerX) / radius;
                double distanceZ = (z - centerZ) / radius;
                best = Math.min(best, distanceX * distanceX + distanceZ * distanceZ);
            }
        }
        return best;
    }

    private static long hash(long seed, int x, int y, int z) {
        return mix(seed ^ x * 0x632BE59BD9B4E019L ^ y * 0x9E3779B97F4A7C15L ^ z * 0x85157AF5D66D3EABL);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("antarchy", path);
    }

    private static BlockState state(String path) {
        return BuiltInRegistries.BLOCK.getOptional(id(path))
                .orElseThrow(() -> new IllegalStateException("Missing Elythia geology block: " + path)).defaultBlockState();
    }
    private static void removeLooseRock(WorldGenLevel level, int startX, int startZ) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        Block loam = state("loam").getBlock();
        Block primordialLoam = state("primordial_loam").getBlock();
        Block primordialMoss = state("primordial_moss_block").getBlock();
        Block shellstone = state("shellstone").getBlock();
        ElythiaLooseRock.Cells cells = (x, y, z) -> {
            BlockState state = level.getBlockState(probe.set(x, y, z));
            if (state.isAir() || !state.getFluidState().isEmpty() || isSurfacePlant(state)) {
                return ElythiaLooseRock.Cell.OPEN;
            }
            return isLooseRockTerrain(state) || state.is(loam) || state.is(primordialLoam) || state.is(primordialMoss) || state.is(shellstone)
                    ? ElythiaLooseRock.Cell.TERRAIN : ElythiaLooseRock.Cell.ANCHOR;
        };
        ChunkAccess chunk = level.getChunk(startX >> 4, startZ >> 4);
        Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockState air = Blocks.AIR.defaultBlockState();
        shaveNeedles(level, startX, startZ, cells, floor, surface);
        LongOpenHashSet anchored = new LongOpenHashSet();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = level.getMinY();
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                if (!level.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(top), QuartPos.fromBlock(z)).is(STRATA_BIOMES)
                        || !ElythiaLooseRock.isTopRunThin(cells, x, top, z)) {
                    continue;
                }
                LongArrayList piece = ElythiaLooseRock.floatingPiece(cells, x, top, z, startX - 16, startX + 31, minY, startZ - 16, startZ + 31, anchored);
                piece.sort((a, b) -> Integer.compare(BlockPos.getY(b), BlockPos.getY(a)));
                for (int i = 0; i < piece.size(); i++) {
                    long position = piece.getLong(i);
                    int px = BlockPos.getX(position);
                    int py = BlockPos.getY(position);
                    int pz = BlockPos.getZ(position);
                    if (px >> 4 != startX >> 4 || pz >> 4 != startZ >> 4) {
                        continue;
                    }
                    clearPlantsAbove(level, px, py, pz, surface);
                    level.setBlock(cursor.set(px, py, pz), air, 2);
                    floor.update(px & 15, py, pz & 15, air);
                    surface.update(px & 15, py, pz & 15, air);
                }
            }
        }
    }

    private static void shaveNeedles(WorldGenLevel level, int startX, int startZ, ElythiaLooseRock.Cells cells, Heightmap floor, Heightmap surface) {
        if (!hasCappedBiome(level, startX, startZ)) {
            return;
        }
        int margin = ElythiaLooseRock.NEEDLE_RADIUS * 2;
        int size = 16 + margin * 2;
        int[] tops = new int[size * size];
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                tops[dx * size + dz] = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, startX - margin + dx, startZ - margin + dz) - 1;
            }
        }
        int[] opened = ElythiaLooseRock.opened(tops, size, ElythiaLooseRock.NEEDLE_RADIUS);
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int index = (localX + margin) * size + localZ + margin;
                int top = tops[index];
                int target = opened[index];
                int x = startX + localX;
                int z = startZ + localZ;
                if (top - target < ElythiaLooseRock.NEEDLE_RISE
                        || top - target > ElythiaLooseRock.NEEDLE_MAX_DEPTH
                        || !isCapped(level.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(top), QuartPos.fromBlock(z)))
                        || cells.at(x, top, z) != ElythiaLooseRock.Cell.TERRAIN) {
                    continue;
                }
                BlockState soil = level.getBlockState(cursor.set(x, top, z));
                clearPlantsAbove(level, x, top, z, surface);
                for (int y = top; y > target && cells.at(x, y, z) == ElythiaLooseRock.Cell.TERRAIN; y--) {
                    level.setBlock(cursor.set(x, y, z), air, 2);
                    floor.update(localX, y, localZ, air);
                    surface.update(localX, y, localZ, air);
                }
                if (cells.at(x, target, z) == ElythiaLooseRock.Cell.TERRAIN) {
                    level.setBlock(cursor.set(x, target, z), soil, 2);
                }
            }
        }
    }

    private static void clearPlantsAbove(WorldGenLevel level, int x, int y, int z, Heightmap surface) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, y + 1, z);
        BlockState air = Blocks.AIR.defaultBlockState();
        while (cursor.getY() < level.getMaxY() + 1 && isSurfacePlant(level.getBlockState(cursor))) {
            level.setBlock(cursor, air, 2);
            surface.update(x & 15, cursor.getY(), z & 15, air);
            cursor.move(0, 1, 0);
        }
    }

    private static boolean isSurfacePlant(BlockState state) {
        return state.getFluidState().isEmpty() && (state.getBlock() instanceof BushBlock || state.getBlock() instanceof CarpetBlock);
    }

    private static boolean hasCappedBiome(WorldGenLevel level, int startX, int startZ) {
        for (int localX = 0; localX < 16; localX += 4) {
            for (int localZ = 0; localZ < 16; localZ += 4) {
                int x = startX + localX;
                int z = startZ + localZ;
                int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                if (isCapped(level.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(top), QuartPos.fromBlock(z)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCapped(Holder<Biome> biome) {
        return biome.is(STRATA_BIOMES) && biome.unwrapKey()
                .flatMap(key -> ElythiaStrataProfile.forBiome(key.identifier().toString()))
                .map(ElythiaStrataProfile::hasCap).orElse(false);
    }

    private static boolean isLooseRockTerrain(BlockState state) {
        return state.is(NATURAL_STONE) || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.DEEPSLATE) || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

}
