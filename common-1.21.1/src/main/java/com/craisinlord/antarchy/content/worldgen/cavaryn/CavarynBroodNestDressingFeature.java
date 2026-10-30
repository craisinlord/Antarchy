package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.SpitBugEggBlock;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynBroodNestDressingFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<NormalNoise.NoiseParameters> NEST_NOISE = ResourceKey.create(
            Registries.NOISE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_brood_nest")
    );
    public static final ResourceKey<NormalNoise.NoiseParameters> GALLERY_NOISE = ResourceKey.create(
            Registries.NOISE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_brood_gallery")
    );
    private static final ResourceLocation BROODSTONE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "broodstone");
    private static final ResourceLocation SPIT_BUG_EGG_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "spit_bug_egg");
    private static final Set<ResourceLocation> LINEABLE = Set.of(
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "myrmite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "biomite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "biomite_turf"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "loam"),
            ResourceLocation.withDefaultNamespace("mud"),
            ResourceLocation.withDefaultNamespace("dirt"),
            ResourceLocation.withDefaultNamespace("coarse_dirt"),
            ResourceLocation.withDefaultNamespace("rooted_dirt"),
            ResourceLocation.withDefaultNamespace("podzol")
    );
    private static final CavarynBlockSet LINEABLE_BLOCKS = new CavarynBlockSet(LINEABLE);
    private static final double LINING_DEPTH = 1.5;
    private static final int MIN_EGGS = 3;
    private static final int MAX_EGGS = 6;

    private final ResourceKey<NormalNoise.NoiseParameters> noiseKey;

    public CavarynBroodNestDressingFeature(Codec<NoneFeatureConfiguration> codec) {
        this(codec, NEST_NOISE);
    }

    public CavarynBroodNestDressingFeature(Codec<NoneFeatureConfiguration> codec, ResourceKey<NormalNoise.NoiseParameters> noiseKey) {
        super(codec);
        this.noiseKey = noiseKey;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        NormalNoise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(this.noiseKey);
        CavarynCavityGrid grid = CavarynCavityGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.getValue(1234.567, 89.123, -456.789),
                noise.getValue(-987.654, 12.345, 654.321)
        ));
        Block broodstone = block(BROODSTONE_ID);
        Block egg = block(SPIT_BUG_EGG_ID);
        if (grid == null || broodstone == null) {
            return false;
        }

        RandomSource random = context.random();
        int originX = context.origin().getX() & ~15;
        int originZ = context.origin().getZ() & ~15;
        int minBuild = level.getMinBuildHeight() + 6;
        int maxBuild = level.getMaxBuildHeight() - 7;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
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
                for (int y = minY; y <= maxY; y++) {
                    double distance = grid.signedDistance(x, y, z);
                    if (distance <= 0.0 || distance > LINING_DEPTH) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (isLineable(level.getBlockState(pos))) {
                        level.setBlock(pos, broodstone.defaultBlockState(), 2);
                        placed = true;
                    }
                }
            }
        }

        if (egg != null) {
            for (CavarynCavityGrid.Chamber chamber : grid.chambersCenteredIn(originX, originZ, originX + 15, originZ + 15)) {
                if (this.placeClutch(level, random, chamber, egg)) {
                    placed = true;
                }
            }
        }
        return placed;
    }

    private boolean placeClutch(WorldGenLevel level, RandomSource random, CavarynCavityGrid.Chamber chamber, Block egg) {
        int eggs = MIN_EGGS + random.nextInt(MAX_EGGS - MIN_EGGS + 1);
        int spread = Math.max(1, Mth.floor(chamber.radius() * 0.55));
        int top = chamber.y();
        int bottom = Math.max(level.getMinBuildHeight() + 1, Mth.floor(chamber.y() - chamber.depth()) - 3);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int attempt = 0; attempt < eggs * 3 && eggs > 0; attempt++) {
            int x = chamber.x() + random.nextInt(spread * 2 + 1) - spread;
            int z = chamber.z() + random.nextInt(spread * 2 + 1) - spread;
            for (int y = top; y >= bottom; y--) {
                pos.set(x, y, z);
                if (!level.getBlockState(pos).isAir()) {
                    break;
                }
                BlockPos below = pos.below();
                if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    level.setBlock(pos, egg.defaultBlockState().setValue(SpitBugEggBlock.ROTATED, random.nextBoolean()), 2);
                    placed = true;
                    eggs--;
                    break;
                }
            }
        }
        return placed;
    }

    private static boolean isLineable(BlockState state) {
        return LINEABLE_BLOCKS.contains(state);
    }

    private static Block block(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
