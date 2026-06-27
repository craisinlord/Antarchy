package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class FallenOuranwoodFeature extends Feature<NoneFeatureConfiguration> {

    public FallenOuranwoodFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        Direction.Axis axis = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
        int length = 36 + random.nextInt(44);  // 36–79 blocks long
        int outerRadius = 1 + random.nextInt(3); // 1–3 (cross-section up to 7-wide)
        boolean hollow = outerRadius >= 2;

        Block ouranwoodLog = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath("antarchy", "ouranwood_log"))
                .orElse(AntarchyObjects.MOSSY_OURANWOOD_LOG.get());

        BlockState mossyLogState = AntarchyObjects.MOSSY_OURANWOOD_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
        BlockState freshLogState = ouranwoodLog.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);
        BlockState woodState = AntarchyObjects.MOSSY_OURANWOOD_WOOD.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        // Pre-generate a smoothed damage profile along the length (0=solid, 1=gap)
        float[] damage = new float[length];
        for (int i = 0; i < length; i++) damage[i] = random.nextFloat();
        for (int pass = 0; pass < 4; pass++) {
            for (int i = 1; i < length - 1; i++) {
                damage[i] = (damage[i - 1] + damage[i] * 2 + damage[i + 1]) / 4f;
            }
        }
        // Bias upward so more sections are heavily damaged
        for (int i = 0; i < length; i++) damage[i] = damage[i] * damage[i];

        int anchorY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        boolean placed = false;

        for (int step = 0; step < length; step++) {
            int bx = axis == Direction.Axis.X ? origin.getX() + step : origin.getX();
            int bz = axis == Direction.Axis.Z ? origin.getZ() + step : origin.getZ();

            float dmg = damage[step];

            // Full gap sections — log has completely rotted through here
            if (dmg > 0.78f) continue;

            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, bx, bz);
            int centerY = Math.max(anchorY, Math.min(anchorY + 2, surfaceY)) + outerRadius;

            for (int dy = -outerRadius; dy <= outerRadius; dy++) {
                for (int dp = -outerRadius; dp <= outerRadius; dp++) {
                    double dist = Math.sqrt(dy * dy + dp * dp);
                    if (dist > outerRadius + 0.5) continue;

                    boolean isShell = dist >= outerRadius - 0.5;

                    // Hollow interior: always skip center when hollow
                    if (hollow && dist < outerRadius - 0.5) continue;

                    // Heavy-damage sections: only keep fragments of the shell
                    if (dmg > 0.55f && !isShell) continue;
                    if (dmg > 0.55f && random.nextFloat() < (dmg - 0.55f) * 3.5f) continue;

                    // All sections: random rot holes punched through the shell
                    if (isShell && random.nextFloat() < dmg * 0.4f) continue;

                    int px = axis == Direction.Axis.X ? bx : bx + dp;
                    int pz = axis == Direction.Axis.Z ? bz : bz + dp;
                    BlockPos pos = new BlockPos(px, centerY + dy, pz);

                    if (!canReplace(level.getBlockState(pos))) continue;

                    // Block material mix: mostly mossy log, some fresh log patches, some moss
                    float r = random.nextFloat();
                    BlockState toPlace;
                    if (r < 0.08f) {
                        toPlace = Blocks.MOSS_BLOCK.defaultBlockState();
                    } else if (r < 0.22f) {
                        toPlace = freshLogState; // fresher wood, less decayed patch
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

        // Moss patches scattered along the log's footprint
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
