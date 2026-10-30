package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelResource;

public final class ElythiaTerrainMap {
    private static final byte DRY = 0;
    private static final byte WATER = 1;
    private static final byte LUMEN = 2;
    private static final int LOW_HEIGHT = 100;
    private static final int HIGH_HEIGHT = 340;
    private static final int DEEP_FLUID = 48;
    private static final Map<String, Integer> BIOME_COLORS = Map.ofEntries(
            Map.entry("antarchy:elythia_ocean", 0x2E5FA8),
            Map.entry("antarchy:elythia_coral_spikes", 0xE07AA0),
            Map.entry("antarchy:elythia_beach", 0xEADBA0),
            Map.entry("antarchy:elythia_river", 0x4A90E0),
            Map.entry("antarchy:primordial_plains", 0x8CCB5E),
            Map.entry("antarchy:primordial_massif", 0xA8A096),
            Map.entry("antarchy:shelfback_highlands", 0x6FA85A),
            Map.entry("antarchy:glimmering_falls", 0x7FE0C8),
            Map.entry("antarchy:truffalo_forest", 0x7A5A3A),
            Map.entry("antarchy:truffalo_spires", 0xB08A60),
            Map.entry("antarchy:butterfly_fields", 0xF0D060),
            Map.entry("antarchy:ouranwood_forest", 0x2F7A3A),
            Map.entry("antarchy:sparse_ouranwood_forest", 0x5A9A50),
            Map.entry("antarchy:fungal_ouranwood_forest", 0x9A4FB0),
            Map.entry("antarchy:fungal_reserve", 0xC060E0),
            Map.entry("antarchy:glimmering_pools", 0x50D0D0),
            Map.entry("antarchy:peach_forest", 0xF3A07A)
    );

    private ElythiaTerrainMap() {
    }

    public record Result(
            Path heightImage,
            Path biomeImage,
            int columns,
            long elapsedMillis,
            int minHeight,
            int maxHeight,
            double averageHeight,
            double fluidShare,
            double lumenShare,
            List<Map.Entry<String, Integer>> biomeCounts
    ) {
    }

