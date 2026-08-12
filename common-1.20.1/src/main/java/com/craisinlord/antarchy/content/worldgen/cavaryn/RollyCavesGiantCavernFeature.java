package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class RollyCavesGiantCavernFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<Biome> ROLLY_CAVES = ResourceKey.create(
            Registries.BIOME, new ResourceLocation(Antarchy.MODID, "rolly_caves"));
    private static final ResourceLocation GLOWCAP_MUSHROOM_ID = new ResourceLocation(Antarchy.MODID, "glowcap_mushroom");
    private static final ResourceLocation AMBER_MOSS_BLOCK_ID = new ResourceLocation(Antarchy.MODID, "amber_moss_block");
    private static final ResourceLocation OURANWOOD_WOOD_ID = new ResourceLocation(Antarchy.MODID, "ouranwood_wood");
    private static final ResourceLocation MOSSY_OURANWOOD_WOOD_ID = new ResourceLocation(Antarchy.MODID, "mossy_ouranwood_wood");
    private static final ResourceLocation MOSSY_OURANWOOD_LOG_ID = new ResourceLocation(Antarchy.MODID, "mossy_ouranwood_log");

    private static final int TRUNK_RADIUS = 2;
    private static final int MOSSY_PATCH_CHANCE = 4;

    private static final int BIOME_SAMPLE_RADIUS = 10;
    private static final int BIOME_SAMPLE_STEP = 5;
    private static final int MIN_BIOME_SAMPLE_MATCHES = 5;

    private static final int MIN_NATURAL_CLEARANCE = 6;
    private static final int SCAN_LIMIT = 48;

    private static final int CHAMBER_HEIGHT_MIN = 26;
    private static final int CHAMBER_HEIGHT_MAX = 46;
    private static final int CHAMBER_RADIUS_MIN = 8;
    private static final int CHAMBER_RADIUS_MAX = 12;
    private static final int BLOB_LAYER_COUNT = 9;
    private static final double MAX_DRIFT_FROM_CENTER = 6.0;

    private static final int MAX_PROCESSED_POSITIONS = 60000;

    private static final int FLOOR_DECORATION_ATTEMPTS = 40;
    private static final int CEILING_DECORATION_ATTEMPTS = 30;
    private static final int MAX_VINE_LENGTH = 10;

    public RollyCavesGiantCavernFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        try {
            return tryPlace(context);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean tryPlace(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        if (!isSurroundedByRollyCaves(level, origin)) {
            return false;
        }

        NaturalPocket pocket = findNaturalPocket(level, origin);
        if (pocket == null) {
            return false;
        }

        List<Blob> blobs = buildBlobs(random, pocket.center());
        int carvedPositions = carveBlobs(level, blobs);
        if (carvedPositions == 0) {
            return false;
        }

        placeTrunk(level, random, blobs);
        decorateFloor(level, random, blobs);
        decorateCeiling(level, random, blobs);

        return true;
    }

    private static void placeTrunk(WorldGenLevel level, RandomSource random, List<Blob> blobs) {
        Block woodBlock = getBlock(OURANWOOD_WOOD_ID);
        Block mossyWoodBlock = getBlock(MOSSY_OURANWOOD_WOOD_ID);
        Block mossyLogBlock = getBlock(MOSSY_OURANWOOD_LOG_ID);
        if (woodBlock == null || mossyWoodBlock == null || mossyLogBlock == null) {
            return;
        }

        Blob bottom = blobs.get(0);
        Blob top = blobs.get(blobs.size() - 1);
        int trunkBottomY = (int) Math.floor(bottom.y() - bottom.verticalRadius()) + 1;
        int trunkTopY = (int) Math.ceil(top.y() + top.verticalRadius()) - 1;

        BlockState woodState = woodBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState mossyWoodState = mossyWoodBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState mossyLogState = mossyLogBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = trunkBottomY; y <= trunkTopY; y++) {
            double[] center = interpolateCenter(blobs, y);
            int centerX = (int) Math.round(center[0]);
            int centerZ = (int) Math.round(center[1]);

            for (int dx = -TRUNK_RADIUS; dx <= TRUNK_RADIUS; dx++) {
                for (int dz = -TRUNK_RADIUS; dz <= TRUNK_RADIUS; dz++) {
                    if (dx * dx + dz * dz > TRUNK_RADIUS * TRUNK_RADIUS) {
                        continue;
                    }

                    mutable.set(centerX + dx, y, centerZ + dz);
                    BlockState existing = level.getBlockState(mutable);
                    if (existing.is(Blocks.BEDROCK) || level.getBlockEntity(mutable) != null) {
                        continue;
                    }
                    if (!existing.getFluidState().isEmpty()) {
                        continue;
                    }

                    boolean surface = dx * dx + dz * dz >= (TRUNK_RADIUS - 1) * (TRUNK_RADIUS - 1);
                    BlockState toPlace = woodState;
                    if (surface && random.nextInt(MOSSY_PATCH_CHANCE) == 0) {
                        toPlace = random.nextBoolean() ? mossyWoodState : mossyLogState;
                    }

                    level.setBlock(mutable, toPlace, 2);
                }
            }
        }
    }

    private static double[] interpolateCenter(List<Blob> blobs, int y) {
        Blob lower = blobs.get(0);
        Blob upper = blobs.get(blobs.size() - 1);

        for (int i = 0; i < blobs.size() - 1; i++) {
            if (y >= blobs.get(i).y() && y <= blobs.get(i + 1).y()) {
                lower = blobs.get(i);
                upper = blobs.get(i + 1);
                break;
            }
        }

        double span = upper.y() - lower.y();
        double t = span == 0 ? 0 : (y - lower.y()) / span;
        t = Math.max(0.0, Math.min(1.0, t));

        double x = lower.x() + (upper.x() - lower.x()) * t;
        double z = lower.z() + (upper.z() - lower.z()) * t;
        return new double[] {x, z};
    }

    private static boolean isSurroundedByRollyCaves(WorldGenLevel level, BlockPos origin) {
        int matches = 0;
        int samples = 0;
        for (int dx = -BIOME_SAMPLE_RADIUS; dx <= BIOME_SAMPLE_RADIUS; dx += BIOME_SAMPLE_STEP) {
            for (int dz = -BIOME_SAMPLE_RADIUS; dz <= BIOME_SAMPLE_RADIUS; dz += BIOME_SAMPLE_STEP) {
                samples++;
                if (level.getBiome(origin.offset(dx, 0, dz)).is(ROLLY_CAVES)) {
                    matches++;
                }
            }
        }
        return samples > 0 && matches >= MIN_BIOME_SAMPLE_MATCHES;
    }

    private static NaturalPocket findNaturalPocket(WorldGenLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = origin.mutable();

        if (!level.getBlockState(cursor).isAir()) {
            boolean found = false;
            for (int radius = 1; radius <= 6 && !found; radius++) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos candidate = origin.relative(direction, radius);
                    if (level.getBlockState(candidate).isAir()) {
                        cursor.set(candidate);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                return null;
            }
        }

        int floorY = cursor.getY();
        for (int steps = 0; steps < SCAN_LIMIT; steps++) {
            BlockPos below = new BlockPos(cursor.getX(), floorY - 1, cursor.getZ());
            if (!level.getBlockState(below).isAir()) {
                break;
            }
            floorY--;
        }

        int ceilingY = cursor.getY();
        for (int steps = 0; steps < SCAN_LIMIT; steps++) {
            BlockPos above = new BlockPos(cursor.getX(), ceilingY + 1, cursor.getZ());
            if (!level.getBlockState(above).isAir()) {
                break;
            }
            ceilingY++;
        }

        int clearance = ceilingY - floorY;
        if (clearance < MIN_NATURAL_CLEARANCE) {
            return null;
        }

        BlockPos center = new BlockPos(cursor.getX(), (floorY + ceilingY) / 2, cursor.getZ());
        return new NaturalPocket(center);
    }

    private static List<Blob> buildBlobs(RandomSource random, BlockPos center) {
        int chamberHeight = CHAMBER_HEIGHT_MIN + random.nextInt(CHAMBER_HEIGHT_MAX - CHAMBER_HEIGHT_MIN + 1);
        int baseY = center.getY() - chamberHeight / 2;

        List<Blob> blobs = new ArrayList<>(BLOB_LAYER_COUNT);
        double driftX = center.getX();
        double driftZ = center.getZ();

        for (int i = 0; i < BLOB_LAYER_COUNT; i++) {
            double t = (double) i / (BLOB_LAYER_COUNT - 1);
            double taper = Math.sin(Math.PI * t);
            int layerY = baseY + (int) Math.round(t * chamberHeight);

            double baseRadius = CHAMBER_RADIUS_MIN + (CHAMBER_RADIUS_MAX - CHAMBER_RADIUS_MIN) * taper;
            double jitter = 0.85 + random.nextDouble() * 0.3;
            double horizontalRadius = Math.max(2.5, baseRadius * jitter);
            double verticalRadius = Math.max(3.0, chamberHeight / (double) BLOB_LAYER_COUNT * 1.6);

            driftX = clampDrift(driftX + (random.nextDouble() - 0.5) * 3.0, center.getX());
            driftZ = clampDrift(driftZ + (random.nextDouble() - 0.5) * 3.0, center.getZ());

            blobs.add(new Blob(driftX, layerY, driftZ, horizontalRadius, verticalRadius));
        }

        return blobs;
    }

    private static double clampDrift(double value, double center) {
        return Math.max(center - MAX_DRIFT_FROM_CENTER, Math.min(center + MAX_DRIFT_FROM_CENTER, value));
    }

    private static int carveBlobs(WorldGenLevel level, List<Blob> blobs) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Blob blob : blobs) {
            minX = Math.min(minX, (int) Math.floor(blob.x() - blob.horizontalRadius()));
            maxX = Math.max(maxX, (int) Math.ceil(blob.x() + blob.horizontalRadius()));
            minY = Math.min(minY, (int) Math.floor(blob.y() - blob.verticalRadius()));
            maxY = Math.max(maxY, (int) Math.ceil(blob.y() + blob.verticalRadius()));
            minZ = Math.min(minZ, (int) Math.floor(blob.z() - blob.horizontalRadius()));
            maxZ = Math.max(maxZ, (int) Math.ceil(blob.z() + blob.horizontalRadius()));
        }

        BlockState caveAir = Blocks.CAVE_AIR.defaultBlockState();
        int processed = 0;
        int carved = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX && processed < MAX_PROCESSED_POSITIONS; x++) {
            for (int y = minY; y <= maxY && processed < MAX_PROCESSED_POSITIONS; y++) {
                for (int z = minZ; z <= maxZ && processed < MAX_PROCESSED_POSITIONS; z++) {
                    processed++;

                    if (!insideAnyBlob(blobs, x, y, z)) {
                        continue;
                    }

                    mutable.set(x, y, z);
                    BlockState state = level.getBlockState(mutable);
                    if (state.is(Blocks.BEDROCK) || !state.getFluidState().isEmpty()) {
                        continue;
                    }
                    if (level.getBlockEntity(mutable) != null) {
                        continue;
                    }
                    if (state.isAir()) {
                        continue;
                    }
                    if (!state.is(AntarchyTags.Blocks.ROLLY_CAVERN_CARVEABLE)) {
                        continue;
                    }

                    level.setBlock(mutable, caveAir, 2);
                    carved++;
                }
            }
        }

        return carved;
    }

    private static boolean insideAnyBlob(List<Blob> blobs, int x, int y, int z) {
        for (Blob blob : blobs) {
            double dx = (x + 0.5 - blob.x()) / blob.horizontalRadius();
            double dy = (y + 0.5 - blob.y()) / blob.verticalRadius();
            double dz = (z + 0.5 - blob.z()) / blob.horizontalRadius();
            if (dx * dx + dy * dy + dz * dz <= 1.0) {
                return true;
            }
        }
        return false;
    }

    private static void decorateFloor(WorldGenLevel level, RandomSource random, List<Blob> blobs) {
        Blob widest = widestBlob(blobs);
        Block glowcap = getBlock(GLOWCAP_MUSHROOM_ID);
        Block amberMoss = getBlock(AMBER_MOSS_BLOCK_ID);

        for (int attempt = 0; attempt < FLOOR_DECORATION_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * widest.horizontalRadius();
            int x = (int) Math.round(widest.x() + Math.cos(angle) * distance);
            int z = (int) Math.round(widest.z() + Math.sin(angle) * distance);

            BlockPos floorPos = findFloor(level, x, (int) Math.round(widest.y()), z);
            if (floorPos == null) {
                continue;
            }

            BlockPos abovePos = floorPos.above();
            if (!level.getBlockState(abovePos).isAir()) {
                continue;
            }

            int roll = random.nextInt(100);
            if (roll < 28) {
                level.setBlock(floorPos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                if (random.nextBoolean()) {
                    level.setBlock(abovePos, Blocks.MOSS_CARPET.defaultBlockState(), 2);
                }
            } else if (roll < 40) {
                level.setBlock(abovePos, Blocks.GRASS.defaultBlockState(), 2);
            } else if (roll < 50) {
                level.setBlock(abovePos, Blocks.SMALL_DRIPLEAF.defaultBlockState(), 2);
            } else if (roll < 58) {
                placeBigDripleaf(level, random, abovePos);
            } else if (roll < 74 && glowcap != null && amberMoss != null) {
                level.setBlock(floorPos, amberMoss.defaultBlockState(), 2);
                level.setBlock(abovePos, glowcap.defaultBlockState(), 2);
            }
        }
    }

    private static void placeBigDripleaf(WorldGenLevel level, RandomSource random, BlockPos basePos) {
        int height = 1 + random.nextInt(3);
        BlockPos.MutableBlockPos cursor = basePos.mutable();
        for (int i = 0; i < height; i++) {
            if (!level.getBlockState(cursor).isAir()) {
                return;
            }
            level.setBlock(cursor, Blocks.BIG_DRIPLEAF_STEM.defaultBlockState(), 2);
            cursor.move(Direction.UP);
        }
        if (level.getBlockState(cursor).isAir()) {
            level.setBlock(cursor, Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BlockStateProperties.TILT, Tilt.NONE), 2);
        }
    }

    private static void decorateCeiling(WorldGenLevel level, RandomSource random, List<Blob> blobs) {
        Blob widest = widestBlob(blobs);

        for (int attempt = 0; attempt < CEILING_DECORATION_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * widest.horizontalRadius();
            int x = (int) Math.round(widest.x() + Math.cos(angle) * distance);
            int z = (int) Math.round(widest.z() + Math.sin(angle) * distance);

            BlockPos ceilingAttach = findCeiling(level, x, (int) Math.round(widest.y()), z);
            if (ceilingAttach == null) {
                continue;
            }

            BlockPos hangPos = ceilingAttach.below();
            if (!level.getBlockState(hangPos).isAir()) {
                continue;
            }

            int roll = random.nextInt(100);
            if (roll < 30) {
                level.setBlock(hangPos, Blocks.SPORE_BLOSSOM.defaultBlockState(), 2);
            } else {
                placeCaveVineCurtain(level, random, hangPos);
            }
        }
    }

    private static void placeCaveVineCurtain(WorldGenLevel level, RandomSource random, BlockPos hangPos) {
        int length = availableAirLength(level, hangPos, MAX_VINE_LENGTH);
        if (length <= 0) {
            return;
        }

        BlockPos.MutableBlockPos cursor = hangPos.mutable();
        for (int i = 0; i < length - 1; i++) {
            boolean berries = random.nextInt(4) == 0;
            level.setBlock(cursor, Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVinesBlock.BERRIES, berries), 2);
            cursor.move(Direction.DOWN);
        }
        boolean tipBerries = random.nextInt(3) == 0;
        level.setBlock(cursor, Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVinesBlock.BERRIES, tipBerries), 2);
    }

    private static int availableAirLength(WorldGenLevel level, BlockPos start, int cap) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        int length = 0;
        while (length < cap && level.getBlockState(cursor).isAir()) {
            length++;
            cursor.move(Direction.DOWN);
        }
        return length;
    }

    private static BlockPos findFloor(WorldGenLevel level, int x, int startY, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, startY, z);
        for (int steps = 0; steps < SCAN_LIMIT; steps++) {
            BlockState state = level.getBlockState(cursor);
            BlockState below = level.getBlockState(cursor.below());
            if (state.isAir() && !below.isAir() && below.isFaceSturdy(level, cursor.below(), Direction.UP)) {
                return cursor.immutable();
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private static BlockPos findCeiling(WorldGenLevel level, int x, int startY, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, startY, z);
        for (int steps = 0; steps < SCAN_LIMIT; steps++) {
            BlockState state = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (state.isAir() && !above.isAir() && above.isFaceSturdy(level, cursor.above(), Direction.DOWN)) {
                return cursor.above().immutable();
            }
            cursor.move(Direction.UP);
        }
        return null;
    }

    private static Blob widestBlob(List<Blob> blobs) {
        Blob widest = blobs.get(0);
        for (Blob blob : blobs) {
            if (blob.horizontalRadius() > widest.horizontalRadius()) {
                widest = blob;
            }
        }
        return widest;
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }

    private record NaturalPocket(BlockPos center) {
    }

    private record Blob(double x, double y, double z, double horizontalRadius, double verticalRadius) {
    }
}
