package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynSpireDressingFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<NormalNoise.NoiseParameters> SPIRE_NOISE = ResourceKey.create(
            Registries.NOISE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_spire")
    );
    private static final ResourceLocation CHITIN_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "chitin_block");
    private static final CavarynBlockSet STRIPEABLE = new CavarynBlockSet(Set.of(
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "myrmite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "loam"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "biomite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "biomite_turf"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "amber_moss_block")
    ));
    private static final double SHELL = 2.0;

    public CavarynSpireDressingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        NormalNoise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(SPIRE_NOISE);
        CavarynSpireGrid grid = CavarynSpireGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.getValue(1234.567, 89.123, -456.789),
                noise.getValue(-987.654, 12.345, 654.321)
        ));
        Block chitin = BuiltInRegistries.BLOCK.getOptional(CHITIN_ID).orElse(null);
        if (grid == null || chitin == null) {
            return false;
        }

        int originX = context.origin().getX() & ~15;
        int originZ = context.origin().getZ() & ~15;
        int maxBuild = level.getMaxBuildHeight() - 1;
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
                int maxY = Math.min(maxBuild, range[1]);
                for (int y = range[0]; y <= maxY; y++) {
                    if (!grid.onStripe(x, y, z, SHELL)) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (STRIPEABLE.contains(level.getBlockState(pos)) && exposed(level, pos)) {
                        level.setBlock(pos, chitin.defaultBlockState(), 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private static boolean exposed(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.isEmptyBlock(pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }
}
