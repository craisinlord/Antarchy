package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynCarapaceRiftDressingFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<NormalNoise.NoiseParameters> RIFT_NOISE = ResourceKey.create(
            Registries.NOISE,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_carapace_rift")
    );
    private static final Identifier CHITIN_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "chitin_block");
    private static final Set<Identifier> LINEABLE = Set.of(
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "myrmite"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "broodstone"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "biomite")
    );
    private static final CavarynBlockSet LINEABLE_BLOCKS = new CavarynBlockSet(LINEABLE);
    private static final double LINING_DEPTH = 1.5;
    private static final double BAND_SPACING = 9.0;
    private static final double BAND_THICKNESS = 2.5;

    public CavarynCarapaceRiftDressingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        NormalNoise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(RIFT_NOISE);
        CavarynCavityGrid grid = CavarynCavityGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.getValue(1234.567, 89.123, -456.789),
                noise.getValue(-987.654, 12.345, 654.321)
        ));
        Block chitin = block(CHITIN_ID);
        if (grid == null || chitin == null) {
            return false;
        }

        int originX = context.origin().getX() & ~15;
        int originZ = context.origin().getZ() & ~15;
        int minBuild = level.getMinY() + 6;
        int maxBuild = (level.getMaxY() + 1) - 8;
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
                double warp = 2.5 * Math.sin(x * 0.05) + 2.5 * Math.sin(z * 0.043);
                for (int y = minY; y <= maxY; y++) {
                    double band = Math.floorMod((long) Math.floor((y + warp) * 4.0), (long) (BAND_SPACING * 4.0)) / 4.0;
                    if (band >= BAND_THICKNESS) {
                        continue;
                    }
                    double distance = grid.signedDistance(x, y, z);
                    if (distance <= 0.0 || distance > LINING_DEPTH) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (isLineable(level.getBlockState(pos))) {
                        level.setBlock(pos, chitin.defaultBlockState(), 2);
                        placed = true;
                    }
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
