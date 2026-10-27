package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public class FallenOuranwoodFeature implements Feature {
    public static final MapCodec<FallenOuranwoodFeature> CODEC = MapCodec.unit(FallenOuranwoodFeature::new);

    @Override
    public MapCodec<FallenOuranwoodFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {

        Direction.Axis axis = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
        int length = 36 + random.nextInt(44);
        int outerRadius = 1 + random.nextInt(3);
        boolean hollow = outerRadius >= 2;

        Block ouranwoodLog = BuiltInRegistries.BLOCK.getOptional(
                Identifier.fromNamespaceAndPath("antarchy", "ouranwood_log"))
                .orElse(AntarchyObjects.MOSSY_OURANWOOD_LOG.get());

        BlockState mossyLogState = AntarchyObjects.MOSSY_OURANWOOD_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
        BlockState freshLogState = ouranwoodLog.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
        BlockState woodState = AntarchyObjects.MOSSY_OURANWOOD_WOOD.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        float[] damage = new float[length];
        for (int i = 0; i < length; i++) damage[i] = random.nextFloat();
        for (int pass = 0; pass < 4; pass++) {
            for (int i = 1; i < length - 1; i++) {
                damage[i] = (damage[i - 1] + damage[i] * 2 + damage[i + 1]) / 4f;
            }
        }
        for (int i = 0; i < length; i++) damage[i] = damage[i] * damage[i];

        int anchorY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        boolean placed = false;

        for (int step = 0; step < length; step++) {
            int bx = axis == Direction.Axis.X ? origin.getX() + step : origin.getX();
            int bz = axis == Direction.Axis.Z ? origin.getZ() + step : origin.getZ();

            float dmg = damage[step];

            if (dmg > 0.78f) continue;

            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, bx, bz);
            int centerY = Math.max(anchorY, Math.min(anchorY + 2, surfaceY)) + outerRadius;

            for (int dy = -outerRadius; dy <= outerRadius; dy++) {
                for (int dp = -outerRadius; dp <= outerRadius; dp++) {
                    double dist = Math.sqrt(dy * dy + dp * dp);
                    if (dist > outerRadius + 0.5) continue;

                    boolean isShell = dist >= outerRadius - 0.5;

                    if (hollow && dist < outerRadius - 0.5) continue;

                    if (dmg > 0.55f && !isShell) continue;
                    if (dmg > 0.55f && random.nextFloat() < (dmg - 0.55f) * 3.5f) continue;

                    if (isShell && random.nextFloat() < dmg * 0.4f) continue;

                    int px = axis == Direction.Axis.X ? bx : bx + dp;
                    int pz = axis == Direction.Axis.Z ? bz : bz + dp;
                    BlockPos pos = new BlockPos(px, centerY + dy, pz);

                    if (!canReplace(level.getBlockState(pos))) continue;

                    float r = random.nextFloat();
                    BlockState toPlace;
                    if (r < 0.08f) {
                        toPlace = Blocks.MOSS_BLOCK.defaultBlockState();
                    } else if (r < 0.22f) {
                        toPlace = freshLogState;
                    } else if (r < 0.32f) {
                        toPlace = woodState;
                    } else {
                        toPlace = mossyLogState;
                    }

                    level.setBlock(pos, toPlace, 2);
                    placed = true;
                }
            }
        }

        int mossCount = 20 + random.nextInt(25);
        for (int i = 0; i < mossCount; i++) {
            int mx = origin.getX() + random.nextInt(length + 6) - 3;
            int mz = axis == Direction.Axis.X
                    ? origin.getZ() + random.nextInt(9) - 4
                    : origin.getZ() + random.nextInt(length + 6) - 3;
            int my = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mx, mz);
            BlockPos mossPos = new BlockPos(mx, my, mz);

            if (!canReplace(level.getBlockState(mossPos))) continue;
            BlockState below = level.getBlockState(mossPos.below());
            if (!below.isFaceSturdy(level, mossPos.below(), Direction.UP)) continue;

            level.setBlock(mossPos,
                    random.nextFloat() < 0.35f
                            ? Blocks.MOSS_BLOCK.defaultBlockState()
                            : Blocks.MOSS_CARPET.defaultBlockState(),
                    2);
        }

        return placed;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced() || (state.getBlock() instanceof LeavesBlock);
    }
}
