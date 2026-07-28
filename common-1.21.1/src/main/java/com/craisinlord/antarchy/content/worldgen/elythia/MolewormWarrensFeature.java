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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MolewormWarrensFeature extends Feature<NoneFeatureConfiguration> {
    public MolewormWarrensFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = findDeepCavity(level, context.origin(), random);
        if (origin == null) {
            return false;
        }

        carveWarrenNetwork(level, random, origin, true);
        return true;
    }

    public static void carveWarrenNetwork(WorldGenLevel level, RandomSource random, BlockPos origin, boolean carveOriginChamber) {
        Vec3 center = Vec3.atCenterOf(origin);
        if (carveOriginChamber) {
            carvePocket(level, random, center, 5.8F + random.nextFloat() * 1.8F, 3.6F, 5.8F + random.nextFloat() * 1.8F, true, true);
        }

        int majorBranches = 7 + random.nextInt(4);
        for (int i = 0; i < majorBranches; i++) {
            float angle = (Mth.TWO_PI * i / majorBranches) + random.nextFloat() * 0.8F;
            Vec3 direction = new Vec3(Mth.cos(angle), -0.12D - random.nextDouble() * 0.28D, Mth.sin(angle)).normalize();
            carveMainTunnel(level, random, center, direction, 0);
        }
    }

    @Nullable
    private static BlockPos findDeepCavity(WorldGenLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < 36; attempt++) {
            BlockPos candidate = origin.offset(random.nextInt(29) - 14, random.nextInt(49) - 24, random.nextInt(29) - 14);
            if (candidate.getY() > 52 || candidate.getY() < level.getMinBuildHeight() + 16) {
                continue;
            }

            for (int dy = -10; dy <= 10; dy++) {
                BlockPos airPos = candidate.above(dy);
                if (isUsableCavity(level, airPos)) {
                    return airPos;
                }
            }
        }
        return null;
    }

    private static boolean carveMainTunnel(WorldGenLevel level, RandomSource random, Vec3 start, Vec3 direction, int branchDepth) {
        Vec3 cursor = start;
        Vec3 travel = normalizeDirection(direction);
        boolean carved = false;
        int minY = level.getMinBuildHeight() + 10;
        int segments = Math.max(5, 12 + random.nextInt(6) - branchDepth * 2);

        for (int segment = 0; segment < segments; segment++) {
            if (cursor.y <= minY) {
                break;
            }

            float progress = segment / (float) Math.max(1, segments - 1);
            float startRadius = branchDepth == 0
                    ? 2.6F + progress * 1.1F + random.nextFloat() * 0.45F
                    : 1.9F + progress * 0.7F + random.nextFloat() * 0.35F;
            float endRadius = startRadius + (random.nextFloat() - 0.5F) * 0.45F;
            double segmentLength = 6.5D + random.nextDouble() * 5.5D;

            Vec3 wobble = new Vec3(
                    random.nextDouble() * 0.7D - 0.35D,
                    -0.06D - random.nextDouble() * 0.22D,
                    random.nextDouble() * 0.7D - 0.35D
            );
            travel = normalizeDirection(travel.add(wobble));

            Vec3 next = cursor.add(travel.scale(segmentLength));
            if (next.y > cursor.y - 0.8D) {
                next = new Vec3(next.x, cursor.y - 0.8D - random.nextDouble() * 2.4D, next.z);
            }
            if (!canAccess(level, BlockPos.containing(next.x, next.y, next.z))) {
                break;
            }

            boolean chamber = segment == 0 || segment == segments - 1 || random.nextFloat() < (branchDepth == 0 ? 0.34F : 0.22F);
            carveTube(level, random, cursor, next, startRadius, endRadius, chamber);
            carved = true;

            if (branchDepth < 2 && random.nextFloat() < (branchDepth == 0 ? 0.45F : 0.28F)) {
                Vec3 branchDirection = rotateHorizontal(travel, random.nextBoolean() ? 0.95F : -0.95F)
                        .add(0.0D, -0.04D - random.nextDouble() * 0.18D, 0.0D);
                carveMainTunnel(level, random, next, branchDirection, branchDepth + 1);
            }

            if (branchDepth == 0 && random.nextFloat() < 0.28F) {
                next = carveDropShaft(level, random, next);
                travel = normalizeDirection(new Vec3(travel.x, -0.65D - random.nextDouble() * 0.16D, travel.z));
            }

            cursor = next;
        }

        return carved;
    }

    private static Vec3 carveDropShaft(WorldGenLevel level, RandomSource random, Vec3 start) {
        Vec3 cursor = start;
        int steps = 4 + random.nextInt(5);
        for (int i = 0; i < steps; i++) {
            cursor = cursor.add(
                    random.nextDouble() * 0.45D - 0.225D,
                    -(1.8D + random.nextDouble() * 1.8D),
                    random.nextDouble() * 0.45D - 0.225D
            );
            carvePocket(level, random, cursor, 2.1F + random.nextFloat() * 0.5F, 1.9F, 2.1F + random.nextFloat() * 0.5F, false, true);
        }
        carvePocket(level, random, cursor, 4.0F + random.nextFloat() * 1.2F, 2.8F, 4.0F + random.nextFloat() * 1.2F, true, true);
        return cursor;
    }

    private static void carveTube(WorldGenLevel level, RandomSource random, Vec3 start, Vec3 end, float startRadius, float endRadius, boolean chamber) {
        Vec3 delta = end.subtract(start);
        double length = Math.max(delta.length(), 1.0D);
        int steps = Math.max(5, Mth.ceil(length * 2.25D));

        for (int step = 0; step <= steps; step++) {
            float progress = step / (float) steps;
            Vec3 center = start.add(delta.scale(progress));
            float radius = Mth.lerp(progress, startRadius, endRadius);
            float radiusX = radius + (random.nextFloat() - 0.5F) * 0.3F;
            float radiusY = Math.max(1.85F, radius * (chamber ? 0.95F : 0.8F) + (random.nextFloat() - 0.5F) * 0.25F);
            float radiusZ = radius + (random.nextFloat() - 0.5F) * 0.3F;
            carvePocket(level, random, center, radiusX, radiusY, radiusZ, chamber && step % 3 == 0, true);
        }

        if (chamber) {
            Vec3 chamberCenter = start.add(end).scale(0.5D);
            carvePocket(level, random, chamberCenter, endRadius + 1.6F, endRadius * 0.9F + 1.2F, endRadius + 1.6F, true, true);
        }
    }

    private static void carvePocket(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ, boolean mudHeavy, boolean decorate) {
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
                    double noise = (hashNoise(x, y, z) - 0.5D) * 0.18D;
                    double distance = dx2 + dz2 + dy * dy + noise;
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (distance <= 1.0D) {
                        if (canCarve(state)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                        continue;
                    }

                    if (distance <= 1.24D && canReplaceShell(state)) {
                        level.setBlock(pos, selectShellState(random, center.y, y), 2);
                    }
                }
            }
        }

        if (decorate) {
            decorateFloor(level, random, center, radiusX, radiusZ, mudHeavy);
            decorateCeiling(level, random, center, radiusX, radiusY, radiusZ);
        }
    }

    private static void decorateFloor(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusZ, boolean mudHeavy) {
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
                    BlockState floorState = level.getBlockState(floorPos);
                    if (!canReplaceShell(floorState)) {
                        continue;
                    }

                    float mudChance = mudHeavy ? 0.22F : 0.08F;
                    level.setBlock(floorPos, random.nextFloat() < mudChance ? Blocks.MUD.defaultBlockState() : selectFloorState(random), 2);
                    tryPlaceDeadBush(level, random, pos);
                    break;
                }
            }
        }
    }

    private static void decorateCeiling(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ) {
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

                    level.setBlock(pos, random.nextFloat() < 0.74F ? rootedDirtVariant(random) : coarseDirtVariant(random), 2);
                    if (random.nextFloat() < 0.38F && hangingRoots.canSurvive(level, airPos)) {
                        level.setBlock(airPos, hangingRoots, 2);
                    }
                    break;
                }
            }
        }

        decorateWalls(level, random, center, radiusX, radiusY, radiusZ);
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
                    if (random.nextFloat() >= 0.035F || !canAccess(level, x, y, z)) {
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

    private static boolean isUsableCavity(WorldGenLevel level, BlockPos pos) {
        return canAccess(level, pos)
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && isSoilOrStone(level.getBlockState(pos.below()))
                && !level.isEmptyBlock(pos.above(3));
    }

    private static Vec3 normalizeDirection(Vec3 direction) {
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        }
        horizontal = horizontal.normalize();
        double y = Mth.clamp(direction.y, -0.92D, -0.05D);
        return new Vec3(horizontal.x, y, horizontal.z).normalize();
    }

    private static Vec3 rotateHorizontal(Vec3 vector, float radians) {
        double cos = Mth.cos(radians);
        double sin = Mth.sin(radians);
        return new Vec3(vector.x * cos - vector.z * sin, vector.y, vector.x * sin + vector.z * cos);
    }

    private static BlockState selectShellState(RandomSource random, double centerY, int y) {
        return y > centerY + 0.25D
                ? (random.nextFloat() < 0.72F ? rootedDirtVariant(random) : coarseDirtVariant(random))
                : (random.nextFloat() < 0.58F ? coarseDirtVariant(random) : rootedDirtVariant(random));
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
                || state.is(Blocks.TUFF)
                || state.is(Blocks.MUD);
    }

    private static double hashNoise(int x, int y, int z) {
        long hash = x * 3129871L ^ z * 116129781L ^ y;
        hash = hash * hash * 42317861L + hash * 11L;
        return ((hash >> 16) & 1023L) / 1023.0D;
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

    private static void tryPlaceDeadBush(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (random.nextFloat() >= 0.045F) {
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
