package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.sizeablefoliage.content.block.BigBushBlock;
import com.craisinlord.sizeablefoliage.content.block.VeryTallGrassBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ElythiaFloraFeature extends Feature<NoneFeatureConfiguration> {
    private static final String SIZEABLE_FOLIAGE_MODID = "sizeable_foliage";
    private static final ResourceLocation CAMELLIA_ID = new ResourceLocation("antarchy", "camellia");
    private static final ResourceLocation SPIDER_LILY_ID = new ResourceLocation("antarchy", "spider_lily");

    private final Variant variant;

    public ElythiaFloraFeature(Codec<NoneFeatureConfiguration> codec, Variant variant) {
        super(codec);
        this.variant = variant;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int attempts = switch (this.variant) {
            case FOREST -> 185;
            case MEADOW -> 95;
            case PEACH_FOREST -> 120;
            case BUTTERFLY_FIELDS -> 150;
            case FLOWER_FOREST_MILKWEED -> 14;
        };
        int radius = switch (this.variant) {
            case FOREST -> 8;
            case MEADOW, PEACH_FOREST -> 14;
            case BUTTERFLY_FIELDS -> 18;
            case FLOWER_FOREST_MILKWEED -> 8;
        };
        boolean placedAny = false;

        int torchflowerPatchRolls = switch (this.variant) {
            case BUTTERFLY_FIELDS -> 2 + random.nextInt(3);
            case PEACH_FOREST -> 1;
            case FLOWER_FOREST_MILKWEED -> 0;
            default -> 1;
        };
        for (int i = 0; i < torchflowerPatchRolls; i++) {
            float torchflowerChance = switch (this.variant) {
                case FOREST -> 0.24F;
                case MEADOW -> 0.42F;
                case PEACH_FOREST -> 0.08F;
                case BUTTERFLY_FIELDS -> 0.24F;
                case FLOWER_FOREST_MILKWEED -> 0.0F;
            };
            if (random.nextFloat() < torchflowerChance) {
                placedAny |= this.placeTorchflowerPatch(level, origin, random);
            }
        }

        if (random.nextFloat() < switch (this.variant) {
            case MEADOW -> 0.1F;
            case PEACH_FOREST -> 0.0F;
            case BUTTERFLY_FIELDS -> 0.12F;
            case FOREST -> 0.05F;
            case FLOWER_FOREST_MILKWEED -> 0.0F;
        }) {
            placedAny |= this.placePitcherPatch(level, origin, random);
        }
        if (random.nextFloat() < switch (this.variant) {
            case MEADOW -> 0.16F;
            case PEACH_FOREST -> 0.0F;
            case BUTTERFLY_FIELDS -> 0.95F;
            case FOREST -> 0.06F;
            case FLOWER_FOREST_MILKWEED -> 0.18F;
        }) {
            placedAny |= this.placeMilkweedPatch(level, origin, random, AntarchyObjects.ORANGE_MILKWEED.get());
        }
        if (random.nextFloat() < switch (this.variant) {
            case MEADOW -> 0.16F;
            case PEACH_FOREST -> 0.0F;
            case BUTTERFLY_FIELDS -> 0.95F;
            case FOREST -> 0.06F;
            case FLOWER_FOREST_MILKWEED -> 0.18F;
        }) {
            placedAny |= this.placeMilkweedPatch(level, origin, random, AntarchyObjects.PINK_MILKWEED.get());
        }
        if (this.variant == Variant.PEACH_FOREST && random.nextFloat() < 0.14F) {
            placedAny |= this.placeMilkweedPatch(level, origin, random, antarchyBlock(CAMELLIA_ID));
        }

        if (this.variant == Variant.FOREST || this.variant == Variant.PEACH_FOREST) {
            placedAny |= this.placeBigBushes(level, origin, random, radius);
        }

        for (int i = 0; i < attempts; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos plantPos = new BlockPos(x, y, z);
            BlockPos groundPos = plantPos.below();

            if (!isValidPlantSpot(level, plantPos, groundPos)) {
                continue;
            }

            normalizePeachForestGround(level, groundPos, this.variant);
            maybeOvergrowGround(level, groundPos, random, this.variant);
            if (placeSelectedPlant(level, plantPos, groundPos, random)) {
                placedAny = true;
            }
        }

        placedAny |= this.placeSeagrass(level, origin, random, radius);

        return placedAny;
    }

    private boolean placeSelectedPlant(WorldGenLevel level, BlockPos plantPos, BlockPos groundPos, RandomSource random) {
        if (this.variant == Variant.BUTTERFLY_FIELDS) {
            float roll = random.nextFloat();
            if (roll < 0.18F) {
                return placeDouble(level, plantPos, AntarchyObjects.ORANGE_MILKWEED.get().defaultBlockState());
            }
            if (roll < 0.36F) {
                return placeDouble(level, plantPos, AntarchyObjects.PINK_MILKWEED.get().defaultBlockState());
            }
            if (roll < 0.84F) {
                return placeSingle(level, plantPos, Blocks.GRASS);
            }
            return placeDouble(level, plantPos, Blocks.TALL_GRASS.defaultBlockState());
        }

        if (this.variant == Variant.FLOWER_FOREST_MILKWEED) {
            float roll = random.nextFloat();
            if (roll < 0.04F) {
                return placeDouble(level, plantPos, AntarchyObjects.ORANGE_MILKWEED.get().defaultBlockState());
            }
            if (roll < 0.08F) {
                return placeDouble(level, plantPos, AntarchyObjects.PINK_MILKWEED.get().defaultBlockState());
            }
            if (roll < 0.62F) {
                return placeSingle(level, plantPos, Blocks.GRASS);
            }
            if (roll < 0.84F) {
                return placeSingle(level, plantPos, Blocks.FERN);
            }
            return placeDouble(level, plantPos, Blocks.LARGE_FERN.defaultBlockState());
        }

        if (this.variant == Variant.PEACH_FOREST) {
            float roll = random.nextFloat();
            if (roll < 0.01F) {
                return placeSingle(level, plantPos, antarchyBlock(SPIDER_LILY_ID));
            }
            if (roll < 0.58F) {
                return placeSingle(level, plantPos, Blocks.GRASS);
            }
            return placeSingle(level, plantPos, veryShortGrass());
        }

        float roll = random.nextFloat();
        if (this.variant == Variant.MEADOW) {
            if (roll < 0.34F) {
                return placeSingle(level, plantPos, Blocks.GRASS);
            }
            if (roll < 0.78F) {
                return placeSingle(level, plantPos, veryShortGrass());
            }
            if (roll < 0.81F) {
                return placeDouble(level, plantPos, Blocks.TALL_GRASS.defaultBlockState());
            }
            return placeSingle(level, plantPos, veryShortGrass());
        }
        if (roll < 0.14F) {
            return placeSingle(level, plantPos, Blocks.GRASS);
        }
        if (roll < 0.52F) {
            return placeSingle(level, plantPos, veryShortGrass());
        }
        if (roll < 0.72F) {
            return placeSingle(level, plantPos, Blocks.FERN);
        }
        if (roll < 0.80F) {
            return placeDouble(level, plantPos, Blocks.TALL_GRASS.defaultBlockState());
        }
        if (roll < 0.84F) {
            return placeVeryTall(level, plantPos, veryTallGrass());
        }
        return placeDouble(level, plantPos, Blocks.LARGE_FERN.defaultBlockState());
    }

    private static Block veryShortGrass() {
        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(SIZEABLE_FOLIAGE_MODID, "very_short_grass"));
        return block == Blocks.AIR ? Blocks.GRASS : block;
    }

    private static Block veryTallGrass() {
        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(SIZEABLE_FOLIAGE_MODID, "very_tall_grass"));
        return block == Blocks.AIR ? Blocks.TALL_GRASS : block;
    }

    private static Block torchflowerBush() {
        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(SIZEABLE_FOLIAGE_MODID, "torchflower_bush"));
        return block == Blocks.AIR ? Blocks.TORCHFLOWER : block;
    }

    private static Block antarchyBlock(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Missing block: " + id);
        }
        return block;
    }

    private boolean placeBigBushes(WorldGenLevel level, BlockPos origin, RandomSource random, int radius) {
        Block bushBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation(SIZEABLE_FOLIAGE_MODID, "big_bush"));
        if (!(bushBlock instanceof BigBushBlock)) {
            return false;
        }

        boolean placed = false;
        int bushAttempts = switch (this.variant) {
            case PEACH_FOREST -> 1 + random.nextInt(2);
            default -> 12 + random.nextInt(7);
        };
        for (int i = 0; i < bushAttempts; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos plantPos = new BlockPos(x, y, z);

            if (!isValidPlantSpot(level, plantPos, plantPos.below())) {
                continue;
            }

            float roll = random.nextFloat();
            int age = roll < 0.4F ? 0 : roll < 0.75F ? 1 : 2;
            BlockState bushState = bushBlock.defaultBlockState().setValue(BigBushBlock.AGE, age);
            if (age > 0) {
                bushState = bushState.setValue(BigBushBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            }

            if (!isFootprintInsideChunk(origin, plantPos, bushState) || !canPlaceBigBush(level, plantPos, bushState)) {
                continue;
            }

            BigBushBlock.placeBushBlocks(level, plantPos, bushState, 2);
            placed = true;
        }
        return placed;
    }

    private static boolean isFootprintInsideChunk(BlockPos featureOrigin, BlockPos bushOrigin, BlockState bushState) {
        ChunkPos chunk = new ChunkPos(featureOrigin);
        int minX = chunk.getMinBlockX() + 1;
        int maxX = chunk.getMaxBlockX() - 1;
        int minZ = chunk.getMinBlockZ() + 1;
        int maxZ = chunk.getMaxBlockZ() - 1;
        if (!isWithin(bushOrigin, minX, maxX, minZ, maxZ)) {
            return false;
        }
        for (BlockPos partPos : BigBushBlock.partPositions(bushState, bushOrigin)) {
            if (!isWithin(partPos, minX, maxX, minZ, maxZ)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWithin(BlockPos pos, int minX, int maxX, int minZ, int maxZ) {
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    private static boolean canPlaceBigBush(WorldGenLevel level, BlockPos originPos, BlockState bushState) {
        if (bushState.getValue(BigBushBlock.AGE) == 0) {
            return bushState.canSurvive(level, originPos);
        }
        if (!BigBushBlock.hasFullSupport(bushState, level, originPos)) {
            return false;
        }
        for (BlockPos partPos : BigBushBlock.partPositions(bushState, originPos)) {
            if (!isOpenPlantSpot(level, partPos)) {
                return false;
            }
        }
        return true;
    }

    private static boolean placeSingle(WorldGenLevel level, BlockPos pos, Block block) {
        BlockState state = block.defaultBlockState();
        if (!isOpenPlantSpot(level, pos) || !state.canSurvive(level, pos)) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    private static boolean placeDouble(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!isOpenPlantSpot(level, pos) || !isOpenPlantSpot(level, pos.above()) || !state.canSurvive(level, pos)) {
            return false;
        }
        DoublePlantBlock.placeAt(level, state, pos, 2);
        return true;
    }

    private static boolean placeVeryTall(WorldGenLevel level, BlockPos pos, Block block) {
        if (!(block instanceof VeryTallGrassBlock)) {
            return placeDouble(level, pos, Blocks.TALL_GRASS.defaultBlockState());
        }
        BlockState state = block.defaultBlockState();
        if (!isOpenPlantSpot(level, pos)
                || !isOpenPlantSpot(level, pos.above())
                || !isOpenPlantSpot(level, pos.above(2))
                || !state.canSurvive(level, pos)) {
            return false;
        }
        VeryTallGrassBlock.placeAt(level, pos, 2);
        return true;
    }

    private boolean placeTorchflowerPatch(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos patchCenter = sampleSurface(level, origin, random, this.variant == Variant.FOREST ? 8 : 12);
        if (patchCenter == null) {
            return false;
        }

        int radius = this.variant == Variant.MEADOW ? 3 : this.variant == Variant.BUTTERFLY_FIELDS ? 3 : 2;
        int plants = this.variant == Variant.BUTTERFLY_FIELDS
                ? 4 + random.nextInt(5)
                : 3 + random.nextInt(this.variant == Variant.MEADOW ? 4 : 3);
        boolean bushEligible = this.variant == Variant.FOREST || this.variant == Variant.MEADOW;
        boolean placed = false;
        for (int i = 0; i < plants; i++) {
            BlockPos plantPos = samplePatchPos(level, patchCenter, radius, random);
            if (plantPos == null) {
                continue;
            }

            BlockPos groundPos = plantPos.below();
            if (!isValidPlantSpot(level, plantPos, groundPos)) {
                continue;
            }

            if (bushEligible && random.nextFloat() < 0.12F) {
                if (placeDouble(level, plantPos, torchflowerBush().defaultBlockState())) {
                    placed = true;
                }
                continue;
            }
            if (placeSingle(level, plantPos, Blocks.TORCHFLOWER)) {
                placed = true;
            }
        }
        return placed;
    }

    private boolean placePitcherPatch(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos patchCenter = sampleSurface(level, origin, random, this.variant == Variant.FOREST ? 8 : 13);
        if (patchCenter == null) {
            return false;
        }

        int radius = this.variant == Variant.BUTTERFLY_FIELDS ? 5 : this.variant == Variant.MEADOW ? 4 : this.variant == Variant.FLOWER_FOREST_MILKWEED ? 3 : 3;
        int plants = this.variant == Variant.BUTTERFLY_FIELDS ? 10 + random.nextInt(12) : this.variant == Variant.FLOWER_FOREST_MILKWEED ? 2 + random.nextInt(3) : 5 + random.nextInt(11);
        boolean placed = false;
        for (int i = 0; i < plants; i++) {
            BlockPos plantPos = samplePatchPos(level, patchCenter, radius, random);
            if (plantPos == null) {
                continue;
            }

            BlockPos groundPos = plantPos.below();
            if (!isValidPlantSpot(level, plantPos, groundPos)) {
                continue;
            }

            if (placeDouble(level, plantPos, Blocks.PITCHER_PLANT.defaultBlockState())) {
                placed = true;
            }
        }
        return placed;
    }

    private boolean placeMilkweedPatch(WorldGenLevel level, BlockPos origin, RandomSource random, Block flower) {
        BlockPos patchCenter = sampleSurface(level, origin, random, this.variant == Variant.FOREST ? 8 : 13);
        if (patchCenter == null) {
            return false;
        }

        int radius = this.variant == Variant.BUTTERFLY_FIELDS ? 7 : this.variant == Variant.MEADOW ? 4 : this.variant == Variant.FLOWER_FOREST_MILKWEED ? 3 : 3;
        int plants = this.variant == Variant.BUTTERFLY_FIELDS ? 14 + random.nextInt(16) : this.variant == Variant.FLOWER_FOREST_MILKWEED ? 2 + random.nextInt(4) : 4 + random.nextInt(9);
        boolean placed = false;
        for (int i = 0; i < plants; i++) {
            BlockPos plantPos = samplePatchPos(level, patchCenter, radius, random);
            if (plantPos == null) {
                continue;
            }

            BlockPos groundPos = plantPos.below();
            if (!isValidPlantSpot(level, plantPos, groundPos)) {
                continue;
            }

            if (placeDouble(level, plantPos, flower.defaultBlockState())) {
                placed = true;
            }
        }
        return placed;
    }

    private boolean placeSeagrass(WorldGenLevel level, BlockPos origin, RandomSource random, int radius) {
        int attempts = switch (this.variant) {
            case FOREST -> 3;
            case MEADOW -> 5;
            case PEACH_FOREST -> 3;
            case BUTTERFLY_FIELDS -> 3;
            case FLOWER_FOREST_MILKWEED -> 2;
        };
        boolean placed = false;

        for (int i = 0; i < attempts; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            BlockPos seagrassPos = new BlockPos(x, y, z);
            BlockPos floorPos = seagrassPos.below();

            if (!level.getFluidState(seagrassPos).isSourceOfType(net.minecraft.world.level.material.Fluids.WATER)) {
                continue;
            }

            if (!isUnderwaterGround(level, floorPos) || !Blocks.SEAGRASS.defaultBlockState().canSurvive(level, seagrassPos)) {
                continue;
            }

            level.setBlock(seagrassPos, Blocks.SEAGRASS.defaultBlockState(), 2);
            placed = true;
        }

        return placed;
    }

    private static BlockPos sampleSurface(WorldGenLevel level, BlockPos origin, RandomSource random, int radius) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos plantPos = new BlockPos(x, y, z);
            if (isValidPlantSpot(level, plantPos, plantPos.below())) {
                return plantPos;
            }
        }
        return null;
    }

    private static BlockPos samplePatchPos(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        int x = center.getX() + random.nextInt(radius * 2 + 1) - radius;
        int z = center.getZ() + random.nextInt(radius * 2 + 1) - radius;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos plantPos = new BlockPos(x, y, z);
        return isOpenPlantSpot(level, plantPos) ? plantPos : null;
    }

    private static void normalizePeachForestGround(WorldGenLevel level, BlockPos groundPos, Variant variant) {
        if (variant != Variant.PEACH_FOREST) {
            return;
        }

        BlockState state = level.getBlockState(groundPos);
        if (state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.ROOTED_DIRT)) {
            level.setBlock(groundPos, AntarchyObjects.BLUSH_MOSS_BLOCK.get().defaultBlockState(), 2);
        }
    }

    private static void maybeOvergrowGround(WorldGenLevel level, BlockPos groundPos, RandomSource random, Variant variant) {
        boolean meadow = variant != Variant.FOREST;
        float overgrowChance = switch (variant) {
            case PEACH_FOREST -> 0.20F;
            case FOREST -> 0.24F;
            default -> 0.45F;
        };
        if (!isValidGround(level, groundPos) || random.nextFloat() > overgrowChance) {
            return;
        }

        BlockState replacement = variant == Variant.PEACH_FOREST
                ? AntarchyObjects.BLUSH_MOSS_BLOCK.get().defaultBlockState()
                : random.nextFloat() < 0.35F
                ? Blocks.ROOTED_DIRT.defaultBlockState()
                : Blocks.MOSS_BLOCK.defaultBlockState();
        level.setBlock(groundPos, replacement, 2);

        for (DirectionHolder direction : DirectionHolder.HORIZONTAL) {
            float spreadChance = switch (variant) {
                case PEACH_FOREST -> 0.07F;
                case FOREST -> 0.1F;
                default -> 0.22F;
            };
            if (random.nextFloat() > spreadChance) {
                continue;
            }
            BlockPos spreadPos = groundPos.offset(direction.stepX, 0, direction.stepZ);
            if (isValidGround(level, spreadPos)) {
                level.setBlock(spreadPos, replacement, 2);
            }
        }
    }

    private static boolean isValidGround(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return isIntendedGround(state) && !state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES) && !state.is(Blocks.TUFF);
    }

    private static boolean isValidPlantSpot(WorldGenLevel level, BlockPos plantPos, BlockPos groundPos) {
        if (!isOpenPlantSpot(level, plantPos) || !isValidGround(level, groundPos)) {
            return false;
        }

        BlockState topState = level.getBlockState(groundPos);
        return hasPlantHeadroom(level, plantPos, 3)
                && Blocks.GRASS.defaultBlockState().canSurvive(level, plantPos)
                && !topState.is(BlockTags.LOGS)
                && !topState.is(BlockTags.LEAVES)
                && !topState.is(Blocks.TUFF)
                && !topState.is(Blocks.ANDESITE_SLAB);
    }

    private static boolean isIntendedGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(AntarchyObjects.BLUSH_MOSS_BLOCK.get());
    }

    private static boolean isUnderwaterGround(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return isValidGround(level, pos) || state.is(Blocks.GRAVEL) || state.is(Blocks.SAND) || state.is(Blocks.CLAY);
    }

    private static boolean isOpenPlantSpot(WorldGenLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos) && level.getFluidState(pos).isEmpty();
    }

    private static boolean hasPlantHeadroom(WorldGenLevel level, BlockPos pos, int height) {
        for (int y = 0; y < height; y++) {
            if (!isOpenPlantSpot(level, pos.above(y))) {
                return false;
            }
        }
        return true;
    }

    private enum DirectionHolder {
        NORTH(0, -1),
        SOUTH(0, 1),
        WEST(-1, 0),
        EAST(1, 0);

        private static final DirectionHolder[] HORIZONTAL = values();

        private final int stepX;
        private final int stepZ;

        DirectionHolder(int stepX, int stepZ) {
            this.stepX = stepX;
            this.stepZ = stepZ;
        }
    }

    public enum Variant {
        FOREST,
        MEADOW,
        PEACH_FOREST,
        BUTTERFLY_FIELDS,
        FLOWER_FOREST_MILKWEED
    }
}
