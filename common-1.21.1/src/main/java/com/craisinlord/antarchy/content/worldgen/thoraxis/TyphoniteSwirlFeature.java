package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class TyphoniteSwirlFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation TYPHONITE_SPIKE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "typhonite_spike");

    public TyphoniteSwirlFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int height = Mth.randomBetweenInclusive(random, 15, 30);
        int arms = Mth.randomBetweenInclusive(random, 1, 3);
        double twist = (0.18D + random.nextDouble() * 0.22D) * (random.nextBoolean() ? 1.0D : -1.0D);
        double radius = 3.0D + random.nextDouble() * 4.0D;
        boolean placed = false;
        for (int y = 0; y < Math.max(4, height / 3); y++) {
            placed |= placeMass(level, origin.above(y), 1 + (y < 3 ? 1 : 0), random);
        }

        for (int arm = 0; arm < arms; arm++) {
            double phase = (Math.PI * 2.0D * arm / arms) + random.nextDouble() * 0.8D;
            for (int y = 0; y < height; y++) {
                double progress = y / (double) Math.max(1, height - 1);
                double angle = phase + y * twist;
                double currentRadius = radius * (0.12D + progress * 0.95D) + Math.sin(y * 0.7D + phase) * 1.2D;
                int x = Mth.floor(origin.getX() + Math.cos(angle) * currentRadius);
                int z = Mth.floor(origin.getZ() + Math.sin(angle) * currentRadius);
                BlockPos pos = new BlockPos(x, origin.getY() + y, z);
                if (placeMass(level, pos, 1 + (random.nextFloat() < 0.35F ? 1 : 0), random)) {
                    placed = true;
                    if (random.nextFloat() < 0.12F) {
                        placed |= placeSpike(level, pos, random, random.nextBoolean() ? Direction.UP : Direction.DOWN);
                    }
                }

                if (random.nextFloat() < 0.18F) {
                    BlockPos sidePos = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
                    if (placeMass(level, sidePos, 1, random)) {
                        placed = true;
                    }
                }
            }
        }

        return placed;
    }

    private static BlockState materialState(RandomSource random, int y, int height) {
        String blockName = random.nextFloat() < 0.18F ? "veined_typhonite" : "typhonite";
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, blockName));
        return block.defaultBlockState();
    }

    private static boolean placeIfAir(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        return true;
    }

    private static boolean placeMass(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        boolean placed = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + 1 || random.nextFloat() < 0.08F) {
                    continue;
                }
                placed |= placeIfAir(level, center.offset(dx, 0, dz), materialState(random, 0, 0));
            }
        }
        return placed;
    }

    private static boolean placeSpike(WorldGenLevel level, BlockPos pos, RandomSource random, Direction direction) {
        Block spike = BuiltInRegistries.BLOCK.get(TYPHONITE_SPIKE);
        int length = 2 + random.nextInt(4);
        boolean placed = false;
        for (int i = 0; i < length; i++) {
            BlockPos spikePos = pos.relative(direction, i + 1);
            if (!level.getBlockState(spikePos).isAir()) {
                break;
            }
            BlockState state = spike.defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)
                    .setValue(PointedDripstoneBlock.THICKNESS, i == length - 1 ? net.minecraft.world.level.block.state.properties.DripstoneThickness.TIP : net.minecraft.world.level.block.state.properties.DripstoneThickness.MIDDLE)
                    .setValue(PointedDripstoneBlock.WATERLOGGED, false);
            if (!state.canSurvive(level, spikePos)) {
                break;
            }
            level.setBlock(spikePos, state, Block.UPDATE_CLIENTS);
            placed = true;
        }
        return placed;
    }
}
