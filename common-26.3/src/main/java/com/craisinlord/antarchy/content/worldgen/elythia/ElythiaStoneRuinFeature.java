package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ElythiaStoneRuinFeature implements Feature {
    public static final MapCodec<ElythiaStoneRuinFeature> CODEC = StoneRuinConfiguration.CODEC.xmap(ElythiaStoneRuinFeature::new, feature -> feature.configuration);
    private final StoneRuinConfiguration configuration;

    private ElythiaStoneRuinFeature(StoneRuinConfiguration configuration) {
        this.configuration = configuration;
    }

    private static final int MAX_HALF_WIDTH = 4;
    private static final int MAX_GROUND_DIFFERENCE = 3;

    @Override
    public MapCodec<ElythiaStoneRuinFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        
        StoneRuinConfiguration config = this.configuration;
        
        
        int halfX = 2 + random.nextInt(MAX_HALF_WIDTH - 1);
        int halfZ = 2 + random.nextInt(MAX_HALF_WIDTH - 1);
        BlockPos center = groundAt(level, config, origin.getX(), origin.getZ());
        if (!validGround(level, config, center) || !isLevel(level, config, center, halfX, halfZ)) {
            return false;
        }
        Palette palette = Palette.of(config);
        boolean placed = false;
        for (int x = -halfX; x <= halfX; x++) {
            for (int z = -halfZ; z <= halfZ; z++) {
                BlockPos ground = groundAt(level, config, origin.getX() + x, origin.getZ() + z);
                if (!validGround(level, config, ground)) {
                    continue;
                }
                boolean edgeX = Math.abs(x) == halfX;
                boolean edgeZ = Math.abs(z) == halfZ;
                if (edgeX && edgeZ) {
                    placed |= placePillar(level, ground, palette, random);
                } else if (edgeX || edgeZ) {
                    placed |= placeWall(level, ground, palette, random);
                } else if (random.nextFloat() < 0.55F) {
                    setStone(level, ground, palette.pick(random));
                    placed = true;
                    if (random.nextFloat() < 0.12F) {
                        setStone(level, ground.above(), palette.slab());
                    }
                }
            }
        }
        if (placed) {
            setStone(level, center, palette.chiseled());
            config.lootTable().ifPresent(lootTable -> placeCache(level, center.below(), lootTable, random));
        }
        return placed;
    }

    private static void placeCache(WorldGenLevel level, BlockPos pos, Identifier lootTable, RandomSource random) {
        BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
        level.setBlock(pos, chest, 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainer container) {
            container.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable), random.nextLong());
        }
    }

    private static boolean placePillar(WorldGenLevel level, BlockPos ground, Palette palette, RandomSource random) {
        if (random.nextFloat() < 0.15F) {
            return false;
        }
        int height = 2 + random.nextInt(4);
        for (int y = 0; y <= height; y++) {
            setStone(level, ground.above(y), palette.pillar());
        }
        setStone(level, ground.above(height + 1), random.nextBoolean() ? palette.wall() : palette.slab());
        return true;
    }

    private static boolean placeWall(WorldGenLevel level, BlockPos ground, Palette palette, RandomSource random) {
        if (random.nextFloat() < 0.3F) {
            return false;
        }
        int height = random.nextInt(4);
        for (int y = 0; y <= height; y++) {
            setStone(level, ground.above(y), palette.pick(random));
        }
        if (random.nextFloat() < 0.35F) {
            setStone(level, ground.above(height + 1), palette.slab());
        }
        return true;
    }

    private static void setStone(WorldGenLevel level, BlockPos pos, BlockState state) {
        BlockState target = state;
        if (target.hasProperty(BlockStateProperties.WATERLOGGED) && level.getFluidState(pos).is(FluidTags.WATER)) {
            target = target.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        level.setBlock(pos, target, 2);
    }

    private static boolean isLevel(WorldGenLevel level, StoneRuinConfiguration config, BlockPos center, int halfX, int halfZ) {
        for (int x = -halfX; x <= halfX; x += halfX) {
            for (int z = -halfZ; z <= halfZ; z += halfZ) {
                BlockPos corner = groundAt(level, config, center.getX() + x, center.getZ() + z);
                if (Math.abs(corner.getY() - center.getY()) > MAX_GROUND_DIFFERENCE) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockPos groundAt(WorldGenLevel level, StoneRuinConfiguration config, int x, int z) {
        Heightmap.Types type = config.submerged() ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
        return new BlockPos(x, level.getHeight(type, x, z) - 1, z);
    }

    private static boolean validGround(WorldGenLevel level, StoneRuinConfiguration config, BlockPos ground) {
        boolean wet = level.getFluidState(ground.above()).is(FluidTags.WATER);
        BlockState state = level.getBlockState(ground);
        return wet == config.submerged()
                && (state.is(ElythiaGeologyFeature.NATURAL_STONE) || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.SAND) || state.is(Blocks.GRAVEL));
    }

    private record Palette(BlockState bricks, BlockState cracked, BlockState mossy, BlockState pillar, BlockState chiseled, BlockState slab, BlockState wall) {
        private static Palette of(StoneRuinConfiguration config) {
            BlockState bricks = state(config.bricks());
            return new Palette(
                    bricks,
                    config.cracked().map(StoneTorConfiguration::resolve).map(Block::defaultBlockState).orElse(bricks),
                    config.mossy().map(StoneTorConfiguration::resolve).map(Block::defaultBlockState).orElse(bricks),
                    state(config.pillar()),
                    state(config.chiseled()),
                    state(config.slab()),
                    state(config.wall()));
        }

        private static BlockState state(net.minecraft.resources.Identifier id) {
            return StoneTorConfiguration.resolve(id).defaultBlockState();
        }

        private BlockState pick(RandomSource random) {
            float roll = random.nextFloat();
            if (roll < 0.25F) {
                return this.mossy;
            }
            return roll < 0.45F ? this.cracked : this.bricks;
        }
    }
}
