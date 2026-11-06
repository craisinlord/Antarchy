package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RiverLogBridgeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int OVERHANG = 2;
    private static final ResourceKey<Biome> PEACH_FOREST = biome("peach_forest");
    private static final List<ResourceKey<Biome>> OURANWOOD_FORESTS = List.of(
            biome("ouranwood_forest"),
            biome("sparse_ouranwood_forest"),
            biome("fungal_ouranwood_forest"),
            biome("fungal_reserve")
    );

    public RiverLogBridgeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Optional<ElythiaRiverCrossing> found = ElythiaRiverCrossing.find(level, context.origin(), random, true, OVERHANG + 1);
        if (found.isEmpty()) {
            return false;
        }

        ElythiaRiverCrossing crossing = found.get();
        Direction.Axis axis = crossing.dx() != 0 ? Direction.Axis.X : Direction.Axis.Z;
        Forest startForest = forestAt(level, crossing, 0, -OVERHANG - 1);
        Forest endForest = forestAt(level, crossing, crossing.steps(), crossing.steps() + OVERHANG + 1);
        if (startForest == Forest.NONE || endForest == Forest.NONE) {
            return false;
        }
        Palette palette = startForest == Forest.PEACH || endForest == Forest.PEACH ? peachPalette(axis) : ouranwoodPalette(axis);
        boolean thick = random.nextBoolean();
        int perpX = -crossing.dz();
        int perpZ = crossing.dx();
        int centerY = Math.max(crossing.waterY() + 1, Math.min(crossing.startGround(), crossing.endGround()) + 1);
        int from = -OVERHANG - random.nextInt(2);
        int to = crossing.steps() + OVERHANG + random.nextInt(2);

        float[] damage = new float[to - from + 1];
        for (int i = 0; i < damage.length; i++) damage[i] = random.nextFloat();
        for (int pass = 0; pass < 4; pass++) {
            for (int i = 1; i < damage.length - 1; i++) {
                damage[i] = (damage[i - 1] + damage[i] * 2 + damage[i + 1]) / 4f;
            }
        }

        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int step = from; step <= to; step++) {
            boolean overWater = step > 0 && step < crossing.steps();
            float dmg = damage[step - from] * damage[step - from];
            if (!overWater && dmg > 0.5F) continue;

            int x = crossing.x(step);
            int z = crossing.z(step);
            placed |= this.placeLog(level, crossing, pos.set(x, centerY, z), palette.pick(random));
            if (!thick) continue;

            int[][] shell = {{perpX, 0, perpZ}, {-perpX, 0, -perpZ}, {0, 1, 0}, {0, -1, 0}};
            for (int[] o : shell) {
                if (!overWater && random.nextFloat() < dmg * 1.5F) continue;
                if (o[1] == 0 && random.nextFloat() < 0.12F) continue;
                placed |= this.placeLog(level, crossing, pos.set(x + o[0], centerY + o[1], z + o[2]), palette.pick(random));
            }
        }

        if (!placed) {
            return false;
        }

        int topY = centerY + (thick ? 1 : 0);
        for (int step = from; step <= to; step++) {
            if (random.nextFloat() >= 0.3F) continue;
            int x = crossing.x(step);
            int z = crossing.z(step);
            if (!crossing.contains(x, z)) continue;
            BlockState below = level.getBlockState(pos.set(x, topY, z));
            if (!below.is(Blocks.MOSS_BLOCK) && below.getBlock() instanceof RotatedPillarBlock && level.getBlockState(pos.set(x, topY + 1, z)).isAir()) {
                level.setBlock(pos, Blocks.MOSS_CARPET.defaultBlockState(), 2);
            }
        }

        this.mossBank(level, random, crossing, crossing.x(0), crossing.z(0));
        this.mossBank(level, random, crossing, crossing.x(crossing.steps()), crossing.z(crossing.steps()));
        return true;
    }

    private boolean placeLog(WorldGenLevel level, ElythiaRiverCrossing crossing, BlockPos pos, BlockState state) {
        if (!crossing.contains(pos.getX(), pos.getZ()) || !canReplace(level.getBlockState(pos))) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    private void mossBank(WorldGenLevel level, RandomSource random, ElythiaRiverCrossing crossing, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int count = 4 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            int mx = x + random.nextInt(5) - 2;
            int mz = z + random.nextInt(5) - 2;
            if (!crossing.contains(mx, mz)) continue;
            pos.set(mx, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mx, mz), mz);
            if (!level.getBlockState(pos).isAir()) continue;
            BlockPos below = pos.below();
            if (!level.getFluidState(below).isEmpty() || !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            level.setBlock(pos, random.nextFloat() < 0.3F ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.MOSS_CARPET.defaultBlockState(), 2);
        }
    }

    private static Forest forestAt(WorldGenLevel level, ElythiaRiverCrossing crossing, int... steps) {
        Forest found = Forest.NONE;
        for (int step : steps) {
            Holder<Biome> biome = level.getBiome(new BlockPos(crossing.x(step), crossing.waterY(), crossing.z(step)));
            if (biome.is(PEACH_FOREST)) {
                return Forest.PEACH;
            }
            if (OURANWOOD_FORESTS.stream().anyMatch(biome::is)) {
                found = Forest.OURANWOOD;
            }
        }
        return found;
    }

    private static ResourceKey<Biome> biome(String path) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(Antarchy.MODID, path));
    }

    private enum Forest {
        NONE,
        OURANWOOD,
        PEACH
    }

    private static Palette ouranwoodPalette(Direction.Axis axis) {
        return new Palette(
                AntarchyObjects.MOSSY_OURANWOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis),
                lookup("ouranwood_log", AntarchyObjects.MOSSY_OURANWOOD_LOG.get()).defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis),
                AntarchyObjects.MOSSY_OURANWOOD_WOOD.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis)
        );
    }

    private static Palette peachPalette(Direction.Axis axis) {
        Block log = lookup("peach_log", AntarchyObjects.MOSSY_OURANWOOD_LOG.get());
        return new Palette(
                log.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis),
                log.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis),
                lookup("peach_wood", log).defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis)
        );
    }

    private static Block lookup(String path, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(Antarchy.MODID, path)).orElse(fallback);
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty() || state.getBlock() instanceof LeavesBlock;
    }

    private record Palette(BlockState main, BlockState fresh, BlockState bark) {
        BlockState pick(RandomSource random) {
            float roll = random.nextFloat();
            if (roll < 0.08F) {
                return Blocks.MOSS_BLOCK.defaultBlockState();
            }
            if (roll < 0.22F) {
                return this.fresh;
            }
            if (roll < 0.32F) {
                return this.bark;
            }
            return this.main;
        }
    }
}
