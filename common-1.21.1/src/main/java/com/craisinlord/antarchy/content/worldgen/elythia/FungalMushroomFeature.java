package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class FungalMushroomFeature extends Feature<NoneFeatureConfiguration> {

    public FungalMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        boolean isRed = random.nextBoolean();
        Block capBlock = isRed ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK;
        Block smallMushroom = isRed ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM;

        int stemSize = random.nextBoolean() ? 2 : 3;
        int stemHeight = 6 + random.nextInt(7);

        double leanAngle = random.nextDouble() * Math.PI * 2;
        double leanDX = Math.cos(leanAngle);
        double leanDZ = Math.sin(leanAngle);
        double maxLean = 2.0 + random.nextDouble() * 3.5;

        int baseY = origin.getY();
        int stemOffset = stemSize == 2 ? 0 : -1; // center 3x3 on origin

        BlockState stemPore = Blocks.MUSHROOM_STEM.defaultBlockState()
                .setValue(HugeMushroomBlock.NORTH, false)
                .setValue(HugeMushroomBlock.SOUTH, false)
                .setValue(HugeMushroomBlock.EAST, false)
                .setValue(HugeMushroomBlock.WEST, false)
                .setValue(HugeMushroomBlock.UP, false)
                .setValue(HugeMushroomBlock.DOWN, false);
        BlockState stemSide = Blocks.MUSHROOM_STEM.defaultBlockState()
                .setValue(HugeMushroomBlock.NORTH, true)
                .setValue(HugeMushroomBlock.SOUTH, true)
                .setValue(HugeMushroomBlock.EAST, true)
                .setValue(HugeMushroomBlock.WEST, true)
                .setValue(HugeMushroomBlock.UP, false)
                .setValue(HugeMushroomBlock.DOWN, false);

        for (int dy = 0; dy < stemHeight; dy++) {
            double leanProgress = (double) dy / stemHeight;
            int leanX = (int) Math.round(leanDX * maxLean * leanProgress);
            int leanZ = (int) Math.round(leanDZ * maxLean * leanProgress);

            for (int dx = 0; dx < stemSize; dx++) {
                for (int dz = 0; dz < stemSize; dz++) {
                    int wx = origin.getX() + stemOffset + dx + leanX;
                    int wz = origin.getZ() + stemOffset + dz + leanZ;
                    BlockPos pos = new BlockPos(wx, baseY + dy, wz);
                    if (!canReplace(level.getBlockState(pos))) continue;
                    boolean isEdge = (dx == 0 || dx == stemSize - 1 || dz == 0 || dz == stemSize - 1);
                    BlockState stemState = isEdge ? stemSide : stemPore;
                    level.setBlock(pos, stemState, 2);
                    BlockPos below = pos.below();
                    while (canReplace(level.getBlockState(below)) && below.getY() > level.getMinBuildHeight()) {
                        level.setBlock(below, stemState, 2);
                        below = below.below();
                    }
                }
            }
        }

        double capCenterX = origin.getX() + (stemSize == 2 ? 0.5 : 0) + leanDX * maxLean;
        double capCenterZ = origin.getZ() + (stemSize == 2 ? 0.5 : 0) + leanDZ * maxLean;
        int capTopY = baseY + stemHeight;
        int baseCapRadius = stemSize + 2 + random.nextInt(2);

        int jitterRange = baseCapRadius + 4;
        float[][] jitter = new float[jitterRange * 2 + 1][jitterRange * 2 + 1];
        for (float[] row : jitter) {
            for (int j = 0; j < row.length; j++) {
                row[j] = random.nextFloat() * 1.0f - 0.5f;
            }
        }

        int capLayers = baseCapRadius + 2;
        for (int cl = 0; cl < capLayers; cl++) {
            int capY = capTopY + cl;
            double nomRadius = baseCapRadius - cl;
            if (nomRadius < 0) break;

            // The cap center also shifts slightly with lean for a drooping overhang feel
            double layerCapX = capCenterX + leanDX * cl * 0.3;
            double layerCapZ = capCenterZ + leanDZ * cl * 0.3;

            for (int dx = -(jitterRange); dx <= jitterRange; dx++) {
                for (int dz = -(jitterRange); dz <= jitterRange; dz++) {
                    double dist = Math.sqrt(
                            (origin.getX() + dx - layerCapX) * (origin.getX() + dx - layerCapX)
                          + (origin.getZ() + dz - layerCapZ) * (origin.getZ() + dz - layerCapZ));

                    float j = jitter[dx + jitterRange][dz + jitterRange];
                    double effectiveRadius = nomRadius + j;

                    if (dist > effectiveRadius + 0.5) continue;
                    if (effectiveRadius < 0) continue;

                    BlockPos capPos = new BlockPos(origin.getX() + dx, capY, origin.getZ() + dz);
                    if (!canReplace(level.getBlockState(capPos))) continue;

                    boolean isOuter = dist >= effectiveRadius - 0.7;
                    boolean isTop = cl == capLayers - 1;

                    BlockState capState = capBlock.defaultBlockState()
                            .setValue(HugeMushroomBlock.UP, isTop || isOuter)
                            .setValue(HugeMushroomBlock.NORTH, isOuter)
                            .setValue(HugeMushroomBlock.SOUTH, isOuter)
                            .setValue(HugeMushroomBlock.EAST, isOuter)
                            .setValue(HugeMushroomBlock.WEST, isOuter)
                            .setValue(HugeMushroomBlock.DOWN, false);

                    level.setBlock(capPos, capState, 2);
                }
            }
        }

        int myceliumCount = 12 + random.nextInt(12);
        for (int i = 0; i < myceliumCount; i++) {
            int radius = stemSize + 3 + random.nextInt(3);
            int mx = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int mz = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int my = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, mx, mz) - 1;
            BlockPos myceliumPos = new BlockPos(mx, my, mz);

            BlockState existing = level.getBlockState(myceliumPos);
            if (existing.is(Blocks.GRASS_BLOCK) || existing.is(Blocks.DIRT)
                    || existing.is(Blocks.MOSS_BLOCK) || existing.is(Blocks.ROOTED_DIRT)) {
                level.setBlock(myceliumPos, Blocks.MYCELIUM.defaultBlockState(), 2);
            }
        }

        int smallCount = 5 + random.nextInt(8);
        for (int i = 0; i < smallCount; i++) {
            int radius = stemSize + 4 + random.nextInt(4);
            int sx = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int sz = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int sy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sx, sz);
            BlockPos smallPos = new BlockPos(sx, sy, sz);

            if (!canReplace(level.getBlockState(smallPos))) continue;
            BlockState below = level.getBlockState(smallPos.below());
            if (!below.isFaceSturdy(level, smallPos.below(), Direction.UP)) continue;

            Block plant = random.nextBoolean() ? smallMushroom
                    : (isRed ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM);
            BlockState plantState = plant.defaultBlockState();
            if (plantState.canSurvive(level, smallPos)) {
                level.setBlock(smallPos, plantState, 2);
            }
        }

        return true;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.canBeReplaced()
                || (state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock);
    }
}
