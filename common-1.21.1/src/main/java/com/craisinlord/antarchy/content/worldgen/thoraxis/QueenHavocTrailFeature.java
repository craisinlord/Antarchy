package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.QueenTrailSpawnMarkerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class QueenHavocTrailFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation DREAM_SAND = id("dream_sand");
    private static final ResourceLocation DREAM_SANDSTONE = id("dream_sandstone");
    private static final ResourceLocation CUT_DREAM_SANDSTONE = id("cut_dream_sandstone");
    private static final ResourceLocation DREAM_FIRE = id("dream_fire_ceiling");
    private static final ResourceLocation SPAWN_MARKER = id("queen_trail_spawn_marker");
    private static final int CHAMBER_RADIUS = 30;
    private static final int CHAMBER_DEPTH = 20;

    public QueenHavocTrailFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(context.origin());
        int cellX = Math.floorDiv(chunk.getMiddleBlockX(), QueenTrailGrid.SPACING);
        int cellZ = Math.floorDiv(chunk.getMiddleBlockZ(), QueenTrailGrid.SPACING);
        boolean placed = false;
        for (int x = cellX - 1; x <= cellX + 1; x++) {
            for (int z = cellZ - 1; z <= cellZ + 1; z++) {
                QueenTrailGrid.Site site = QueenTrailGrid.site(level.getSeed(), x, z);
                placed |= placeSiteSlice(level, chunk, site);
            }
        }
        return placed;
    }

    private static boolean placeSiteSlice(WorldGenLevel level, ChunkPos chunk, QueenTrailGrid.Site site) {
        int reach = site.length() + CHAMBER_RADIUS;
        if (site.terminalX() + reach < chunk.getMinBlockX() || site.terminalX() - reach > chunk.getMaxBlockX()
                || site.terminalZ() + reach < chunk.getMinBlockZ() || site.terminalZ() - reach > chunk.getMaxBlockZ()) {
            return false;
        }
        boolean placed = placeChamber(level, chunk, site);
        for (int index = 0; index < site.footprints(); index++) {
            double progress = index / (double) Math.max(1, site.footprints() - 1);
            double distance = 18.0D + (site.footprints() - 1 - index) * site.stride();
            double side = (index & 1) == 0 ? -2.5D : 2.5D;
            double centerX = site.terminalX() - site.directionX() * distance - site.directionZ() * side;
            double centerZ = site.terminalZ() - site.directionZ() * distance + site.directionX() * side;
            placed |= placeFootprint(level, chunk, site, index, progress, centerX, centerZ);
            if (index % 4 == 2) {
                double craterSide = ((site.id() >>> (index & 31)) & 1L) == 0L ? -5.0D : 5.0D;
                placed |= placeCrater(level, chunk, site, index, progress,
                        centerX - site.directionZ() * craterSide,
                        centerZ + site.directionX() * craterSide);
            }
        }
        if (contains(chunk, site.terminalX(), site.terminalZ())) {
            placed |= placeMarker(level, site);
        }
        return placed;
    }

    private static boolean placeFootprint(WorldGenLevel level, ChunkPos chunk, QueenTrailGrid.Site site, int index,
                                          double progress, double centerX, double centerZ) {
        boolean placed = false;
        int minX = Math.max(chunk.getMinBlockX(), (int) Math.floor(centerX - 4.0D));
        int maxX = Math.min(chunk.getMaxBlockX(), (int) Math.ceil(centerX + 4.0D));
        int minZ = Math.max(chunk.getMinBlockZ(), (int) Math.floor(centerZ - 4.0D));
        int maxZ = Math.min(chunk.getMaxBlockZ(), (int) Math.ceil(centerZ + 4.0D));
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x + 0.5D - centerX;
                double dz = z + 0.5D - centerZ;
                double forward = dx * site.directionX() + dz * site.directionZ();
                double lateral = -dx * site.directionZ() + dz * site.directionX();
                boolean pad = square((forward + 0.5D) / 2.5D) + square(lateral / 2.0D) <= 1.0D;
                boolean toes = toe(forward, lateral, -1.5D) || toe(forward, lateral, 0.0D) || toe(forward, lateral, 1.5D);
                if (!pad && !toes) {
                    continue;
                }
                int depth = progress > 0.78D ? 3 : progress > 0.35D ? 2 : 1;
                double fireChance = 0.12D + progress * 0.48D;
                placed |= carvePrintColumn(level, x, z, depth, unit(site.id(), x, z, index) < fireChance,
                        index % 3 == 0 ? CUT_DREAM_SANDSTONE : DREAM_SANDSTONE);
            }
        }
        return placed;
    }

    private static boolean placeCrater(WorldGenLevel level, ChunkPos chunk, QueenTrailGrid.Site site, int index,
                                       double progress, double centerX, double centerZ) {
        int radius = progress > 0.65D ? 4 : 3;
        boolean placed = false;
        for (int x = Math.max(chunk.getMinBlockX(), (int) centerX - radius); x <= Math.min(chunk.getMaxBlockX(), (int) centerX + radius); x++) {
            for (int z = Math.max(chunk.getMinBlockZ(), (int) centerZ - radius); z <= Math.min(chunk.getMaxBlockZ(), (int) centerZ + radius); z++) {
                double distance = square((x + 0.5D - centerX) / radius) + square((z + 0.5D - centerZ) / radius);
                if (distance <= 1.0D && unit(site.id() ^ 0x6A09E667F3BCC909L, x, z, index) > distance * 0.35D) {
                    placed |= carvePrintColumn(level, x, z, distance < 0.32D ? 2 : 1,
                            unit(site.id(), z, x, index + 31) < 0.35D + progress * 0.35D, DREAM_SANDSTONE);
                }
            }
        }
        return placed;
    }

    private static boolean placeChamber(WorldGenLevel level, ChunkPos chunk, QueenTrailGrid.Site site) {
        int minX = Math.max(chunk.getMinBlockX(), site.terminalX() - CHAMBER_RADIUS);
        int maxX = Math.min(chunk.getMaxBlockX(), site.terminalX() + CHAMBER_RADIUS);
        int minZ = Math.max(chunk.getMinBlockZ(), site.terminalZ() - CHAMBER_RADIUS);
        int maxZ = Math.min(chunk.getMaxBlockZ(), site.terminalZ() + CHAMBER_RADIUS);
        boolean placed = false;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - site.terminalX();
                int dz = z - site.terminalZ();
                int distanceSquared = dx * dx + dz * dz;
                if (distanceSquared > CHAMBER_RADIUS * CHAMBER_RADIUS) {
                    continue;
                }
                BlockPos ceiling = findCeiling(level, x, z);
                if (ceiling == null) {
                    continue;
                }
                for (int depth = 1; depth <= CHAMBER_DEPTH; depth++) {
                    BlockPos air = ceiling.below(depth);
                    if (!level.getBlockState(air).isAir()) {
                        level.setBlock(air, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                double edge = Math.sqrt(distanceSquared) / CHAMBER_RADIUS;
                if (edge > 0.58D && unit(site.id() ^ 0xBB67AE8584CAA73BL, x, z, 0) < 0.18D + edge * 0.22D) {
                    placeCeilingFire(level, ceiling);
                }
                placed = true;
            }
        }
        return placed;
    }

    private static boolean placeMarker(WorldGenLevel level, QueenTrailGrid.Site site) {
        BlockPos ceiling = findCeiling(level, site.terminalX(), site.terminalZ());
        if (ceiling == null) {
            return false;
        }
        Block markerBlock = block(SPAWN_MARKER, Blocks.BARRIER);
        BlockPos markerPos = ceiling.above(4);
        level.setBlock(markerPos, markerBlock.defaultBlockState(), 2);
        BlockEntity blockEntity = level.getBlockEntity(markerPos);
        if (!(blockEntity instanceof QueenTrailSpawnMarkerBlockEntity marker)) {
            return false;
        }
        BlockPos spawnPos = new BlockPos(site.terminalX(), ceiling.getY() - 17, site.terminalZ());
        BlockPos homePos = new BlockPos(site.terminalX(), ceiling.getY() - 2, site.terminalZ());
        float yaw = (float) Math.toDegrees(Math.atan2(site.directionZ(), site.directionX())) - 90.0F;
        marker.configure(site.id(), spawnPos, homePos, yaw);
        return true;
    }

    private static boolean carvePrintColumn(WorldGenLevel level, int x, int z, int depth, boolean fire, ResourceLocation backingId) {
        BlockPos ceiling = findCeiling(level, x, z);
        if (ceiling == null) {
            return false;
        }
        BlockPos backing = ceiling.above(depth);
        level.setBlock(backing, block(backingId, Blocks.SANDSTONE).defaultBlockState(), 2);
        for (int offset = 0; offset < depth; offset++) {
            level.setBlock(ceiling.above(offset), Blocks.AIR.defaultBlockState(), 2);
        }
        if (fire) {
            placeCeilingFire(level, backing);
        }
        return true;
    }

    private static void placeCeilingFire(WorldGenLevel level, BlockPos support) {
        BlockPos firePos = support.below();
        if (level.getBlockState(firePos).isAir()) {
            level.setBlock(firePos, block(DREAM_FIRE, Blocks.SOUL_FIRE).defaultBlockState(), 2);
        }
    }

    private static BlockPos findCeiling(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, -8, z);
        int minimum = Math.max(level.getMinBuildHeight() + 20, -240);
        for (int y = -8; y >= minimum; y--) {
            cursor.setY(y);
            if (isDreamSurface(level.getBlockState(cursor)) && level.getBlockState(cursor.below()).isAir()) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean isDreamSurface(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return DREAM_SAND.equals(id) || DREAM_SANDSTONE.equals(id) || CUT_DREAM_SANDSTONE.equals(id);
    }

    private static boolean contains(ChunkPos chunk, int x, int z) {
        return x >= chunk.getMinBlockX() && x <= chunk.getMaxBlockX() && z >= chunk.getMinBlockZ() && z <= chunk.getMaxBlockZ();
    }

    private static boolean toe(double forward, double lateral, double toeCenter) {
        return square((forward - 2.2D) / 1.25D) + square((lateral - toeCenter) / 0.8D) <= 1.0D;
    }

    private static double square(double value) {
        return value * value;
    }

    private static double unit(long seed, int x, int z, int salt) {
        long value = seed ^ (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL ^ (long) salt * 0x165667B19E3779F9L;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        return (value >>> 11) * 0x1.0p-53;
    }

    private static Block block(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
    }
}
