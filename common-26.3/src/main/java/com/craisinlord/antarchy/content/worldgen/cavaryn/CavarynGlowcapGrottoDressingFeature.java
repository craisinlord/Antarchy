package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CavarynGlowcapGrottoDressingFeature implements Feature {
    public static final ResourceKey<NormalNoise> GROTTO_NOISE = ResourceKey.create(
            Registries.NOISE,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn_glowcap_grotto")
    );
    private static final Identifier AMBER_MOSS_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "amber_moss_block");
    private static final Set<Identifier> LINEABLE = Set.of(
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "myrmite"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "broodstone"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "biomite"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "loam"),
            Identifier.withDefaultNamespace("mud"),
            Identifier.withDefaultNamespace("dirt"),
            Identifier.withDefaultNamespace("coarse_dirt"),
            Identifier.withDefaultNamespace("rooted_dirt"),
            Identifier.withDefaultNamespace("podzol")
    );
    private static final CavarynBlockSet LINEABLE_BLOCKS = new CavarynBlockSet(LINEABLE);
    private static final double FLOOR_DEPTH = 1.6;
    private static final float MYCELIUM_CHANCE = 0.4F;

    public static final MapCodec<CavarynGlowcapGrottoDressingFeature> CODEC = MapCodec.unit(CavarynGlowcapGrottoDressingFeature::new);

    @Override
    public MapCodec<CavarynGlowcapGrottoDressingFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        net.minecraft.world.level.levelgen.synth.Noise noise = level.getLevel().getChunkSource().randomState().getOrCreateNoise(GROTTO_NOISE);
        CavarynCavityGrid grid = CavarynCavityGrid.forSalt(CavarynWormBoreFunction.saltFor(
                noise.get(1234.567, 89.123, -456.789),
                noise.get(-987.654, 12.345, 654.321)
        ));
        Block amberMoss = block(AMBER_MOSS_ID);
        if (grid == null || amberMoss == null) {
            return false;
        }

        int originX = origin.getX() & ~15;
        int originZ = origin.getZ() & ~15;
        int minBuild = level.getMinY() + 6;
        int maxBuild = (level.getMaxY() + 1) - 8;
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
        return LINEABLE_BLOCKS.contains(state);
    }

    private static Block block(Identifier id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
