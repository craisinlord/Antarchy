package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynGlowcapGrottoDressingFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<NormalNoise.NoiseParameters> GROTTO_NOISE = ResourceKey.create(
            Registries.NOISE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_glowcap_grotto")
    );
    private static final ResourceLocation AMBER_MOSS_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "amber_moss_block");
    private static final Set<ResourceLocation> LINEABLE = Set.of(
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "myrmite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "broodstone"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "biomite"),
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "loam"),
            ResourceLocation.withDefaultNamespace("mud"),
            ResourceLocation.withDefaultNamespace("dirt"),
            ResourceLocation.withDefaultNamespace("coarse_dirt"),
            ResourceLocation.withDefaultNamespace("rooted_dirt"),
            ResourceLocation.withDefaultNamespace("podzol")
    );
    private static final double FLOOR_DEPTH = 1.6;
    private static final float MYCELIUM_CHANCE = 0.4F;

    public CavarynGlowcapGrottoDressingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        NormalNoise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(GROTTO_NOISE);
        CavarynCavityGrid grid = CavarynCavityGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.getValue(1234.567, 89.123, -456.789),
                noise.getValue(-987.654, 12.345, 654.321)
        ));
        Block amberMoss = block(AMBER_MOSS_ID);
        if (grid == null || amberMoss == null) {
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
                    if (distance <= 0.0 || distance > FLOOR_DEPTH || next > 0.0) {
                        continue;
                    }
                    pos.set(x, y, z);
                    above.set(x, y + 1, z);
                    if (!level.getBlockState(above).isAir() || !isLineable(level.getBlockState(pos))) {
                        continue;
                    }
                    BlockState floor = random.nextFloat() < MYCELIUM_CHANCE ? Blocks.MYCELIUM.defaultBlockState() : amberMoss.defaultBlockState();
                    level.setBlock(pos, floor, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean isLineable(BlockState state) {
        return LINEABLE.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static Block block(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
