package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import com.craisinlord.antarchy.content.worldgen.ocean.StarCoralPatchFeature;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ElythiaReefFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SCATTER_MIN = 2;
    private static final int SCATTER_RANGE = 13;
    private static final int DENSITY_SCALE = 4;
    private static final double CORAL_CLEARANCE = 6.0D;
    private static final double STAR_CLEARANCE = 10.0D;
    private static final StarCoralPatchFeature STAR_CORAL_PATCH = new StarCoralPatchFeature(NoneFeatureConfiguration.CODEC);

    public ElythiaReefFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = ChunkPos.containing(context.origin());
        int cellX = Math.floorDiv(chunk.getMinBlockX(), ReefPlanner.CELL);
        int cellZ = Math.floorDiv(chunk.getMinBlockZ(), ReefPlanner.CELL);
        List<ReefPiece> pieces = ReefPlanner.pieces(level, context.chunkGenerator(), cellX, cellZ);

        ReefCanvas canvas = null;
        try {
            for (ReefPiece piece : pieces) {
                if (!overlaps(chunk, piece.centerX(), piece.centerZ(), piece.radius())) {
                    continue;
                }
                if (canvas == null) {
                    canvas = new ReefCanvas(level, context.chunkGenerator(), chunk);
                }
                piece.render(canvas);
            }
            if (canvas != null) {
                canvas.finish(context.random());
            }
        } finally {
            if (canvas != null) {
                canvas.release();
            }
        }
        scatter(context, chunk, pieces);
        return true;
    }

    private static boolean overlaps(ChunkPos chunk, int x, int z, int radius) {
        return x + radius >= chunk.getMinBlockX() && x - radius <= chunk.getMaxBlockX() + 1
                && z + radius >= chunk.getMinBlockZ() && z - radius <= chunk.getMaxBlockZ() + 1;
    }

    private static void scatter(FeaturePlaceContext<NoneFeatureConfiguration> context, ChunkPos chunk, List<ReefPiece> pieces) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int sea = context.chunkGenerator().getSeaLevel();
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
            Feature<NoneFeatureConfiguration> feature;
            if (star) {
                feature = STAR_CORAL_PATCH;
            } else if (roll < 0.3F) {
                feature = Feature.CORAL_TREE;
            } else if (roll < 0.58F) {
                feature = Feature.CORAL_CLAW;
            } else {
                feature = Feature.CORAL_MUSHROOM;
            }
            feature.place(NoneFeatureConfiguration.INSTANCE, level, context.chunkGenerator(), random, pos);
        }
    }

    private static double density(long seed, int chunkX, int chunkZ) {
        int cx = Math.floorDiv(chunkX, DENSITY_SCALE);
        int cz = Math.floorDiv(chunkZ, DENSITY_SCALE);
        double tx = (Math.floorMod(chunkX, DENSITY_SCALE) + 0.5D) / DENSITY_SCALE;
        double tz = (Math.floorMod(chunkZ, DENSITY_SCALE) + 0.5D) / DENSITY_SCALE;
        long salt = seed ^ 0xC0A1F1E1DL;
        double value = Mth.lerp2(Mth.smoothstep(tx), Mth.smoothstep(tz),
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
}
