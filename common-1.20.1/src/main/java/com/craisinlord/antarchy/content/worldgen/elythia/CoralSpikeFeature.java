package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralSpikeFeature extends Feature<NoneFeatureConfiguration> {

    private record CoralSet(
            Block block,
            Block coral,
            Block wallFan,
            Block floorFan,
            Block deadBlock,
            Block deadCoral,
            Block deadWallFan,
            Block deadFloorFan
    ) {}

    private static final CoralSet[] VANILLA_CORAL_SETS = {
        new CoralSet(Blocks.TUBE_CORAL_BLOCK,   Blocks.TUBE_CORAL,   Blocks.TUBE_CORAL_WALL_FAN,   Blocks.TUBE_CORAL_FAN,   Blocks.DEAD_TUBE_CORAL_BLOCK,   Blocks.DEAD_TUBE_CORAL,   Blocks.DEAD_TUBE_CORAL_WALL_FAN,   Blocks.DEAD_TUBE_CORAL_FAN),
        new CoralSet(Blocks.BRAIN_CORAL_BLOCK,  Blocks.BRAIN_CORAL,  Blocks.BRAIN_CORAL_WALL_FAN,  Blocks.BRAIN_CORAL_FAN,  Blocks.DEAD_BRAIN_CORAL_BLOCK,  Blocks.DEAD_BRAIN_CORAL,  Blocks.DEAD_BRAIN_CORAL_WALL_FAN,  Blocks.DEAD_BRAIN_CORAL_FAN),
        new CoralSet(Blocks.BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.DEAD_BUBBLE_CORAL, Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN),
        new CoralSet(Blocks.FIRE_CORAL_BLOCK,   Blocks.FIRE_CORAL,   Blocks.FIRE_CORAL_WALL_FAN,   Blocks.FIRE_CORAL_FAN,   Blocks.DEAD_FIRE_CORAL_BLOCK,   Blocks.DEAD_FIRE_CORAL,   Blocks.DEAD_FIRE_CORAL_WALL_FAN,   Blocks.DEAD_FIRE_CORAL_FAN),
        new CoralSet(Blocks.HORN_CORAL_BLOCK,   Blocks.HORN_CORAL,   Blocks.HORN_CORAL_WALL_FAN,   Blocks.HORN_CORAL_FAN,   Blocks.DEAD_HORN_CORAL_BLOCK,   Blocks.DEAD_HORN_CORAL,   Blocks.DEAD_HORN_CORAL_WALL_FAN,   Blocks.DEAD_HORN_CORAL_FAN),
    };

    public CoralSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int seaLevel = level.getSeaLevel();

        int spikes = 1 + random.nextInt(3);
        boolean placed = false;
        for (int i = 0; i < spikes; i++) {
            int offX = random.nextInt(11) - 5;
            int offZ = random.nextInt(11) - 5;
            placed |= placeSpike(level, random, origin.offset(offX, 0, offZ), seaLevel);
        }
        return placed;
    }

    private boolean placeSpike(WorldGenLevel level, RandomSource random, BlockPos origin, int seaLevel) {
        int floorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
        if (floorY >= seaLevel) return false;

        CoralSet[] coralSets = coralSets();
        CoralSet coral = coralSets[random.nextInt(coralSets.length)];

        int baseRadius = 2 + random.nextInt(2);           // 2-3 wide at base
        int totalHeight = 20 + random.nextInt(15);         // 20-34 blocks tall

        double curveAngle = random.nextDouble() * Math.PI * 2;
        double curveDX = Math.cos(curveAngle);
        double curveDZ = Math.sin(curveAngle);
        double maxCurve = 4.0 + random.nextDouble() * 5.0; // 4-9 block lean at tip

        boolean placed = false;

        for (int dy = 0; dy < totalHeight; dy++) {
            int blockY = floorY + dy;
            double progress = (double) dy / totalHeight;

            // Quadratic lean -- slow at base, bending hard near tip
            double curveOffset = maxCurve * progress * progress;
            double centerX = origin.getX() + curveDX * curveOffset;
            double centerZ = origin.getZ() + curveDZ * curveOffset;

            // Taper: full width for bottom 25%, then narrows sharply to a point
            double taperT = Math.max(0.0, (progress - 0.25) / 0.75);
            int radius = (int) Math.max(0, baseRadius * (1.0 - taperT * taperT * 1.3));

            for (int dx = -(radius + 1); dx <= radius + 1; dx++) {
                for (int dz = -(radius + 1); dz <= radius + 1; dz++) {
                    double dist = Math.sqrt(
                            (origin.getX() + dx - centerX) * (origin.getX() + dx - centerX)
                          + (origin.getZ() + dz - centerZ) * (origin.getZ() + dz - centerZ));

                    if (dist > radius + 0.5) continue;

                    BlockPos pos = new BlockPos(origin.getX() + dx, blockY, origin.getZ() + dz);
                    if (!canReplace(level.getBlockState(pos))) continue;

                    boolean inWater = touchesWater(level, pos);
                    level.setBlock(pos, inWater
                            ? coral.block().defaultBlockState()
                            : coral.deadBlock().defaultBlockState(),
                            2);
                    placed = true;

                    if (dist >= radius - 0.6 && radius > 0) {
                        BlockPos above = pos.above();
                        if (canReplace(level.getBlockState(above))) {
                            boolean aboveInWater = level.getBlockState(above).is(Blocks.WATER);
                            if (random.nextFloat() < 0.35f && aboveInWater) {
                                level.setBlock(above,
                                        coral.coral().defaultBlockState()
                                                .setValue(BlockStateProperties.WATERLOGGED, true),
                                        2);
                            }
                        }
                    }

                    if (dist >= radius - 0.5) {
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            if (random.nextFloat() >= 0.4f) continue;
                            BlockPos fanPos = pos.relative(dir);
                            BlockState existing = level.getBlockState(fanPos);
                            if (!existing.isAir() && !existing.is(Blocks.WATER)) continue;
                            boolean fanInWater = existing.is(Blocks.WATER);
                            if (fanInWater) {
                                level.setBlock(fanPos,
                                        coral.wallFan().defaultBlockState()
                                                .setValue(BlockStateProperties.HORIZONTAL_FACING, dir)
                                                .setValue(BlockStateProperties.WATERLOGGED, true),
                                        2);
                            } else {
                                level.setBlock(fanPos,
                                        coral.deadWallFan().defaultBlockState()
                                                .setValue(BlockStateProperties.HORIZONTAL_FACING, dir)
                                                .setValue(BlockStateProperties.WATERLOGGED, false),
                                        2);
                            }
                        }
                    }
                }
            }
        }

        // Floor scatter: coral plants, fans, and sea pickles around the base
        int decorCount = 10 + random.nextInt(12);
        for (int i = 0; i < decorCount; i++) {
            int fx = origin.getX() + random.nextInt(13) - 6;
            int fz = origin.getZ() + random.nextInt(13) - 6;
            int fy = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, fx, fz);
            BlockPos decorPos = new BlockPos(fx, fy, fz);

            if (!canReplace(level.getBlockState(decorPos))) continue;
            BlockState below = level.getBlockState(decorPos.below());
            if (below.isAir() || !below.isFaceSturdy(level, decorPos.below(), Direction.UP)) continue;

            boolean decorInWater = level.getBlockState(decorPos).is(Blocks.WATER);
            float roll = random.nextFloat();
            CoralSet randomCoral = coralSets[random.nextInt(coralSets.length)];

            if (roll < 0.35f) {
                if (decorInWater) {
                    BlockState coralState = randomCoral.coral().defaultBlockState()
                            .setValue(BlockStateProperties.WATERLOGGED, true);
                    if (coralState.canSurvive(level, decorPos)) {
                        level.setBlock(decorPos, coralState, 2);
                    }
                } else {
                    BlockState coralState = randomCoral.deadCoral().defaultBlockState()
                            .setValue(BlockStateProperties.WATERLOGGED, false);
                    if (coralState.canSurvive(level, decorPos)) {
                        level.setBlock(decorPos, coralState, 2);
                    }
                }
            } else if (roll < 0.65f) {
                if (decorInWater) {
                    BlockState fanState = randomCoral.floorFan().defaultBlockState()
                            .setValue(BlockStateProperties.WATERLOGGED, true);
                    if (fanState.canSurvive(level, decorPos)) {
                        level.setBlock(decorPos, fanState, 2);
                    }
                } else {
                    BlockState fanState = randomCoral.deadFloorFan().defaultBlockState()
                            .setValue(BlockStateProperties.WATERLOGGED, false);
                    if (fanState.canSurvive(level, decorPos)) {
                        level.setBlock(decorPos, fanState, 2);
                    }
                }
            } else if (decorInWater) {
                // Sea pickle cluster -- only underwater
                BlockState pickle = Blocks.SEA_PICKLE.defaultBlockState()
                        .setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4));
                if (pickle.canSurvive(level, decorPos)) {
                    level.setBlock(decorPos, pickle, 2);
                }
            }
        }

        return placed;
    }

    private static boolean touchesWater(WorldGenLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.WATER)) return true;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(Blocks.WATER)) return true;
        }
        return false;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.canBeReplaced();
    }

    private static CoralSet[] coralSets() {
        CoralSet starCoral = new CoralSet(
                lookupBlock("star_coral_block", Blocks.TUBE_CORAL_BLOCK),
                lookupBlock("star_coral", Blocks.TUBE_CORAL),
                lookupBlock("star_coral_wall_fan", Blocks.TUBE_CORAL_WALL_FAN),
                lookupBlock("star_coral_fan", Blocks.TUBE_CORAL_FAN),
                lookupBlock("dead_star_coral_block", Blocks.DEAD_TUBE_CORAL_BLOCK),
                lookupBlock("dead_star_coral", Blocks.DEAD_TUBE_CORAL),
                lookupBlock("dead_star_coral_wall_fan", Blocks.DEAD_TUBE_CORAL_WALL_FAN),
                lookupBlock("dead_star_coral_fan", Blocks.DEAD_TUBE_CORAL_FAN)
        );

        CoralSet[] combined = new CoralSet[VANILLA_CORAL_SETS.length + 1];
        System.arraycopy(VANILLA_CORAL_SETS, 0, combined, 0, VANILLA_CORAL_SETS.length);
        combined[VANILLA_CORAL_SETS.length] = starCoral;
        return combined;
    }

    private static Block lookupBlock(String path, Block fallback) {
        ResourceLocation id = new ResourceLocation(Antarchy.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
