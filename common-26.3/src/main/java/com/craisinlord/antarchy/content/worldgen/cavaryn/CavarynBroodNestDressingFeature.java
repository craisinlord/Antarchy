package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.SpitBugEggBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynBroodNestDressingFeature implements Feature {
    public static final ResourceKey<NormalNoise> NEST_NOISE = ResourceKey.create(
            Registries.NOISE,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_brood_nest")
    );
    public static final ResourceKey<NormalNoise> GALLERY_NOISE = ResourceKey.create(
            Registries.NOISE,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_brood_gallery")
    );
    private static final Identifier BROODSTONE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "broodstone");
    private static final Identifier SPIT_BUG_EGG_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "spit_bug_egg");
    private static final Set<Identifier> LINEABLE = Set.of(
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "myrmite"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "biomite"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "biomite_turf"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "loam"),
            Identifier.withDefaultNamespace("mud"),
            Identifier.withDefaultNamespace("dirt"),
            Identifier.withDefaultNamespace("coarse_dirt"),
            Identifier.withDefaultNamespace("rooted_dirt"),
            Identifier.withDefaultNamespace("podzol")
    );
    private static final CavarynBlockSet LINEABLE_BLOCKS = new CavarynBlockSet(LINEABLE);
    private static final double LINING_DEPTH = 1.5;
    private static final int MIN_EGGS = 3;
    private static final int MAX_EGGS = 6;

    private final ResourceKey<NormalNoise> noiseKey;

    public static final MapCodec<CavarynBroodNestDressingFeature> CODEC = MapCodec.unit(() -> new CavarynBroodNestDressingFeature(NEST_NOISE));
    public static final MapCodec<CavarynBroodNestDressingFeature> GALLERY_CODEC = MapCodec.unit(() -> new CavarynBroodNestDressingFeature(GALLERY_NOISE));

    public CavarynBroodNestDressingFeature(ResourceKey<NormalNoise> noiseKey) {
        this.noiseKey = noiseKey;
    }

    @Override
    public MapCodec<CavarynBroodNestDressingFeature> codec() {
        return this.noiseKey.equals(GALLERY_NOISE) ? GALLERY_CODEC : CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        net.minecraft.world.level.levelgen.synth.Noise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(this.noiseKey);
        CavarynCavityGrid grid = CavarynCavityGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.get(1234.567, 89.123, -456.789),
                noise.get(-987.654, 12.345, 654.321)
        ));
        Block broodstone = block(BROODSTONE_ID);
        Block egg = block(SPIT_BUG_EGG_ID);
        if (grid == null || broodstone == null) {
            return false;
        }

        int originX = origin.getX() & ~15;
        int originZ = origin.getZ() & ~15;
        int minBuild = level.getMinY() + 6;
        int maxBuild = (level.getMaxY() + 1) - 7;
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
        int bottom = Math.max(level.getMinY() + 1, Mth.floor(chamber.y() - chamber.depth()) - 3);
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

    private static Block block(Identifier id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
