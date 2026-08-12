package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;

public class MolewormSurfaceMoundsFeature extends Feature<NoneFeatureConfiguration> {
    public MolewormSurfaceMoundsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int chunkStartX = origin.getX() & ~15;
        int chunkStartZ = origin.getZ() & ~15;
        boolean placedAny = false;
        int tooLow = 0;
        int validCenters = 0;
        int invalidSurface = 0;
        int ventsCarved = 0;
        int aboveSurfaceBlocks = 0;

        int moundCount = 5 + random.nextInt(3);
        for (int i = 0; i < moundCount; i++) {
            int x = chunkStartX + random.nextInt(16);
            int z = chunkStartZ + random.nextInt(16);
            if (!canAccessColumn(level, x, z)) {
                continue;
            }
            int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
            if (topY <= level.getMinBuildHeight() + 2) {
                tooLow++;
                continue;
            }

            BlockPos center = new BlockPos(x, topY, z);
            if (!isValidSurface(level, center)) {
                invalidSurface++;
                continue;
            }

            validCenters++;
            float radiusX = 2.2F + random.nextFloat() * 2.8F;
            float radiusZ = 2.2F + random.nextFloat() * 2.8F;
            int height = 3 + random.nextInt(4);
            aboveSurfaceBlocks += this.raiseMound(level, random, center, radiusX, radiusZ, height);
            placedAny = true;

            if (random.nextFloat() < 0.9F) {
                if (this.carveVent(level, random, center, 1.5F + random.nextFloat() * 1.35F, 3 + random.nextInt(4))) {
                    ventsCarved++;
                }
            }
            if (random.nextFloat() < 0.16F) {
                this.placeSurfaceDripstone(level, random, center, 1.6F + random.nextFloat() * 1.2F);
            }
        }
        return placedAny;
    }

    private int raiseMound(WorldGenLevel level, RandomSource random, BlockPos center, float radiusX, float radiusZ, int height) {
        int minX = Mth.floor(center.getX() - radiusX);
        int maxX = Mth.floor(center.getX() + radiusX);
        int minZ = Mth.floor(center.getZ() - radiusZ);
        int maxZ = Mth.floor(center.getZ() + radiusZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int aboveSurfaceBlocks = 0;

        for (int x = minX; x <= maxX; x++) {
            double dx = (x + 0.5D - (center.getX() + 0.5D)) / radiusX;
            double dx2 = dx * dx;
            for (int z = minZ; z <= maxZ; z++) {
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }
                double dz = (z + 0.5D - (center.getZ() + 0.5D)) / radiusZ;
                double shape = 1.0D - (dx2 + dz * dz);
                if (shape <= 0.0D) {
                    continue;
                }

                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                pos.set(x, topY, z);
                if (!isSoilOrStone(level.getBlockState(pos))) {
                    continue;
                }

                int localHeight = Math.max(1, (int) Math.floor(shape * shape * (height + 1) + random.nextDouble() * 1.1D));
                level.setBlock(pos, disturbedSurfaceState(random), 2);
                for (int depth = 1; depth <= 2 + random.nextInt(2); depth++) {
                    pos.set(x, topY - depth, z);
                    if (isSoilOrStone(level.getBlockState(pos))) {
                        level.setBlock(pos, disturbedSubsurfaceState(random), 2);
                    }
                }

                if (localHeight <= 1 && shape < 0.42D) {
                    continue;
                }

                for (int y = 1; y <= localHeight; y++) {
                    pos.set(x, topY + y, z);
                    if (!level.isEmptyBlock(pos) || level.isEmptyBlock(pos.below())) {
                        continue;
                    }
                    level.setBlock(pos, disturbedSurfaceState(random), 2);
                    aboveSurfaceBlocks++;
                }
            }
        }

        return aboveSurfaceBlocks;
    }

    private boolean carveVent(WorldGenLevel level, RandomSource random, BlockPos center, float radius, int depth) {
        Vec3 cursor = Vec3.atCenterOf(center).add(0.0D, 2.1D, 0.0D);
        boolean carved = this.carvePocket(level, random, cursor, radius + 0.6F, 2.0F, radius + 0.6F);

        for (int step = 0; step < depth; step++) {
            cursor = cursor.add(
                    random.nextDouble() * 0.5D - 0.25D,
                    -(0.9D + random.nextDouble() * 0.9D),
                    random.nextDouble() * 0.5D - 0.25D
            );
            carved |= this.carvePocket(level, random, cursor, Math.max(0.95F, radius - step * 0.08F), 1.45F, Math.max(0.95F, radius - step * 0.08F));
        }

        return carved;
    }

    private boolean carvePocket(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ) {
        int minX = Mth.floor(center.x - radiusX - 1.0F);
        int maxX = Mth.floor(center.x + radiusX + 1.0F);
        int minY = Math.max(level.getMinBuildHeight() + 1, Mth.floor(center.y - radiusY - 1.0F));
        int maxY = Math.min(level.getMaxBuildHeight() - 2, Mth.floor(center.y + radiusY + 1.0F));
        int minZ = Mth.floor(center.z - radiusZ - 1.0F);
        int maxZ = Mth.floor(center.z + radiusZ + 1.0F);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean carvedAny = false;

        for (int x = minX; x <= maxX; x++) {
            double dx = (x + 0.5D - center.x) / radiusX;
            double dx2 = dx * dx;
            for (int z = minZ; z <= maxZ; z++) {
                double dz = (z + 0.5D - center.z) / radiusZ;
                double dz2 = dz * dz;
                for (int y = minY; y <= maxY; y++) {
                    if (!canAccess(level, x, y, z)) {
                        continue;
                    }
                    double dy = (y + 0.5D - center.y) / radiusY;
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    double distance = dx2 + dz2 + dy * dy + (hashNoise(x, y, z) - 0.5D) * 0.16D;

                    if (distance <= 1.0D) {
                        if (canCarve(state)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                            carvedAny = true;
                        }
                        continue;
                    }

                    if (distance <= 1.22D && canReplaceShell(state)) {
                        level.setBlock(pos, random.nextFloat() < 0.58F ? rootedDirtVariant(random) : coarseDirtVariant(random), 2);
                    }
                }
            }
        }

        return carvedAny;
    }

    private void placeSurfaceDripstone(WorldGenLevel level, RandomSource random, BlockPos center, float radius) {
        int minX = Mth.floor(center.getX() - radius);
        int maxX = Mth.floor(center.getX() + radius);
        int minZ = Mth.floor(center.getZ() - radius);
        int maxZ = Mth.floor(center.getZ() + radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState pointedDripstone = Blocks.POINTED_DRIPSTONE.defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }

                double dx = x + 0.5D - (center.getX() + 0.5D);
                double dz = z + 0.5D - (center.getZ() + 0.5D);
                if (dx * dx + dz * dz > radius * radius || random.nextFloat() < 0.35F) {
                    continue;
                }

                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                pos.set(x, topY, z);
                if (!isSoilOrStone(level.getBlockState(pos))) {
                    continue;
                }

                level.setBlock(pos, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
                int spikeHeight = 1 + random.nextInt(3);
                for (int y = 1; y <= spikeHeight; y++) {
                    pos.set(x, topY + y, z);
                    if (!level.isEmptyBlock(pos)) {
                        break;
                    }
                    if (!pointedDripstone.canSurvive(level, pos)) {
                        break;
                    }
                    level.setBlock(pos, pointedDripstone, 2);
                }
            }
        }
    }

    private static BlockState disturbedSurfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.46F) {
            return coarseDirtVariant(random);
        }
        if (roll < 0.82F) {
            return rootedDirtVariant(random);
        }
        return Blocks.PODZOL.defaultBlockState();
    }

    private static BlockState disturbedSubsurfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.52F) {
            return rootedDirtVariant(random);
        }
        if (roll < 0.9F) {
            return coarseDirtVariant(random);
        }
        return Blocks.PODZOL.defaultBlockState();
    }

    private static boolean isValidSurface(WorldGenLevel level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState state = level.getBlockState(pos);
        return level.getFluidState(abovePos).isEmpty()
                && isSoilOrStone(state)
                && !state.is(Blocks.MUD);
    }

    private static boolean isSoilOrStone(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(AntarchyObjects.INFESTED_ROOTED_DIRT.get())
                || state.is(AntarchyObjects.INFESTED_COARSE_DIRT.get())
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRAVEL);
    }

    private static boolean canCarve(BlockState state) {
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && !state.is(Blocks.BEDROCK)
                && !state.is(Blocks.BARRIER)
                && !state.is(Blocks.OBSIDIAN)
                && !state.is(Blocks.CRYING_OBSIDIAN);
    }

    private static boolean canReplaceShell(BlockState state) {
        return canCarve(state)
                && !state.is(BlockTags.LOGS)
                && !state.is(BlockTags.LEAVES)
                && !state.is(Blocks.WATER)
                && !state.is(Blocks.LAVA);
    }

    private static double hashNoise(int x, int y, int z) {
        long hash = x * 3129871L ^ z * 116129781L ^ y;
        hash = hash * hash * 42317861L + hash * 11L;
        return ((hash >> 16) & 1023L) / 1023.0D;
    }

    private static boolean canAccessColumn(WorldGenLevel level, int x, int z) {
        int probeY = Mth.clamp((level.getMinBuildHeight() + level.getMaxBuildHeight()) / 2, level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 2);
        return canAccess(level, x, probeY, z);
    }

    private static boolean canAccess(WorldGenLevel level, int x, int y, int z) {
        return canAccess(level, new BlockPos(x, Mth.clamp(y, level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 2), z));
    }

    private static boolean canAccess(WorldGenLevel level, BlockPos pos) {
        return !(level instanceof WorldGenRegion region) || region.ensureCanWrite(pos);
    }

    private static BlockState rootedDirtVariant(RandomSource random) {
        return random.nextFloat() < 0.12F
                ? AntarchyObjects.INFESTED_ROOTED_DIRT.get().defaultBlockState()
                : Blocks.ROOTED_DIRT.defaultBlockState();
    }

    private static BlockState coarseDirtVariant(RandomSource random) {
        return random.nextFloat() < 0.08F
                ? AntarchyObjects.INFESTED_COARSE_DIRT.get().defaultBlockState()
                : Blocks.COARSE_DIRT.defaultBlockState();
    }
}
