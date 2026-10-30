package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynMolewormWarrenDressingFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<NormalNoise.NoiseParameters> WARREN_NOISE = ResourceKey.create(
            Registries.NOISE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_moleworm_warren")
    );
    private static final Set<ResourceLocation> LINEABLE = Set.of(
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "myrmite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "broodstone"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "biomite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "loam")
    );
    private static final CavarynBlockSet LINEABLE_BLOCKS = new CavarynBlockSet(LINEABLE);
    private static final double LINING_DEPTH = 1.4;
    private static final float COARSE_CHANCE = 0.15F;
    private static final float PODZOL_CHANCE = 0.12F;
    private static final double MOUND_MIN_RADIUS = 2.5;
    private static final double MOUND_MAX_RADIUS = 4.5;
    private static final double MOUND_MIN_HEIGHT = 3.0;
    private static final double MOUND_MAX_HEIGHT = 5.0;

    public CavarynMolewormWarrenDressingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        NormalNoise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(WARREN_NOISE);
        CavarynCavityGrid grid = CavarynCavityGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.getValue(1234.567, 89.123, -456.789),
                noise.getValue(-987.654, 12.345, 654.321)
        ));
        if (grid == null) {
            return false;
        }

        RandomSource random = context.random();
        int originX = context.origin().getX() & ~15;
        int originZ = context.origin().getZ() & ~15;
        int minBuild = level.getMinBuildHeight() + 6;
        int maxBuild = level.getMaxBuildHeight() - 8;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = originX + dx;
                int z = originZ + dz;
                int[] range = grid.verticalRange(x, z);
                if (range == null) {
                    continue;
                }
                int minY = Math.max(minBuild, range[0]);
                int maxY = Math.min(maxBuild, range[1]);
                double next = grid.signedDistance(x, minY, z);
                for (int y = minY; y <= maxY; y++) {
                    double distance = next;
                    next = grid.signedDistance(x, y + 1, z);
                    if (distance <= 0.0 || distance > LINING_DEPTH) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (!isLineable(level.getBlockState(pos))) {
                        continue;
                    }
                    above.set(x, y + 1, z);
                    boolean floor = next <= 0.0 && level.getBlockState(above).isAir();
                    level.setBlock(pos, lining(random, floor), 2);
                    placed = true;
                }
            }
        }

        for (CavarynCavityGrid.Chamber chamber : grid.chambersCenteredIn(originX, originZ, originX + 15, originZ + 15)) {
            int mounds = 2 + random.nextInt(2);
            for (int i = 0; i < mounds; i++) {
                if (placeMound(level, random, grid, chamber)) {
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean placeMound(WorldGenLevel level, RandomSource random, CavarynCavityGrid grid, CavarynCavityGrid.Chamber chamber) {
        int spread = Math.max(1, Mth.floor(chamber.radius() * 0.5));
        int cx = chamber.x() + random.nextInt(spread * 2 + 1) - spread;
        int cz = chamber.z() + random.nextInt(spread * 2 + 1) - spread;
        int floorY = findFloor(level, cx, chamber.y(), Mth.floor(chamber.y() - chamber.depth()) - 4, cz);
        if (floorY == Integer.MIN_VALUE) {
            return false;
        }
        double radius = MOUND_MIN_RADIUS + random.nextDouble() * (MOUND_MAX_RADIUS - MOUND_MIN_RADIUS);
        double height = MOUND_MIN_HEIGHT + random.nextDouble() * (MOUND_MAX_HEIGHT - MOUND_MIN_HEIGHT);
        int reach = Mth.ceil(radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double falloff = 1.0 - (dx * dx + dz * dz) / (radius * radius);
                if (falloff <= 0.0) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                int base = findFloor(level, x, floorY + 3, floorY - 3, z);
                if (base == Integer.MIN_VALUE) {
                    continue;
                }
                int top = Mth.floor(height * falloff + random.nextDouble() * 0.6);
                for (int k = 0; k < top; k++) {
                    pos.set(x, base + k, z);
                    if (!level.getBlockState(pos).isAir() || grid.signedDistance(x, base + k, z) > -1.0) {
                        break;
                    }
                    level.setBlock(pos, random.nextFloat() < 0.6F ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.ROOTED_DIRT.defaultBlockState(), 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static int findFloor(WorldGenLevel level, int x, int top, int bottom, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top; y >= bottom; y--) {
            pos.set(x, y, z);
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            pos.set(x, y - 1, z);
            if (!level.getBlockState(pos).isAir()) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static BlockState lining(RandomSource random, boolean floor) {
        float roll = random.nextFloat();
        if (floor) {
            if (roll < PODZOL_CHANCE) {
                return Blocks.PODZOL.defaultBlockState();
            }
            return roll < PODZOL_CHANCE + COARSE_CHANCE ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.ROOTED_DIRT.defaultBlockState();
        }
        return roll < COARSE_CHANCE ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.ROOTED_DIRT.defaultBlockState();
    }

    private static boolean isLineable(BlockState state) {
        return LINEABLE_BLOCKS.contains(state);
    }
}
