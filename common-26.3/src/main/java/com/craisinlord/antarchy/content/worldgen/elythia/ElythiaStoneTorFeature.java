package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ElythiaStoneTorFeature implements Feature {
    public static final MapCodec<ElythiaStoneTorFeature> CODEC = StoneTorConfiguration.CODEC.xmap(ElythiaStoneTorFeature::new, feature -> feature.configuration);
    private final StoneTorConfiguration configuration;

    private ElythiaStoneTorFeature(StoneTorConfiguration configuration) {
        this.configuration = configuration;
    }

    private static final int BURIED_LAYERS = 2;
    private static final int SEA_CLEARANCE = 2;

    @Override
    public MapCodec<ElythiaStoneTorFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {

        StoneTorConfiguration config = this.configuration;

        BlockPos ground = groundAt(level, config, origin.getX(), origin.getZ());
        if (!validGround(level, config, ground)) {
            return false;
        }
        BlockState block = StoneTorConfiguration.resolve(config.block()).defaultBlockState();
        BlockState accent = config.accent().map(id -> StoneTorConfiguration.resolve(id).defaultBlockState()).orElse(block);
        BlockState slab = StoneTorConfiguration.resolve(config.slab()).defaultBlockState();
        int span = Math.max(1, config.maxHeight() - config.minHeight() + 1);
        int height = config.minHeight() + random.nextInt(span);
        int seaLevel = chunkGenerator.getSeaLevel();
        int radiusBonus = config.breach() ? 1 : 0;
        boolean placed = placeSpire(level, config, seaLevel, ground, height, 2 + radiusBonus + random.nextInt(2), block, accent, slab, random);
        int satellites = 1 + random.nextInt(3);
        for (int i = 0; i < satellites; i++) {
            int x = origin.getX() + random.nextInt(9) - 4;
            int z = origin.getZ() + random.nextInt(9) - 4;
            BlockPos base = groundAt(level, config, x, z);
            if (validGround(level, config, base)) {
                placed |= placeSpire(level, config, seaLevel, base, Math.max(2, height / 2 + random.nextInt(3) - 1), 1 + radiusBonus + random.nextInt(2), block, accent, slab, random);
            }
        }
        return placed;
    }

    private static boolean placeSpire(WorldGenLevel level, StoneTorConfiguration config, int seaLevel, BlockPos ground, int height, int baseRadius,
                                      BlockState block, BlockState accent, BlockState slab, RandomSource random) {
        int limit = config.breach() ? height + Math.max(0, seaLevel - ground.getY())
                : config.submerged() ? Math.min(height, seaLevel + SEA_CLEARANCE - ground.getY()) : height;
        if (limit < 1) {
            return false;
        }
        boolean placed = false;
        int bandStart = 1 + random.nextInt(3);
        for (int y = -BURIED_LAYERS; y <= limit; y++) {
            double progress = Math.max(0.0D, y) / (double) limit;
            double radius = baseRadius * (1.0D - progress * progress * 0.8D) + 0.35D;
            boolean band = (y - bandStart) % 4 == 0 && y > 0;
            int reach = (int) Math.ceil(radius);
            for (int x = -reach; x <= reach; x++) {
                for (int z = -reach; z <= reach; z++) {
                    if (x * x + z * z > radius * radius + random.nextDouble() * 0.6D) {
                        continue;
                    }
                    BlockPos pos = ground.offset(x, y, z);
                    if (canCarve(level.getBlockState(pos))) {
                        level.setBlock(pos, band ? accent : block, 2);
                        placed = true;
                    }
                }
            }
        }
        BlockPos top = ground.above(limit + 1);
        if (placed && random.nextBoolean() && level.getBlockState(top.below()).is(block.getBlock()) && canCarve(level.getBlockState(top))) {
            BlockState cap = slab;
            if (cap.hasProperty(BlockStateProperties.WATERLOGGED) && level.getFluidState(top).is(FluidTags.WATER)) {
                cap = cap.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            if (cap.getBlock() instanceof SlabBlock) {
                level.setBlock(top, cap, 2);
            }
        }
        return placed;
    }

    private static BlockPos groundAt(WorldGenLevel level, StoneTorConfiguration config, int x, int z) {
        Heightmap.Types type = config.submerged() ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
        return new BlockPos(x, level.getHeight(type, x, z) - 1, z);
    }

    private static boolean validGround(WorldGenLevel level, StoneTorConfiguration config, BlockPos ground) {
        boolean wet = level.getFluidState(ground.above()).is(FluidTags.WATER);
        return wet == config.submerged() && isGround(level.getBlockState(ground));
    }

    static boolean canCarve(BlockState state) {
        return state.isAir() || state.canBeReplaced() || isGround(state);
    }

    static boolean isGround(BlockState state) {
        return state.is(ElythiaGeologyFeature.NATURAL_STONE) || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.MOSS_BLOCK);
    }
}
