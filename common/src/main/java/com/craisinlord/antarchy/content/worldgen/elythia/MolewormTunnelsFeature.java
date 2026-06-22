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

public class MolewormTunnelsFeature extends Feature<NoneFeatureConfiguration> {
    public MolewormTunnelsFeature(Codec<NoneFeatureConfiguration> codec) {
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
        int tooLowOrHigh = 0;
        int invalidSurface = 0;
        int successfulEntrances = 0;
        int extraMainBranches = 0;
        int extraSideBranches = 0;

        this.disturbSurface(level, random, chunkStartX, chunkStartZ);
        this.raiseSurfaceBurrowField(level, random, chunkStartX, chunkStartZ);

        int entrances = 3 + random.nextInt(3);
        for (int i = 0; i < entrances; i++) {
            int x = chunkStartX + 1 + random.nextInt(14);
            int z = chunkStartZ + 1 + random.nextInt(14);
            if (!canAccessColumn(level, x, z)) {
                continue;
            }
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
            if (surfaceY <= level.getMinBuildHeight() + 20 || surfaceY >= level.getMaxBuildHeight() - 20) {
                tooLowOrHigh++;
                continue;
            }

            BlockPos surfacePos = new BlockPos(x, surfaceY, z);
            if (!isValidSurface(level, surfacePos)) {
                invalidSurface++;
                continue;
            }

            TunnelStart start = random.nextFloat() < 0.68F
                    ? this.carveSlopedEntrance(level, random, surfacePos)
                    : this.carveCollapsedShaft(level, random, surfacePos);
            if (start == null) {
                continue;
            }
            if (random.nextFloat() < 0.18F) {
                this.placeSurfaceDripstone(level, random, surfacePos, 1.5F + random.nextFloat());
            }

            this.carveMainTunnel(level, random, start.position, start.direction, surfaceY, 0);
            successfulEntrances++;
            if (random.nextFloat() < 0.50F) {
                this.carveMainTunnel(level, random, start.position.add(random.nextDouble() - 0.5D, -0.6D, random.nextDouble() - 0.5D),
                        rotateHorizontal(start.direction, random.nextBoolean() ? 0.6F : -0.6F), surfaceY, 0);
                extraMainBranches++;
            }
            if (random.nextFloat() < 0.35F) {
                this.carveMainTunnel(level, random, start.position.add(random.nextDouble() - 0.5D, -1.0D, random.nextDouble() - 0.5D),
                        rotateHorizontal(start.direction, random.nextBoolean() ? 1.05F : -1.05F), surfaceY, 1);
                extraSideBranches++;
            }
            placedAny = true;
        }
        return placedAny;
    }

    private void disturbSurface(WorldGenLevel level, RandomSource random, int chunkStartX, int chunkStartZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = chunkStartX + localX;
                int z = chunkStartZ + localZ;
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }
                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (topY <= level.getMinBuildHeight() + 2) {
                    continue;
                }

                pos.set(x, topY, z);
                if (!isValidSurface(level, pos)) {
                    continue;
                }

