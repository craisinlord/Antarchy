package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ElythiaReefFeature implements Feature {

    private static final int SCATTER_MIN = 2;
    private static final int SCATTER_RANGE = 13;
    private static final int DENSITY_SCALE = 4;
    private static final double CORAL_CLEARANCE = 6.0D;
    private static final double STAR_CLEARANCE = 10.0D;
    private static final StarCoralPatchFeature STAR_CORAL_PATCH = new StarCoralPatchFeature();

    public static final MapCodec<ElythiaReefFeature> CODEC = MapCodec.unit(ElythiaReefFeature::new);

    @Override
    public MapCodec<ElythiaReefFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        ChunkPos chunk = ChunkPos.containing(origin);
        int cellX = Math.floorDiv(chunk.getMinBlockX(), ReefPlanner.CELL);
        int cellZ = Math.floorDiv(chunk.getMinBlockZ(), ReefPlanner.CELL);
        List<ReefPiece> pieces = ReefPlanner.pieces(level, chunkGenerator, cellX, cellZ);

        ReefCanvas canvas = null;
        try {
            for (ReefPiece piece : pieces) {
                if (!overlaps(chunk, piece.centerX(), piece.centerZ(), piece.radius())) {
                    continue;
                }
                if (canvas == null) {
                    canvas = new ReefCanvas(level, chunkGenerator, chunk);
                }
                piece.render(canvas);
            }
            if (canvas != null) {
                canvas.finish(random);
            }
        } finally {
            if (canvas != null) {
                canvas.release();
            }
        }
        scatter(level, chunkGenerator, random, chunk, pieces);
        return true;
    }

    private static boolean overlaps(ChunkPos chunk, int x, int z, int radius) {
        return x + radius >= chunk.getMinBlockX() && x - radius <= chunk.getMaxBlockX() + 1
                && z + radius >= chunk.getMinBlockZ() && z - radius <= chunk.getMaxBlockZ() + 1;
    }

    private static void scatter(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, ChunkPos chunk, List<ReefPiece> pieces) {
        int sea = chunkGenerator.getSeaLevel();
        double density = density(level.getSeed(), chunk.x(), chunk.z());
        int attempts = SCATTER_MIN + Mth.floor(density * SCATTER_RANGE);
        for (int i = 0; i < attempts; i++) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            float roll = random.nextFloat();
            boolean star = roll >= 0.85F;
            if (blocked(pieces, x, z, star ? STAR_CLEARANCE : CORAL_CLEARANCE)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            if (y > sea - 3) {
                continue;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).is(Blocks.WATER) || !level.getBiome(pos).is(ReefPlanner.ELYTHIA_OCEAN)) {
                continue;
            }
            Feature feature;
            if (star) {
                feature = STAR_CORAL_PATCH;
            } else if (roll < 0.3F) {
                feature = coral(level, "tree");
            } else if (roll < 0.58F) {
                feature = coral(level, "claw");
            } else {
                placeMushroom(level, chunkGenerator, random, pos);
                continue;
            }
            feature.place(level, chunkGenerator, random, pos);
        }
    }

    private static double density(long seed, int chunkX, int chunkZ) {
        int cx = Math.floorDiv(chunkX, DENSITY_SCALE);
        int cz = Math.floorDiv(chunkZ, DENSITY_SCALE);
        double tx = (Math.floorMod(chunkX, DENSITY_SCALE) + 0.5D) / DENSITY_SCALE;
        double tz = (Math.floorMod(chunkZ, DENSITY_SCALE) + 0.5D) / DENSITY_SCALE;
        long salt = seed ^ 0xC0A1F1E1DL;
        double value = Mth.lerp2(smoothstep(tx), smoothstep(tz),
                ReefCanvas.noise(salt, cx, cz), ReefCanvas.noise(salt, cx + 1, cz),
                ReefCanvas.noise(salt, cx, cz + 1), ReefCanvas.noise(salt, cx + 1, cz + 1));
        return value * value * (3.0D - 2.0D * value);
    }

    private static boolean blocked(List<ReefPiece> pieces, int x, int z, double margin) {
        for (ReefPiece piece : pieces) {
            long dx = x - piece.centerX();
            long dz = z - piece.centerZ();
            double reach = piece.radius() + margin;
            if (dx * dx + dz * dz < reach * reach && piece.covers(x, z, margin)) {
                return true;
            }
        }
        return false;
    }
    private static Feature coral(WorldGenLevel level, String kind) {
        return level.registryAccess().lookupOrThrow(Registries.FEATURE)
                .getOrThrow(ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath("antarchy", "elythia_reef_coral_" + kind))).value();
    }

    private static double smoothstep(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static void placeMushroom(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        net.minecraft.world.level.block.state.BlockState coral = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getRandomElementOf(net.minecraft.tags.BlockTags.CORAL_BLOCKS, random)
                .map(holder -> holder.value().defaultBlockState()).orElse(null);
        if (coral == null) {
            return;
        }
        Feature decoration = level.registryAccess().lookupOrThrow(Registries.FEATURE)
                .getOrThrow(ResourceKey.create(Registries.FEATURE, Identifier.withDefaultNamespace("coral/block_decoration"))).value();
        int height = 3 + random.nextInt(3);
        int width = 3 + random.nextInt(3);
        int depth = 3 + random.nextInt(3);
        int drop = 1 + random.nextInt(3);
        BlockPos.MutableBlockPos cursor = origin.mutable();
        for (int x = 0; x <= width; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = 0; z <= depth; z++) {
                    boolean sideX = x == 0 || x == width;
                    boolean sideY = y == 0 || y == height;
                    boolean sideZ = z == 0 || z == depth;
                    if ((sideX && sideY) || (sideZ && sideY) || (sideX && sideZ)
                            || !(sideX || sideY || sideZ) || random.nextFloat() < 0.1F) {
                        continue;
                    }
                    cursor.setWithOffset(origin, x, y - drop, z);
                    if ((level.getBlockState(cursor).is(Blocks.WATER) || level.getBlockState(cursor).is(net.minecraft.tags.BlockTags.CORALS))
                            && level.getBlockState(cursor.above()).is(Blocks.WATER)) {
                        level.setBlock(cursor, coral, 3);
                        decoration.place(level, generator, random, cursor);
                    }
                }
            }
        }
    }

}