    public static Result render(ServerLevel level, BlockPos center, int radius, int step, boolean exact) throws IOException {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        BiomeSource biomeSource = generator.getBiomeSource();
        Climate.Sampler sampler = randomState.sampler();
        DensityFunction surfaceDensity = randomState.router().preliminarySurfaceLevel();
        int seaLevel = generator.getSeaLevel();
        ElythiaHydrology hydrology = randomState.router().veinToggle() instanceof ElythiaHydrology found ? found : null;
        int size = radius * 2 / step + 1;
        int columns = size * size;
        int[] heights = new int[columns];
        int[] floors = new int[columns];
        byte[] fluids = new byte[columns];
        String[] biomes = new String[columns];
        int top = (level.getMaxY() + 1) - 1;
        int bottom = level.getMinY();

        long start = System.nanoTime();
        ForkJoinPool pool = new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        try {
            pool.submit(() -> IntStream.range(0, size).parallel().forEach(row -> {
                int z = center.getZ() - radius + row * step;
                for (int column = 0; column < size; column++) {
                    int x = center.getX() - radius + column * step;
                    int index = row * size + column;
                    int y;
                    int floor;
                    if (exact) {
                        NoiseColumn noise = generator.getBaseColumn(x, z, level, randomState);
                        y = top;
                        while (y > bottom && noise.getBlock(y).isAir()) {
                            y--;
                        }
                        FluidState fluid = noise.getBlock(y).getFluidState();
                        floor = y;
                        if (!fluid.isEmpty()) {
                            fluids[index] = AntarchyFluidChecks.isLumen(fluid) ? LUMEN : WATER;
                            while (floor > bottom && !noise.getBlock(floor).getFluidState().isEmpty()) {
                                floor--;
                            }
                        }
                    } else {
                        floor = solveSurface(surfaceDensity, x, z, bottom, top);
                        y = floor;
                        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(x, floor, z);
                        double reserveValue = hydrology == null ? 0.0D : hydrology.reserve().compute(point);
                        boolean reserve = reserveValue >= 0.5D;
                        double riverLevel = hydrology == null ? Double.NEGATIVE_INFINITY : ElythiaAquifer.level(hydrology.riverLevel().compute(point));
                        if (reserveValue < 0.04D && floor < riverLevel - 1 && hydrology.valley().compute(point) > 0.0D && hydrology.riverChannel().compute(point) > 0.0D) {
                            fluids[index] = hydrology.lumen().compute(point) > 0.75D ? LUMEN : WATER;
                            y = (int) riverLevel - 1;
                        } else if (!reserve && floor < seaLevel - 1 && (reserveValue <= 0.0D || floor < seaLevel - 64)) {
                            fluids[index] = WATER;
                            y = seaLevel - 1;
                        }
                    }
                    heights[index] = y;
                    floors[index] = floor;
                    biomes[index] = biomeSource.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(floor), QuartPos.fromBlock(z), sampler)
                            .unwrapKey()
                            .map(key -> key.identifier().toString())
                            .orElse("unknown");
                }
            })).join();
        } finally {
            pool.shutdown();
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;

        BufferedImage heightImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        BufferedImage biomeImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Map<String, Integer> biomeCounts = new HashMap<>();
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        long heightTotal = 0L;
        int landColumns = 0;
        int fluidColumns = 0;
        int lumenColumns = 0;
        for (int index = 0; index < columns; index++) {
            int x = index % size;
            int z = index / size;
            float shade = Mth.clamp((floors[index] - LOW_HEIGHT) / (float) (HIGH_HEIGHT - LOW_HEIGHT), 0.0F, 1.0F);
            biomeCounts.merge(biomes[index], 1, Integer::sum);
            if (fluids[index] == DRY) {
                landColumns++;
                heightTotal += heights[index];
                minHeight = Math.min(minHeight, heights[index]);
                maxHeight = Math.max(maxHeight, heights[index]);
                int gray = (int) (40 + shade * 195);
                heightImage.setRGB(x, z, gray << 16 | gray << 8 | gray);
            } else {
                fluidColumns++;
                float depth = Mth.clamp((heights[index] - floors[index]) / (float) DEEP_FLUID, 0.0F, 1.0F);
                if (fluids[index] == LUMEN) {
                    lumenColumns++;
                    heightImage.setRGB(x, z, scale(0x60F0E0, 1.0F - depth * 0.6F));
                } else {
                    heightImage.setRGB(x, z, scale(0x5090E8, 1.0F - depth * 0.7F));
                }
            }
            int biomeColor = BIOME_COLORS.getOrDefault(biomes[index], biomes[index].hashCode() & 0xFFFFFF);
            float biomeShade = fluids[index] == DRY ? 0.55F + shade * 0.45F : 0.7F;
            biomeImage.setRGB(x, z, scale(biomeColor, biomeShade));
        }

        Path directory = level.getServer().getWorldPath(LevelResource.ROOT).resolve("antarchy_maps");
        Files.createDirectories(directory);
        String name = level.dimension().identifier().getPath() + "_" + center.getX() + "_" + center.getZ()
                + "_r" + radius + "_s" + step + "_" + System.currentTimeMillis() / 1000L;
        Path heightPath = directory.resolve(name + "_height.png");
        Path biomePath = directory.resolve(name + "_biome.png");
        ImageIO.write(heightImage, "png", heightPath.toFile());
        ImageIO.write(biomeImage, "png", biomePath.toFile());

        List<Map.Entry<String, Integer>> sortedBiomes = new ArrayList<>(biomeCounts.entrySet());
        sortedBiomes.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        return new Result(
                heightPath.toAbsolutePath(),
                biomePath.toAbsolutePath(),
                columns,
                elapsedMillis,
                landColumns == 0 ? 0 : minHeight,
                landColumns == 0 ? 0 : maxHeight,
                landColumns == 0 ? 0.0D : heightTotal / (double) landColumns,
                fluidColumns / (double) columns,
                lumenColumns / (double) columns,
                sortedBiomes
        );
    }

    private static int solveSurface(DensityFunction density, int x, int z, int bottom, int top) {
        return Mth.clamp(Mth.floor(density.compute(new DensityFunction.SinglePointContext(x, 0, z))), bottom, top);
    }

    private static int scale(int color, float factor) {
        int red = Mth.clamp((int) ((color >> 16 & 0xFF) * factor), 0, 255);
        int green = Mth.clamp((int) ((color >> 8 & 0xFF) * factor), 0, 255);
        int blue = Mth.clamp((int) ((color & 0xFF) * factor), 0, 255);
        return red << 16 | green << 8 | blue;
    }
}