                level.setBlock(pos, disturbedSurfaceState(random), 2);
                for (int depth = 1; depth <= 3 + random.nextInt(2); depth++) {
                    pos.set(x, topY - depth, z);
                    BlockState state = level.getBlockState(pos);
                    if (!isSoilOrStone(state)) {
                        break;
                    }
                    level.setBlock(pos, disturbedSubsurfaceState(random), 2);
                }
            }
        }
    }

    private void raiseSurfaceBurrowField(WorldGenLevel level, RandomSource random, int chunkStartX, int chunkStartZ) {
        int moundCount = 4 + random.nextInt(5);
        for (int i = 0; i < moundCount; i++) {
            int x = chunkStartX + random.nextInt(16);
            int z = chunkStartZ + random.nextInt(16);
            if (!canAccessColumn(level, x, z)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
            if (y <= level.getMinBuildHeight() + 2) {
                continue;
            }

            BlockPos center = new BlockPos(x, y, z);
            if (!isValidSurface(level, center)) {
                continue;
            }

            this.raiseBroadMound(level, random, center, 2.1F + random.nextFloat() * 2.2F, 1 + random.nextInt(3));
        }
    }

    private TunnelStart carveSlopedEntrance(WorldGenLevel level, RandomSource random, BlockPos surfacePos) {
        Vec3 direction = horizontalDirection(random);
        Vec3 cursor = Vec3.atCenterOf(surfacePos);
        float startRadius = 3.5F + random.nextFloat() * 1.9F;
        int steps = 7 + random.nextInt(4);

        this.raiseSpoilMound(level, random, surfacePos, startRadius + 1.5F, 2 + random.nextInt(3), direction);
        this.carvePocket(level, random, cursor.add(0.0D, 3.0D, 0.0D), startRadius + 1.8F, 3.6F, startRadius + 1.8F, 0, true, true);
        this.carvePocket(level, random, cursor.add(direction.x * 1.8D, 2.4D, direction.z * 1.8D), startRadius + 0.6F, 2.8F, startRadius + 0.6F, 0, false, false);

        for (int step = 0; step < steps; step++) {
            float progress = (step + 1) / (float) steps;
            float radius = Mth.lerp(progress, startRadius, 2.2F + random.nextFloat() * 0.9F);
            cursor = cursor.add(
                    direction.x * (2.2D + random.nextDouble() * 1.0D),
                    -(1.0D + random.nextDouble() * 0.7D),
                    direction.z * (2.2D + random.nextDouble() * 1.0D)
            );
            this.carvePocket(level, random, cursor, radius, 1.8F, radius, 4 + step * 2, false, step >= steps - 2);
        }

        this.removeFloatingBlocks(level, surfacePos, Mth.ceil(startRadius + 3.0F), surfacePos.getY());

        Vec3 tunnelDir = new Vec3(
                direction.x + random.nextDouble() * 0.3D - 0.15D,
                -0.45D - random.nextDouble() * 0.2D,
                direction.z + random.nextDouble() * 0.3D - 0.15D
        ).normalize();
        return new TunnelStart(cursor.add(0.0D, -0.5D, 0.0D), tunnelDir);
    }

    private TunnelStart carveCollapsedShaft(WorldGenLevel level, RandomSource random, BlockPos surfacePos) {
        Vec3 center = Vec3.atCenterOf(surfacePos).add(0.0D, 0.5D, 0.0D);
        float rimRadius = 4.2F + random.nextFloat() * 2.0F;
        this.raiseSpoilRing(level, random, surfacePos, rimRadius + 1.0F, 2 + random.nextInt(2));
        this.carvePocket(level, random, center.add(0.0D, 3.0D, 0.0D), rimRadius + 1.3F, 3.8F, rimRadius + 1.3F, 0, true, true);

        int shaftDepth = 8 + random.nextInt(7);
        for (int step = 0; step < shaftDepth; step++) {
            float radius = step < 3 ? 2.2F : 1.7F + random.nextFloat() * 0.5F;
            Vec3 shaftCenter = center.add(
                    random.nextDouble() * 0.5D - 0.25D,
                    -step * 1.45D,
                    random.nextDouble() * 0.5D - 0.25D
            );
            this.carvePocket(level, random, shaftCenter, radius, 1.5F, radius, 5 + step * 2, false, step >= shaftDepth - 3);
        }

        this.removeFloatingBlocks(level, surfacePos, Mth.ceil(rimRadius + 3.0F), surfacePos.getY());

        Vec3 branchDir = horizontalDirection(random).add(0.0D, -0.55D, 0.0D).normalize();
        return new TunnelStart(center.add(0.0D, -shaftDepth * 1.45D, 0.0D), branchDir);
    }

    private boolean carveMainTunnel(WorldGenLevel level, RandomSource random, Vec3 start, Vec3 direction, int surfaceY, int branchDepth) {
        Vec3 cursor = start;
        Vec3 travel = normalizeTunnelDirection(direction);
        boolean carved = false;
        int minY = level.getMinBuildHeight() + 10;
        int targetDepth = 30 + random.nextInt(27) + branchDepth * 8;
        int segments = 8 + random.nextInt(5) - branchDepth;
        segments = Math.max(segments, 3);

        for (int segment = 0; segment < segments; segment++) {
            int currentDepth = surfaceY - Mth.floor(cursor.y);
            if (currentDepth >= targetDepth || cursor.y <= minY) {
                break;
            }

            float segmentProgress = segment / (float) Math.max(1, segments - 1);
            float startRadius = branchDepth == 0
                    ? 2.0F + segmentProgress * 1.15F + random.nextFloat() * 0.35F
                    : 1.55F + segmentProgress * 0.7F + random.nextFloat() * 0.25F;
            float endRadius = startRadius + (random.nextFloat() - 0.5F) * 0.35F;
            double segmentLength = 4.75D + random.nextDouble() * 3.15D;

            Vec3 wobble = new Vec3(
                    random.nextDouble() * 0.55D - 0.275D,
                    -0.08D - random.nextDouble() * 0.14D,
                    random.nextDouble() * 0.55D - 0.275D
            );
            travel = normalizeTunnelDirection(travel.add(wobble));

            Vec3 next = cursor.add(travel.scale(segmentLength));
            if (next.y > cursor.y - 1.5D) {
                next = new Vec3(next.x, cursor.y - 1.5D - random.nextDouble() * 2.0D, next.z);
            }
            if (!canAccess(level, BlockPos.containing(next.x, next.y, next.z))) {
                break;
            }

            boolean chamber = currentDepth > 14 && (segment == 2 || segment == 4 || random.nextFloat() < 0.22F);
            this.carveTube(level, random, cursor, next, startRadius, endRadius, surfaceY, chamber);
            carved = true;

            if (branchDepth < 2 && currentDepth > 6 && random.nextFloat() < (branchDepth == 0 ? 0.40F : 0.20F)) {
                Vec3 branchDirection = rotateHorizontal(travel, random.nextBoolean() ? 0.95F : -0.95F).add(0.0D, -0.08D - random.nextDouble() * 0.18D, 0.0D);
                this.carveMainTunnel(level, random, next, branchDirection, surfaceY, branchDepth + 1);
            }

            if (branchDepth == 0 && currentDepth > 18 && currentDepth < targetDepth - 8 && random.nextFloat() < 0.12F) {
                next = this.carveDropShaft(level, random, next, surfaceY);
                travel = normalizeTunnelDirection(new Vec3(travel.x, -0.65D - random.nextDouble() * 0.12D, travel.z));
            }

            cursor = next;
        }

        if (branchDepth == 0 && carved) {
            this.carvePocket(
                    level,
                    random,
                    cursor,
                    3.5F + random.nextFloat() * 1.8F,
                    2.6F + random.nextFloat() * 0.8F,
                    3.5F + random.nextFloat() * 1.8F,
                    surfaceY - Mth.floor(cursor.y),
                    true,
                    true
            );
        }

        return carved;
    }

    private Vec3 carveDropShaft(WorldGenLevel level, RandomSource random, Vec3 start, int surfaceY) {
        Vec3 cursor = start;
        int steps = 4 + random.nextInt(4);
        for (int i = 0; i < steps; i++) {
            cursor = cursor.add(
                    random.nextDouble() * 0.4D - 0.2D,
                    -(1.9D + random.nextDouble() * 1.4D),
                    random.nextDouble() * 0.4D - 0.2D
            );
            this.carvePocket(level, random, cursor, 1.8F + random.nextFloat() * 0.5F, 1.7F, 1.8F + random.nextFloat() * 0.5F, surfaceY - Mth.floor(cursor.y), false, true);
        }
        this.carvePocket(level, random, cursor, 3.2F + random.nextFloat(), 2.5F, 3.2F + random.nextFloat(), surfaceY - Mth.floor(cursor.y), true, true);
        return cursor;
    }

    private void raiseBroadMound(WorldGenLevel level, RandomSource random, BlockPos surfacePos, float radius, int height) {
        int minX = Mth.floor(surfacePos.getX() - radius);
        int maxX = Mth.floor(surfacePos.getX() + radius);
        int minZ = Mth.floor(surfacePos.getZ() - radius);
        int maxZ = Mth.floor(surfacePos.getZ() + radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }
                double dx = x + 0.5D - (surfacePos.getX() + 0.5D);
                double dz = z + 0.5D - (surfacePos.getZ() + 0.5D);
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) {
                    continue;
                }

                double shape = 1.0D - distance / radius;
                int localHeight = (int) Math.floor(shape * shape * (height + 1) + random.nextDouble() * 1.1D);
                localHeight = Math.min(localHeight, 2);
                if (localHeight <= 0) {
                    continue;
                }

                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                pos.set(x, topY, z);
                if (!isSoilOrStone(level.getBlockState(pos))) {
                    continue;
                }

                level.setBlock(pos, disturbedSurfaceState(random), 2);
                for (int depth = 1; depth <= 2; depth++) {
                    pos.set(x, topY - depth, z);
                    if (isSoilOrStone(level.getBlockState(pos))) {
                        level.setBlock(pos, disturbedSubsurfaceState(random), 2);
                    }
                }

                if (localHeight <= 1 && shape < 0.45D) {
                    continue;
                }

                for (int y = 1; y <= localHeight; y++) {
                    pos.set(x, topY + y, z);
                    if (!level.isEmptyBlock(pos) || level.isEmptyBlock(pos.below())) {
                        continue;
                    }
                    level.setBlock(pos, disturbedSurfaceState(random), 2);
                }
            }
        }
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
                if (dx * dx + dz * dz > radius * radius || random.nextFloat() < 0.42F) {
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
                    if (!level.isEmptyBlock(pos) || !pointedDripstone.canSurvive(level, pos)) {
                        break;
                    }
                    level.setBlock(pos, pointedDripstone, 2);
                }
            }
        }
    }

    private void raiseSpoilMound(WorldGenLevel level, RandomSource random, BlockPos surfacePos, float radius, int height, Vec3 favoredDirection) {
        int minX = Mth.floor(surfacePos.getX() - radius);
        int maxX = Mth.floor(surfacePos.getX() + radius);
        int minZ = Mth.floor(surfacePos.getZ() - radius);
        int maxZ = Mth.floor(surfacePos.getZ() + radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }
                double dx = x + 0.5D - (surfacePos.getX() + 0.5D);
                double dz = z + 0.5D - (surfacePos.getZ() + 0.5D);
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) {
                    continue;
                }

                double directionalBias = favoredDirection.x * dx + favoredDirection.z * dz;
                int localHeight = (int) Math.floor((1.0D - distance / radius) * height + directionalBias * 0.18D + random.nextDouble() * 0.75D);
                localHeight = Math.min(localHeight, 2);
                if (localHeight <= 0) {
                    continue;
                }

                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                pos.set(x, topY, z);
                if (!isSoilOrStone(level.getBlockState(pos))) {
                    continue;
                }

                level.setBlock(pos, disturbedSurfaceState(random), 2);
                for (int y = 1; y <= localHeight; y++) {
                    pos.set(x, topY + y, z);
                    if (!level.isEmptyBlock(pos)) {
                        continue;
                    }
                    level.setBlock(pos, disturbedSurfaceState(random), 2);
                }
            }
        }
    }

    private void raiseSpoilRing(WorldGenLevel level, RandomSource random, BlockPos surfacePos, float radius, int height) {
        int minX = Mth.floor(surfacePos.getX() - radius);
        int maxX = Mth.floor(surfacePos.getX() + radius);
        int minZ = Mth.floor(surfacePos.getZ() - radius);
        int maxZ = Mth.floor(surfacePos.getZ() + radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!canAccessColumn(level, x, z)) {
                    continue;
                }
                double dx = x + 0.5D - (surfacePos.getX() + 0.5D);
                double dz = z + 0.5D - (surfacePos.getZ() + 0.5D);
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius || distance < radius * 0.42D) {
                    continue;
                }

                double rimStrength = 1.0D - Math.abs(distance - radius * 0.72D) / (radius * 0.32D);
                int localHeight = (int) Math.floor(rimStrength * height + random.nextDouble() * 0.85D);
                localHeight = Math.min(localHeight, 2);
                if (localHeight <= 0) {
                    continue;
                }

                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                pos.set(x, topY, z);
                if (!isSoilOrStone(level.getBlockState(pos))) {
                    continue;
                }

                level.setBlock(pos, disturbedSurfaceState(random), 2);
                for (int y = 1; y <= localHeight; y++) {
                    pos.set(x, topY + y, z);
                    if (!level.isEmptyBlock(pos)) {
                        continue;
                    }
                    level.setBlock(pos, disturbedSurfaceState(random), 2);
                }
            }
        }
    }

    private void carveTube(WorldGenLevel level, RandomSource random, Vec3 start, Vec3 end, float startRadius, float endRadius, int surfaceY, boolean chamber) {
        Vec3 delta = end.subtract(start);
        double length = Math.max(delta.length(), 1.0D);
        int steps = Math.max(4, Mth.ceil(length * 2.2D));

        for (int step = 0; step <= steps; step++) {
            float progress = step / (float) steps;
            Vec3 center = start.add(delta.scale(progress));
            float radius = Mth.lerp(progress, startRadius, endRadius);
            float radiusX = radius + (random.nextFloat() - 0.5F) * 0.25F;
            float radiusY = Math.max(1.45F, radius * (chamber ? 0.95F : 0.82F) + (random.nextFloat() - 0.5F) * 0.2F);
            float radiusZ = radius + (random.nextFloat() - 0.5F) * 0.25F;
            this.carvePocket(level, random, center, radiusX, radiusY, radiusZ, surfaceY - Mth.floor(center.y), chamber && step % 3 == 0, true);
        }

        if (chamber) {
            Vec3 chamberCenter = start.add(end).scale(0.5D);
            this.carvePocket(
                    level,
                    random,
                    chamberCenter,
                    endRadius + 1.2F + random.nextFloat(),
                    endRadius * 0.85F + 1.1F,
                    endRadius + 1.2F + random.nextFloat(),
                    surfaceY - Mth.floor(chamberCenter.y),
                    true,
                    true
            );
        }
    }

    private void carvePocket(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ, int depthFromSurface, boolean chamber, boolean decorateCeiling) {
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

                    if (distance > 1.24D || !canReplaceShell(state)) {
                        continue;
                    }

                    level.setBlock(pos, selectShellState(random, depthFromSurface, center.y, y, chamber), 2);
                }
            }
        }

        this.decorateFloor(level, random, center, radiusX, radiusZ, depthFromSurface, chamber);
        if (decorateCeiling) {
            this.decorateCeiling(level, random, center, radiusX, radiusY, radiusZ, depthFromSurface, chamber);
        }
    }

    private void decorateFloor(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusZ, int depthFromSurface, boolean chamber) {
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

                    level.setBlock(floorPos, selectFloorState(random, depthFromSurface, chamber), 2);
                    break;
                }
            }
        }
    }

    private void decorateCeiling(WorldGenLevel level, RandomSource random, Vec3 center, float radiusX, float radiusY, float radiusZ, int depthFromSurface, boolean chamber) {
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
                    if (!level.isEmptyBlock(airPos)) {
                        continue;
                    }

                    if (!canReplaceShell(level.getBlockState(pos))) {
                        break;
                    }

                    level.setBlock(pos, random.nextFloat() < 0.76F ? rootedDirtVariant(random) : coarseDirtVariant(random), 2);
                    if (random.nextFloat() < hangingRootChance(depthFromSurface, chamber) && hangingRoots.canSurvive(level, airPos)) {
                        level.setBlock(airPos, hangingRoots, 2);
                    }
                    break;
                }
            }
        }
    }

    private static BlockState selectShellState(RandomSource random, int depthFromSurface, double centerY, int y, boolean chamber) {
        boolean ceiling = y > centerY + 0.25D;
        if (ceiling) {
            return random.nextFloat() < 0.72F ? rootedDirtVariant(random) : coarseDirtVariant(random);
        }
        return random.nextFloat() < 0.58F ? coarseDirtVariant(random) : rootedDirtVariant(random);
    }

    private static BlockState selectFloorState(RandomSource random, int depthFromSurface, boolean chamber) {
        return random.nextFloat() < 0.56F ? coarseDirtVariant(random) : rootedDirtVariant(random);
    }

    private static BlockState disturbedSurfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.44F) {
            return coarseDirtVariant(random);
        }
        if (roll < 0.79F) {
            return rootedDirtVariant(random);
        }
        return Blocks.PODZOL.defaultBlockState();
    }

    private static BlockState disturbedSubsurfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.48F) {
            return rootedDirtVariant(random);
        }
        if (roll < 0.84F) {
            return coarseDirtVariant(random);
        }
        return Blocks.PODZOL.defaultBlockState();
    }

    private static float hangingRootChance(int depthFromSurface, boolean chamber) {
        float chance = depthFromSurface > 18 ? 0.34F : 0.18F;
        return chamber ? chance + 0.14F : chance;
    }

    private static Vec3 horizontalDirection(RandomSource random) {
        float angle = random.nextFloat() * Mth.TWO_PI;
        return new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
    }

    private static Vec3 rotateHorizontal(Vec3 vector, float radians) {
        double cos = Mth.cos(radians);
        double sin = Mth.sin(radians);
        return new Vec3(vector.x * cos - vector.z * sin, vector.y, vector.x * sin + vector.z * cos);
    }

    private static Vec3 normalizeTunnelDirection(Vec3 direction) {
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        }
        horizontal = horizontal.normalize();
        double y = Mth.clamp(direction.y, -0.88D, -0.08D);
        return new Vec3(horizontal.x, y, horizontal.z).normalize();
    }

    private static boolean isValidSurface(WorldGenLevel level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState state = level.getBlockState(pos);
        return level.canSeeSky(abovePos)
                && level.getFluidState(abovePos).isEmpty()
                && isSoilOrStone(state)
                && !state.is(Blocks.MUD);
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
                || state.is(Blocks.GRAVEL);
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

    private static class TunnelStart {
        private final Vec3 position;
        private final Vec3 direction;

        private TunnelStart(Vec3 position, Vec3 direction) {
            this.position = position;
            this.direction = direction;
        }
    }
}
