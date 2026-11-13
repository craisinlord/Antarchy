package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class SkyIsleFeature extends Feature<NoneFeatureConfiguration> {
    private static final double ROYAL_CASTLE_CLEARANCE = 30.0D;
    private static final double FALLEN_CASTLE_RATIO = 0.85D;
    private static final ResourceKey<ConfiguredFeature<?, ?>> ROYAL_TREE_SMALL = key("royal_tree_small");
    private static final ResourceKey<ConfiguredFeature<?, ?>> ROYAL_TREE_MEDIUM = key("royal_tree_medium");
    private static final ResourceKey<ConfiguredFeature<?, ?>> ROYAL_TREE_LARGE = key("royal_tree_large");
    private static final ResourceKey<ConfiguredFeature<?, ?>> ZEBRA_RUIN = key("zebra_stone_ruin");
    private static final ResourceKey<ConfiguredFeature<?, ?>> TIDESTONE_RUIN = key("tidestone_ruin");
    private static final ResourceKey<ConfiguredFeature<?, ?>> ROCKITE_RUIN = key("rockite_ruin");
    private static final ResourceKey<ConfiguredFeature<?, ?>> ARKROSE_RUIN = key("arkrose_ruin");
    private static final int RUIN_TRIES = 6;

    public SkyIsleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int startX = context.origin().getX() & ~15;
        int startZ = context.origin().getZ() & ~15;
        List<SkyIsleGrid.Island> islands = SkyIsleGrid.forSeed(level.getSeed()).islandsTouching(startX, startZ, startX + 15, startZ + 15);
        if (islands.isEmpty()) {
            return false;
        }
        boolean placed = false;
        for (SkyIsleGrid.Island island : islands) {
            placed |= this.placeIslandRuins(context, island, startX, startZ);
        }

        int attempts = 6;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = startX + 2 + random.nextInt(12);
            int z = startZ + 2 + random.nextInt(12);
            SkyIsleGrid.Island island = null;
            for (SkyIsleGrid.Island candidate : islands) {
                if (candidate.topAt(x, z) != SkyIsleGrid.NO_HEIGHT) {
                    island = candidate;
                    break;
                }
            }
            if (island == null || island.holdsWater(x, z)) {
                continue;
            }
            double edge = island.edgeDistance(x, z);
            double ratio = island.centerRatio(x, z);
            if (edge < 3.0D || this.inCastleZone(island, x, z, ratio)) {
                continue;
            }
            BlockPos origin = new BlockPos(x, island.topAt(x, z) + 1, z);
            ResourceKey<ConfiguredFeature<?, ?>> pick = this.pick(island, random, ratio);
            if (pick != null) {
                placed |= place(context, pick, origin);
            }
        }
        return placed;
    }

    private ResourceKey<ConfiguredFeature<?, ?>> pick(SkyIsleGrid.Island island, RandomSource random, double ratio) {
        float roll = random.nextFloat();
        return switch (island.theme) {
            case ROYAL_GROVE -> roll < 0.7F ? tree(random) : roll < 0.76F ? ZEBRA_RUIN : null;
            case GROVE -> roll < 0.45F ? tree(random) : roll < 0.65F ? ruin(island, random) : null;
            case FALLEN_REACH -> roll < 0.45F ? ruin(island, random) : null;
            case LAGOON -> roll < 0.15F ? tree(random) : roll < 0.35F ? ruin(island, random) : null;
            case SHARK_SHOALS -> roll < 0.3F ? ruin(island, random) : null;
        };
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> ruin(SkyIsleGrid.Island island, RandomSource random) {
        boolean first = random.nextBoolean();
        return switch (island.theme) {
            case LAGOON -> first ? TIDESTONE_RUIN : ARKROSE_RUIN;
            case SHARK_SHOALS -> first ? ROCKITE_RUIN : ARKROSE_RUIN;
            default -> first ? ZEBRA_RUIN : ROCKITE_RUIN;
        };
    }

    private boolean placeIslandRuins(FeaturePlaceContext<NoneFeatureConfiguration> context, SkyIsleGrid.Island island, int startX, int startZ) {
        if (island.crown) {
            return false;
        }
        int centerX = Mth.floor(island.centerX);
        int centerZ = Mth.floor(island.centerZ);
        if (centerX >> 4 != startX >> 4 || centerZ >> 4 != startZ >> 4) {
            return false;
        }
        RandomSource random = context.random();
        int ruins = 1 + (island.radius >= 25.0D ? 1 : 0) + (island.radius >= 35.0D ? 1 : 0);
        double spread = Math.max(3.0D, island.radius * 0.4D);
        boolean placed = false;
        for (int ruin = 0; ruin < ruins; ruin++) {
            for (int attempt = 0; attempt < RUIN_TRIES; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = random.nextDouble() * spread;
                int x = Mth.floor(island.centerX + Math.cos(angle) * distance);
                int z = Mth.floor(island.centerZ + Math.sin(angle) * distance);
                double inset = island.insetAt(x, z);
                if (inset < 4.0D) {
                    continue;
                }
                int top = island.topFor(x, z, inset);
                if (island.holdsWater(x, z, top)) {
                    continue;
                }
                if (place(context, ruin(island, random), new BlockPos(x, top + 1, z))) {
                    placed = true;
                    break;
                }
            }
        }
        return placed;
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> tree(RandomSource random) {
        float roll = random.nextFloat();
        return roll < 0.5F ? ROYAL_TREE_SMALL : roll < 0.85F ? ROYAL_TREE_MEDIUM : ROYAL_TREE_LARGE;
    }

    private boolean inCastleZone(SkyIsleGrid.Island island, int x, int z, double ratio) {
        if (!island.crown) {
            return false;
        }
        if (island.theme == SkyIsleGrid.Theme.FALLEN_REACH) {
            return ratio < FALLEN_CASTLE_RATIO;
        }
        double dx = x + 0.5D - island.centerX;
        double dz = z + 0.5D - island.centerZ;
        return dx * dx + dz * dz < ROYAL_CASTLE_CLEARANCE * ROYAL_CASTLE_CLEARANCE;
    }

    private static boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context, ResourceKey<ConfiguredFeature<?, ?>> key, BlockPos origin) {
        WorldGenLevel level = context.level();
        return level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(key)
                .map(holder -> holder.value().place(level, context.chunkGenerator(), context.random(), origin))
                .orElse(false);
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
