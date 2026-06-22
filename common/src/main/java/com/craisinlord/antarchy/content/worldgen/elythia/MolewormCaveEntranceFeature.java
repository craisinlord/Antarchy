package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MolewormCaveEntranceFeature extends Feature<NoneFeatureConfiguration> {
    public MolewormCaveEntranceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos surfaceAnchor = this.findSurfaceAnchor(level, context.origin(), random);
        if (surfaceAnchor == null) {
            return false;
        }

        BlockPos cavity = this.findWarrenAnchor(level, surfaceAnchor, random);
        if (cavity == null) {
            return false;
        }

        float entranceRadius = 4.0F + random.nextFloat() * 1.6F;
        this.raiseHabitationMound(level, random, surfaceAnchor, entranceRadius, 4 + random.nextInt(3));
        this.carveEntranceMouth(level, random, surfaceAnchor, entranceRadius);
        this.carveGuidedTunnel(level, random, surfaceAnchor, cavity);
        this.removeFloatingBlocks(level, surfaceAnchor, Mth.ceil(entranceRadius + 2.0F), surfaceAnchor.getY());
        this.carveNestChamber(level, random, cavity);
        MolewormWarrensFeature.carveWarrenNetwork(level, random, cavity, false);
        return true;
    }

    @Nullable
    private BlockPos findWarrenAnchor(WorldGenLevel level, BlockPos surfaceAnchor, RandomSource random) {
        int minY = level.getMinBuildHeight() + 18;
        int maxY = Math.max(minY + 12, surfaceAnchor.getY() - 24);
        if (maxY <= minY) {
            return null;
        }

        for (int attempt = 0; attempt < 36; attempt++) {
            int x = surfaceAnchor.getX() + random.nextInt(11) - 5;
            int z = surfaceAnchor.getZ() + random.nextInt(11) - 5;
            int y = Mth.clamp(surfaceAnchor.getY() - 28 - random.nextInt(34), minY, maxY);
            BlockPos candidate = new BlockPos(x, y, z);
            if (!canAccess(level, candidate)) {
                continue;
            }

            for (int dy = -8; dy <= 8; dy++) {
                BlockPos airPos = candidate.above(dy);
                if (this.isUsableCavity(level, airPos)) {
                    return airPos;
                }
            }

            BlockState candidateState = level.getBlockState(candidate);
            if (canReplaceShell(candidateState) || candidateState.isAir()) {
                return candidate;
            }
        }

        return null;
    }

    @Nullable
    private BlockPos findSurfaceAnchor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (surfaceY < level.getMinBuildHeight() + 8 || surfaceY > level.getMaxBuildHeight() - 8) {
                    continue;
                }

                BlockPos candidate = new BlockPos(x, surfaceY, z);
                if (!isValidSurface(level, candidate)) {
                    continue;
                }

                double dxScore = x - origin.getX();
                double dzScore = z - origin.getZ();
                double score = dxScore * dxScore + dzScore * dzScore + Math.abs(dx) * 0.5D + Math.abs(dz) * 0.5D + random.nextDouble() * 0.15D;
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }

        return best;
    }

    private void raiseHabitationMound(WorldGenLevel level, RandomSource random, BlockPos center, float radius, int height) {
        int minX = Mth.floor(center.getX() - radius);
        int maxX = Mth.floor(center.getX() + radius);
        int minZ = Mth.floor(center.getZ() - radius);
        int maxZ = Mth.floor(center.getZ() + radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }

                double dx = (x + 0.5D - (center.getX() + 0.5D)) / radius;
                double dz = (z + 0.5D - (center.getZ() + 0.5D)) / radius;
                double shape = 1.0D - (dx * dx + dz * dz);
                if (shape <= 0.0D) {
                    continue;
                }

                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                pos.set(x, topY, z);
                if (!isSoilOrStone(level.getBlockState(pos))) {
                    continue;
                }

                int localHeight = Math.max(1, (int) Math.floor(shape * shape * (height + 1) + random.nextDouble() * 0.6D));
                localHeight = Math.min(localHeight, 2);
                level.setBlock(pos, disturbedSurfaceState(random), 2);
                for (int depth = 1; depth <= 3; depth++) {
                    pos.set(x, topY - depth, z);
                    if (isSoilOrStone(level.getBlockState(pos))) {
                        level.setBlock(pos, disturbedSubsurfaceState(random), 2);
                    }
                }

                for (int y = 1; y <= localHeight; y++) {
                    pos.set(x, topY + y, z);
                    if (!level.isEmptyBlock(pos) || level.isEmptyBlock(pos.below())) {
                        continue;
                    }
                    level.setBlock(pos, disturbedSurfaceState(random), 2);
                    tryPlaceDeadBush(level, random, pos.above());
                }
            }
        }
    }

    private void removeFloatingBlocks(WorldGenLevel level, BlockPos center, int radius, int surfaceY) {
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

        for (int y = surfaceY + 1; y <= surfaceY + 8; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!canAccess(level, x, y, z) || !canAccess(level, x, y - 1, z)) {
                        continue;
                    }

                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }

                    belowPos.set(x, y - 1, z);
                    if (!level.getBlockState(belowPos).isAir()) {
                        continue;
                    }

                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private void carveEntranceMouth(WorldGenLevel level, RandomSource random, BlockPos surfaceAnchor, float radius) {
        Vec3 mouthCenter = Vec3.atCenterOf(surfaceAnchor).add(0.0D, 2.2D, 0.0D);
        this.carvePocket(level, random, mouthCenter, radius, 3.0F, radius, true);
        this.carvePocket(level, random, mouthCenter.add(0.0D, -2.0D, 0.0D), radius - 0.9F, 2.1F, radius - 0.9F, false);
    }

    private void carveGuidedTunnel(WorldGenLevel level, RandomSource random, BlockPos surfaceAnchor, BlockPos cavity) {
        Vec3 surface = Vec3.atCenterOf(surfaceAnchor).add(0.0D, 1.2D, 0.0D);
        Vec3 chamber = Vec3.atCenterOf(cavity);
        Vec3 horizontal = new Vec3(chamber.x - surface.x, 0.0D, chamber.z - surface.z);
        if (horizontal.lengthSqr() < 1.0D) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            horizontal = new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
        }
        horizontal = horizontal.normalize();

        Vec3 cursor = surface;
        int slopedSteps = 6 + random.nextInt(3);
        for (int step = 0; step < slopedSteps; step++) {
            Vec3 next = cursor.add(
                    horizontal.x * (1.8D + random.nextDouble() * 0.5D),
                    -(0.95D + random.nextDouble() * 0.25D),
                    horizontal.z * (1.8D + random.nextDouble() * 0.5D)
            );
            this.carveTube(level, random, cursor, next, 2.7F, 2.35F);
            cursor = next;
        }

        this.carveTube(level, random, cursor, chamber, 2.35F, 2.7F);
    }

    private void carveNestChamber(WorldGenLevel level, RandomSource random, BlockPos cavity) {
        Vec3 center = Vec3.atCenterOf(cavity);
        this.carvePocket(level, random, center, 4.5F + random.nextFloat() * 1.2F, 3.0F, 4.5F + random.nextFloat() * 1.2F, true);
        this.decorateFloor(level, random, center, 4.8F, 4.8F);
        this.decorateCeiling(level, random, center, 4.8F, 3.2F, 4.8F);
    }

    private void carveTube(WorldGenLevel level, RandomSource random, Vec3 start, Vec3 end, float startRadius, float endRadius) {
        Vec3 delta = end.subtract(start);
        double length = Math.max(delta.length(), 1.0D);
        int steps = Math.max(5, Mth.ceil(length * 2.3D));

        for (int step = 0; step <= steps; step++) {
            float progress = step / (float) steps;
            Vec3 center = start.add(delta.scale(progress));
            float radius = Mth.lerp(progress, startRadius, endRadius);
            this.carvePocket(level, random, center, radius, Math.max(1.85F, radius * 0.82F), radius, step % 4 == 0);
        }
    }

    private void carvePocket(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ, boolean decorate) {
        int minX = Mth.floor(center.x - radiusX - 1.5F);
        int maxX = Mth.floor(center.x + radiusX + 1.5F);
        int minY = Math.max(level.getMinBuildHeight() + 1, Mth.floor(center.y - radiusY - 2.0F));
        int maxY = Math.min(level.getMaxBuildHeight() - 2, Mth.floor(center.y + radiusY + 2.0F));
        int minZ = Mth.floor(center.z - radiusZ - 1.5F);
        int maxZ = Mth.floor(center.z + radiusZ + 1.5F);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

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
                    double distance = dx2 + dz2 + dy * dy + (hashNoise(x, y, z) - 0.5D) * 0.16D;
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (distance <= 1.0D) {
                        if (canCarve(state)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                        continue;
                    }

                    if (distance <= 1.22D && canReplaceShell(state)) {
                        level.setBlock(pos, selectShellState(random, center.y, y), 2);
                    }
                }
            }
        }

        if (decorate) {
            this.decorateFloor(level, random, center, radiusX, radiusZ);
            this.decorateCeiling(level, random, center, radiusX, radiusY, radiusZ);
        }
    }

    private void decorateFloor(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusZ) {
        int minX = Mth.floor(center.x - radiusX);
        int maxX = Mth.floor(center.x + radiusX);
        int minZ = Mth.floor(center.z - radiusZ);
        int maxZ = Mth.floor(center.z + radiusZ);
        int minY = Math.max(level.getMinBuildHeight() + 2, Mth.floor(center.y - 6.0D));
        int maxY = Math.min(level.getMaxBuildHeight() - 4, Mth.floor(center.y + 3.0D));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; y--) {
                    if (!canAccess(level, x, y, z) || !canAccess(level, x, y + 1, z)) {
                        continue;
                    }

                    pos.set(x, y, z);
                    if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) {
                        continue;
                    }

                    BlockPos floorPos = pos.below();
                    if (!canReplaceShell(level.getBlockState(floorPos))) {
                        continue;
                    }

                    level.setBlock(floorPos, random.nextFloat() < 0.14F ? Blocks.MUD.defaultBlockState() : selectFloorState(random), 2);
                    break;
                }
            }
        }
    }

    private void decorateCeiling(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ) {
        int minX = Mth.floor(center.x - radiusX);
        int maxX = Mth.floor(center.x + radiusX);
        int minY = Math.max(level.getMinBuildHeight() + 2, Mth.floor(center.y - radiusY));
        int maxY = Math.min(level.getMaxBuildHeight() - 3, Mth.floor(center.y + radiusY + 1.0F));
        int minZ = Mth.floor(center.z - radiusZ);
        int maxZ = Mth.floor(center.z + radiusZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState hangingRoots = Blocks.HANGING_ROOTS.defaultBlockState();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; y--) {
                    if (!canAccess(level, x, y, z) || !canAccess(level, x, y - 1, z)) {
                        continue;
                    }

                    pos.set(x, y, z);
                    if (level.isEmptyBlock(pos)) {
                        continue;
                    }

                    BlockPos airPos = pos.below();
                    if (!level.isEmptyBlock(airPos) || !canReplaceShell(level.getBlockState(pos))) {
                        break;
                    }

                    level.setBlock(pos, random.nextFloat() < 0.72F ? rootedDirtVariant(random) : coarseDirtVariant(random), 2);
                    if (random.nextFloat() < 0.34F && hangingRoots.canSurvive(level, airPos)) {
                        level.setBlock(airPos, hangingRoots, 2);
                    }
                    break;
                }
            }
        }

        decorateWalls(level, random, center, radiusX, radiusY, radiusZ);
    }

    private boolean isUsableCavity(WorldGenLevel level, BlockPos pos) {
        return canAccess(level, pos)
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && isSoilOrStone(level.getBlockState(pos.below()))
                && !level.isEmptyBlock(pos.above(3));
    }

    private static boolean isValidSurface(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return level.canSeeSky(pos.above())
                && level.getFluidState(pos.above()).isEmpty()
                && isSoilOrStone(state)
                && !state.is(Blocks.MUD);
    }

    private static BlockState disturbedSurfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.42F) {
            return coarseDirtVariant(random);
        }
        if (roll < 0.86F) {
            return rootedDirtVariant(random);
        }
        return Blocks.PODZOL.defaultBlockState();
    }

    private static BlockState disturbedSubsurfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.58F) {
            return rootedDirtVariant(random);
        }
        if (roll < 0.94F) {
            return coarseDirtVariant(random);
        }
        return Blocks.PODZOL.defaultBlockState();
    }

    private static BlockState selectShellState(RandomSource random, double centerY, int y) {
        return y > centerY + 0.25D
                ? (random.nextFloat() < 0.7F ? rootedDirtVariant(random) : coarseDirtVariant(random))
                : (random.nextFloat() < 0.56F ? coarseDirtVariant(random) : rootedDirtVariant(random));
    }

    private static BlockState selectFloorState(RandomSource random) {
        return random.nextFloat() < 0.54F ? coarseDirtVariant(random) : rootedDirtVariant(random);
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

    private static boolean isSoilOrStone(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(AntarchyObjects.INFESTED_ROOTED_DIRT.get())
                || state.is(AntarchyObjects.INFESTED_COARSE_DIRT.get())
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.TUFF);
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

    private static void decorateWalls(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ) {
        int minX = Mth.floor(center.x - radiusX);
        int maxX = Mth.floor(center.x + radiusX);
        int minY = Math.max(level.getMinBuildHeight() + 2, Mth.floor(center.y - radiusY));
        int maxY = Math.min(level.getMaxBuildHeight() - 3, Mth.floor(center.y + radiusY));
        int minZ = Mth.floor(center.z - radiusZ);
        int maxZ = Mth.floor(center.z + radiusZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (random.nextFloat() >= 0.028F || !canAccess(level, x, y, z)) {
                        continue;
                    }

                    pos.set(x, y, z);
                    if (!level.isEmptyBlock(pos)) {
                        continue;
                    }

                    tryPlaceGlowLichen(level, random, pos);
                }
            }
        }
    }

    private static void tryPlaceDeadBush(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (random.nextFloat() >= 0.06F) {
            return;
        }

        BlockState bush = Blocks.DEAD_BUSH.defaultBlockState();
        if (level.isEmptyBlock(pos) && bush.canSurvive(level, pos)) {
            level.setBlock(pos, bush, 2);
        }
    }

    private static void tryPlaceGlowLichen(WorldGenLevel level, RandomSource random, BlockPos pos) {
        Direction[] directions = Direction.values();
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[(i + random.nextInt(directions.length)) % directions.length];
            if (direction == Direction.DOWN) {
                continue;
            }

            BlockPos attachmentPos = pos.relative(direction);
            if (!canAccess(level, attachmentPos)) {
                continue;
            }

            BlockState attachmentState = level.getBlockState(attachmentPos);
            if (attachmentState.isAir() || !attachmentState.isFaceSturdy(level, attachmentPos, direction.getOpposite())) {
                continue;
            }

            BlockState lichen = Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(direction), true);
            if (lichen.canSurvive(level, pos)) {
                level.setBlock(pos, lichen, 2);
                return;
            }
        }
    }
}
