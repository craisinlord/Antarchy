package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public final class SkyIsleTerrainFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_GAP = 24;
    private static final int LAKE_ISLAND_GAP = 30;
    private static final int SOIL_DEPTH = 3;
    private static final double SPILL_EDGE = 2.0D;
    private static final double LILY_CHANCE = 0.08D;
    private static final double RUIN_CHANCE = 0.3D;
    private static final long COVER_SALT = 0xA1C3E5F7L;
    private static final long PLANT_SALT = 0x5B7D9F13L;
    private static final ResourceKey<ConfiguredFeature<?, ?>> ZEBRA_RUIN = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, "zebra_stone_ruin"));

    private static final EnumSet<Heightmap.Types> WORLDGEN_HEIGHTMAPS = EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG);

    private static volatile Palette palette;

    public SkyIsleTerrainFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int startX = context.origin().getX() & ~15;
        int startZ = context.origin().getZ() & ~15;
        long seed = level.getSeed();
        List<SkyIsleGrid.Island> islands = SkyIsleGrid.forSeed(seed).islandsTouching(startX, startZ, startX + 15, startZ + 15);
        if (islands.isEmpty()) {
            return false;
        }

        Palette palette = palette();
        ChunkAccess chunk = level.getChunk(startX >> 4, startZ >> 4);
        int minY = level.getMinY() + 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placed = false;

        int[] floors = new int[256];
        SkyIsleGrid.Island[] craterOwners = new SkyIsleGrid.Island[256];
        double[] craterRatios = new double[256];
        SkyIsleGrid.Island[] columnIslands = new SkyIsleGrid.Island[256];
        double[] insets = new double[256];
        int[] tops = new int[256];
        int[] bottoms = new int[256];
        int count = islands.size();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = startX + localX;
                int z = startZ + localZ;
                int index = localX << 4 | localZ;
                floors[index] = SkyIsleGrid.NO_HEIGHT;
                SkyIsleGrid.Island owner = null;
                double ownerInset = 0.0D;
                int craterDepth = 0;
                for (int i = 0; i < count; i++) {
                    SkyIsleGrid.Island candidate = islands.get(i);
                    double inset = candidate.insetAt(x, z);
                    if (inset == -Double.MAX_VALUE) {
                        continue;
                    }
                    int depth = candidate.craterDepthFor(inset);
                    if (depth > craterDepth) {
                        craterDepth = depth;
                        owner = candidate;
                        ownerInset = inset;
                    }
                    if (columnIslands[index] == null && inset > 0.0D) {
                        int top = candidate.topFor(x, z, inset);
                        columnIslands[index] = candidate;
                        insets[index] = inset;
                        tops[index] = top;
                        bottoms[index] = candidate.bottomFor(x, z, inset, top);
                    }
                }
                craterOwners[index] = owner;
                craterRatios[index] = owner != null ? owner.craterRatioFor(ownerInset) : Double.NaN;
                SkyIsleGrid.Island island = columnIslands[index];
                if (craterDepth > 0 || island != null) {
                    int bottom = island != null ? bottoms[index] : SkyIsleGrid.NO_HEIGHT;
                    floors[index] = carveCrater(level, chunk, cursor, palette, seed, x, z, craterDepth, bottom, owner, minY);
                    placed |= floors[index] != SkyIsleGrid.NO_HEIGHT;
                }
            }
        }

        RandomState randomState = level.getLevel().getChunkSource().randomState();
        placed |= fillCraterLakes(level, context.chunkGenerator(), randomState, cursor, palette, seed, startX, startZ, floors, craterOwners, craterRatios);
        placed |= placeRubble(chunk, cursor, palette, startX, startZ, floors, craterOwners, craterRatios);
        placed |= placeCraterRuin(level, context, startX, startZ, craterOwners, craterRatios);

        for (int index = 0; index < 256; index++) {
            SkyIsleGrid.Island island = columnIslands[index];
            if (island != null && bottoms[index] < tops[index]) {
                int x = startX + (index >> 4);
                int z = startZ + (index & 15);
                buildIsland(level, chunk, cursor, palette, seed, x, z, island, insets[index], tops[index], bottoms[index]);
                placed = true;
            }
        }
        if (placed) {
            Heightmap.primeHeightmaps(chunk, WORLDGEN_HEIGHTMAPS);
        }
        return placed;
    }

    private static Palette palette() {
        Palette current = palette;
        if (current == null) {
            current = new Palette();
            palette = current;
        }
        return current;
    }

    private static int carveCrater(WorldGenLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos cursor, Palette palette, long seed, int x, int z,
                                   int depth, int islandBottom, SkyIsleGrid.Island owner, int minY) {
        int surfaceTop = Math.max(chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15), chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x & 15, z & 15));
        int solidTop = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x & 15, z & 15);
        boolean submerged = surfaceTop > solidTop && !chunk.getBlockState(cursor.set(x, solidTop + 1, z)).getFluidState().isEmpty();
        int floor = solidTop - depth;
        if (islandBottom != SkyIsleGrid.NO_HEIGHT) {
            floor = Math.min(floor, islandBottom - MIN_GAP);
        }
        floor = Math.max(floor, minY);
        if (floor >= solidTop) {
            return SkyIsleGrid.NO_HEIGHT;
        }
        if (submerged) {
            for (int y = solidTop; y > floor; y--) {
                chunk.setBlockState(cursor.set(x, y, z), palette.water);
            }
            return SkyIsleGrid.NO_HEIGHT;
        }
        for (int y = surfaceTop; y > floor; y--) {
            chunk.setBlockState(cursor.set(x, y, z), palette.air);
        }
        boolean royal = owner != null && owner.isRoyal();
        BlockState top = royal ? palette.grass : primordialCover(palette, seed, x, z, owner);
        BlockState soil = royal ? palette.dirt : palette.soil;
        chunk.setBlockState(cursor.set(x, floor, z), top);
        for (int y = floor - 1; y >= Math.max(minY, floor - SOIL_DEPTH); y--) {
            BlockState current = chunk.getBlockState(cursor.set(x, y, z));
            if (current.isAir() || !current.getFluidState().isEmpty()) {
                break;
            }
            chunk.setBlockState(cursor, soil);
        }
        placePlant(level, cursor, palette, seed, x, floor, z, top, owner != null ? owner.theme : SkyIsleGrid.Theme.GROVE);
        return floor;
    }

    private static BlockState primordialCover(Palette palette, long seed, int x, int z, SkyIsleGrid.Island owner) {
        long salt = owner != null ? owner.shapeSeed : seed;
        double cover = SkyIsleGrid.fbm(salt ^ COVER_SALT, x / 11.0D, z / 11.0D, 2);
        if (cover > 0.3D) {
            return palette.moss;
        }
        return cover < -0.38D ? palette.soil : palette.topSoil;
    }

    private static BlockState islandCover(Palette palette, SkyIsleGrid.Island island, int x, int z) {
        double cover = SkyIsleGrid.fbm(island.shapeSeed ^ COVER_SALT, x / 11.0D, z / 11.0D, 2);
        return switch (island.theme) {
            case ROYAL_GROVE -> palette.grass;
            case FALLEN_REACH -> cover > 0.42D ? palette.moss : cover < -0.45D ? palette.tuff : cover < -0.25D ? palette.coarseDirt : palette.topSoil;
            case SHARK_SHOALS -> cover > 0.35D ? palette.moss : cover > 0.05D ? palette.topSoil : null;
            default -> cover > 0.3D ? palette.moss : cover < -0.38D ? palette.soil : palette.topSoil;
        };
    }

    private static void placePlant(WorldGenLevel level, BlockPos.MutableBlockPos cursor, Palette palette, long seed, int x, int groundY, int z,
                                   BlockState ground, SkyIsleGrid.Theme theme) {
        double roll = SkyIsleGrid.hash(seed ^ PLANT_SALT, x, z);
        cursor.set(x, groundY + 1, z);
        if (ground == palette.grass) {
            if (roll < 0.4D) {
                level.setBlock(cursor, palette.shortGrass, 2);
            } else if (roll < 0.46D) {
                if (level.getBlockState(cursor.above()).isAir()) {
                    DoublePlantBlock.placeAt(level, palette.tallGrass, cursor, 2);
                }
            } else if (roll < 0.5D) {
                level.setBlock(cursor, palette.dandelion, 2);
            } else if (roll < 0.515D) {
                level.setBlock(cursor, palette.oxeyeDaisy, 2);
            } else if (roll < 0.525D) {
                level.setBlock(cursor, palette.cornflower, 2);
            }
            return;
        }
        if (ground != palette.topSoil && ground != palette.moss) {
            return;
        }
        double chance = switch (theme) {
            case FALLEN_REACH -> 0.35D;
            case SHARK_SHOALS -> 0.4D;
            default -> 0.55D;
        };
        if (roll < chance) {
            level.setBlock(cursor, palette.primordialGrass, 2);
        }
    }

    private static boolean fillCraterLakes(WorldGenLevel level, ChunkGenerator generator, RandomState randomState, BlockPos.MutableBlockPos cursor,
                                           Palette palette, long seed, int startX, int startZ, int[] floors, SkyIsleGrid.Island[] owners, double[] craterRatios) {
        boolean placed = false;
        SkyIsleGrid.Island lakeIsland = null;
        int lakeY = SkyIsleGrid.NO_HEIGHT;
        for (int index = 0; index < 256; index++) {
            SkyIsleGrid.Island owner = owners[index];
            int floor = floors[index];
            if (owner == null || !owner.isLagoon() || floor == SkyIsleGrid.NO_HEIGHT) {
                continue;
            }
            if (owner != lakeIsland) {
                lakeIsland = owner;
                lakeY = lakeLevel(level, generator, randomState, owner);
            }
            double craterRatio = craterRatios[index];
            if (Double.isNaN(craterRatio) || craterRatio > 0.8D) {
                continue;
            }
            int x = startX + (index >> 4);
            int z = startZ + (index & 15);
            if (floor < lakeY) {
                level.setBlock(cursor.set(x, floor, z), palette.tidestone, 2);
                for (int y = floor + 1; y <= lakeY; y++) {
                    level.setBlock(cursor.set(x, y, z), palette.water, 2);
                }
                level.setBlock(cursor.set(x, lakeY + 1, z),
                        SkyIsleGrid.hash(seed ^ 0x6C8E9CF5L, x, z) < LILY_CHANCE ? palette.lilyPad : palette.air, 2);
                placed = true;
            } else if (floor <= lakeY + 2) {
                level.setBlock(cursor.set(x, floor, z), palette.of(ElythiaStrata.tideLayer(seed, x, floor, z)), 2);
                placed = true;
            }
        }
        return placed;
    }

    private static int lakeLevel(WorldGenLevel level, ChunkGenerator generator, RandomState randomState, SkyIsleGrid.Island island) {
        int cached = island.cachedLakeLevel();
        if (cached != SkyIsleGrid.NO_HEIGHT) {
            return cached;
        }
        int centerX = (int) Math.floor(island.centerX);
        int centerZ = (int) Math.floor(island.centerZ);
        int ground = generator.getBaseHeight(centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, level, randomState) - 1;
        int lake = Math.min(ground - Math.round(island.craterDepth() * 0.35F), island.bottomAt(centerX, centerZ) - LAKE_ISLAND_GAP);
        island.cacheLakeLevel(lake);
        return lake;
    }

    private static boolean placeRubble(ChunkAccess chunk, BlockPos.MutableBlockPos cursor, Palette palette, int startX, int startZ, int[] floors,
                                       SkyIsleGrid.Island[] owners, double[] craterRatios) {
        boolean placed = false;
        for (int index = 0; index < 256; index++) {
            SkyIsleGrid.Island owner = owners[index];
            int floor = floors[index];
            if (owner == null || floor == SkyIsleGrid.NO_HEIGHT) {
                continue;
            }
            if (owner.theme != SkyIsleGrid.Theme.ROYAL_GROVE && owner.theme != SkyIsleGrid.Theme.FALLEN_REACH) {
                continue;
            }
            int x = startX + (index >> 4);
            int z = startZ + (index & 15);
            double field = Math.sin(x * 0.19D + owner.shapeSeed % 7) * Math.sin(z * 0.17D + owner.shapeSeed % 11)
                    + 0.35D * Math.sin(x * 0.53D + z * 0.41D);
            double threshold = owner.theme == SkyIsleGrid.Theme.FALLEN_REACH ? 0.55D : 0.8D;
            if (field <= threshold) {
                continue;
            }
            double craterRatio = craterRatios[index];
            if (Double.isNaN(craterRatio) || craterRatio > 0.85D) {
                continue;
            }
            int height = 1 + (int) Math.round((field - threshold) * 4.0D);
            for (int step = 1; step <= height; step++) {
                double pick = SkyIsleGrid.hash(owner.shapeSeed ^ step, x, z);
                BlockState rubble = step == height && pick < 0.6D ? palette.moss : pick < 0.5D ? palette.rockite : pick < 0.8D ? palette.zebraStone : palette.conglomerate;
                chunk.setBlockState(cursor.set(x, floor + step, z), rubble);
            }
            placed = true;
        }
        return placed;
    }

    private static boolean placeCraterRuin(WorldGenLevel level, FeaturePlaceContext<NoneFeatureConfiguration> context, int startX, int startZ,
                                           SkyIsleGrid.Island[] owners, double[] craterRatios) {
        int center = 8 << 4 | 8;
        SkyIsleGrid.Island owner = owners[center];
        if (owner == null || owner.theme != SkyIsleGrid.Theme.ROYAL_GROVE) {
            return false;
        }
        if (SkyIsleGrid.hash(owner.shapeSeed ^ 0x3C6EF372L, startX >> 4, startZ >> 4) >= RUIN_CHANCE) {
            return false;
        }
        double craterRatio = craterRatios[center];
        if (Double.isNaN(craterRatio) || craterRatio > 0.7D) {
            return false;
        }
        int x = startX + 8;
        int z = startZ + 8;
        int y = level.getChunk(x >> 4, z >> 4).getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) + 1;
        RandomSource random = context.random();
        return level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(ZEBRA_RUIN)
                .map(holder -> holder.value().place(level, context.chunkGenerator(), random, new BlockPos(x, y, z)))
                .orElse(false);
    }

    private static void buildIsland(WorldGenLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos cursor, Palette palette, long seed, int x, int z,
                                    SkyIsleGrid.Island island, double edge, int top, int bottom) {
        ElythiaStrataProfile profile = ElythiaStrataProfile.SKY_ISLES;
        boolean water = island.holdsWater(x, z, top);
        int waterLevel = island.waterLevel();
        int soilDepth = top - bottom > SOIL_DEPTH + 3 ? SOIL_DEPTH : 0;
        int capDepth = ElythiaStrata.capDepth(seed, profile, x, z);
        boolean veins = ElythiaStrata.veinRegion(seed, profile, x, z, false);
        double bedOffset = ElythiaStrata.bedOffset(seed, profile, x, z) + island.tiltAt(x, z);
        double ratio = island.centerRatio(x, z);
        boolean tidal = island.isLagoon() && !water && top <= waterLevel + 1 && ratio < 0.75D;
        BlockState cover = water ? palette.tidestone : tidal ? palette.of(ElythiaStrata.tideLayer(seed, x, top, z)) : islandCover(palette, island, x, z);
        boolean royal = island.isRoyal();
        BlockState soil = royal ? palette.dirt : palette.soil;
        int spill = !royal && edge < SPILL_EDGE && cover == palette.moss ? 1 + (int) (SkyIsleGrid.hash(island.shapeSeed ^ 0x2D7F1A93L, x, z) * 3.0D) : 0;
        double tuffPhase = (island.shapeSeed & 0xFFFFL) * 0.0137D;
        for (int y = top; y >= bottom; y--) {
            int depth = top - y;
            BlockState state;
            if (depth == 0 && cover != null) {
                state = cover;
            } else if (depth <= spill && depth < top - bottom) {
                state = palette.moss;
            } else if (depth <= soilDepth && cover != null) {
                state = soil;
            } else {
                int rockDepth = depth - soilDepth - 1;
                ElythiaStrata.Layer layer = veins && ElythiaStrata.inVein(seed, x, y, z)
                        ? ElythiaStrata.Layer.ARKROSE : ElythiaStrata.capLayer(rockDepth, capDepth);
                if (layer == ElythiaStrata.Layer.BASE) {
                    layer = ElythiaStrata.layerAt(seed, profile, y, bedOffset);
                }
                state = layer == ElythiaStrata.Layer.BASE && tuffPocket(island, tuffPhase, x, y, z) ? palette.tuff : palette.of(layer);
            }
            chunk.setBlockState(cursor.set(x, y, z), state);
        }
        if (water) {
            for (int y = top + 1; y <= waterLevel; y++) {
                level.setBlock(cursor.set(x, y, z), palette.water, 2);
            }
            if (edge < 2.0D) {
                level.scheduleTick(cursor.set(x, waterLevel, z), Fluids.WATER, 0);
            } else if (SkyIsleGrid.hash(island.shapeSeed ^ 0x6C8E9CF5L, x, z) < LILY_CHANCE) {
                level.setBlock(cursor.set(x, waterLevel + 1, z), palette.lilyPad, 2);
            }
        } else if (cover != null && edge >= 1.0D) {
            placePlant(level, cursor, palette, seed, x, top, z, cover, island.theme);
        }
    }

    private static boolean tuffPocket(SkyIsleGrid.Island island, double phase, int x, int y, int z) {
        return SkyIsleGrid.valueNoise(island.shapeSeed ^ 0x88L, x / 9.0D + phase, (y * 1.6D + z) / 9.0D) > 0.6D;
    }

    private static BlockState block(String path) {
        return BuiltInRegistries.BLOCK.getOptional(Identifier.fromNamespaceAndPath(Antarchy.MODID, path))
                .orElseThrow(() -> new IllegalStateException("Missing sky isle block: " + path)).defaultBlockState();
    }

    private static final class Palette {
        private final BlockState topSoil = block("primordial_loam");
        private final BlockState soil = block("loam");
        private final BlockState moss = block("primordial_moss_block");
        private final BlockState primordialGrass = block("primordial_grass");
        private final BlockState rockite = block("rockite");
        private final BlockState zebraStone = block("zebra_stone");
        private final BlockState conglomerate = block("conglomerate");
        private final BlockState arkrose = block("arkrose");
        private final BlockState tidestone = block("tidestone");
        private final BlockState tuff = Blocks.TUFF.defaultBlockState();
        private final BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        private final BlockState dirt = Blocks.DIRT.defaultBlockState();
        private final BlockState coarseDirt = Blocks.COARSE_DIRT.defaultBlockState();
        private final BlockState shortGrass = Blocks.SHORT_GRASS.defaultBlockState();
        private final BlockState tallGrass = Blocks.TALL_GRASS.defaultBlockState();
        private final BlockState dandelion = Blocks.DANDELION.defaultBlockState();
        private final BlockState oxeyeDaisy = Blocks.OXEYE_DAISY.defaultBlockState();
        private final BlockState cornflower = Blocks.CORNFLOWER.defaultBlockState();
        private final BlockState lilyPad = Blocks.LILY_PAD.defaultBlockState();
        private final BlockState water = Blocks.WATER.defaultBlockState();
        private final BlockState air = Blocks.AIR.defaultBlockState();

        private BlockState of(ElythiaStrata.Layer layer) {
            return switch (layer) {
                case BASE -> this.rockite;
                case ZEBRA -> this.zebraStone;
                case CONGLOMERATE -> this.conglomerate;
                case ARKROSE -> this.arkrose;
                case TIDESTONE -> this.tidestone;
            };
        }
    }
}
