package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Places the authored King's Tree as independently generated chunk slices on a fixed world grid. */
public final class KingsTreeGridFeature extends Feature<NoneFeatureConfiguration> {
    private static final Map<ServerLevel, Set<Long>> CLAIMED_TREE_KINGS = new WeakHashMap<>();
    private static final ConcurrentLinkedQueue<PendingKingSpawn> PENDING_KING_SPAWNS = new ConcurrentLinkedQueue<>();
    private static final int TREE_MIN_X = -191;
    private static final int TREE_MAX_X = 192;
    private static final int TREE_MIN_Z = -227;
    private static final int TREE_MAX_Z = 227;
    private static final int TREE_BOTTOM_Y = -22;
    private static final int TREE_TOP_Y = 335;

    public KingsTreeGridFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(context.origin());
        int centerX = KingsTreeGrid.nearestCoordinate(chunk.getMiddleBlockX(), true);
        int centerZ = KingsTreeGrid.nearestCoordinate(chunk.getMiddleBlockZ(), true);
        if (centerX == 0 || centerZ == 0) {
            return false;
        }

        int relativeMinX = chunk.getMinBlockX() - centerX;
        int relativeMaxX = chunk.getMaxBlockX() - centerX;
        int relativeMinZ = chunk.getMinBlockZ() - centerZ;
        int relativeMaxZ = chunk.getMaxBlockZ() - centerZ;
        if (relativeMaxX < TREE_MIN_X || relativeMinX > TREE_MAX_X
                || relativeMaxZ < TREE_MIN_Z || relativeMinZ > TREE_MAX_Z) {
            return false;
        }
        int minTileX = Math.floorDiv(relativeMinX, 16);
        int maxTileX = Math.floorDiv(relativeMaxX, 16);
        int minTileZ = Math.floorDiv(relativeMinZ, 16);
        int maxTileZ = Math.floorDiv(relativeMaxZ, 16);

        int surfaceY = context.chunkGenerator().getBaseHeight(
                centerX,
                centerZ,
                Heightmap.Types.WORLD_SURFACE_WG,
                level,
                level.getLevel().getChunkSource().randomState()
        );
        int minimumBaseY = level.getMinBuildHeight() - TREE_BOTTOM_Y;
        int maximumBaseY = level.getMaxBuildHeight() - 1 - TREE_TOP_Y;
        int baseY = Math.clamp(surfaceY, minimumBaseY, maximumBaseY);
        BlockPos treeBottomOrigin = new BlockPos(0, baseY + TREE_BOTTOM_Y, 0);

        BoundingBox chunkBounds = new BoundingBox(
                chunk.getMinBlockX(),
                level.getMinBuildHeight(),
                chunk.getMinBlockZ(),
                chunk.getMaxBlockX(),
                level.getMaxBuildHeight() - 1,
                chunk.getMaxBlockZ()
        );
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setBoundingBox(chunkBounds)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        StructureTemplateManager templates = level.getLevel().getStructureManager();
        RandomSource random = context.random();
        boolean placed = false;
        boolean centralTilePlaced = false;

        for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
            for (int tileZ = minTileZ; tileZ <= maxTileZ; tileZ++) {
                Optional<StructureTemplate> template = templates.get(tileLocation(tileX, tileZ));
                if (template.isEmpty()) {
                    continue;
                }
                BlockPos tileOrigin = treeBottomOrigin.offset(centerX + tileX * 16, 0, centerZ + tileZ * 16);
                boolean tilePlaced = template.get().placeInWorld(level, tileOrigin, tileOrigin, settings, random, 2);
                placed |= tilePlaced;
                centralTilePlaced |= tileX == 0 && tileZ == 0 && tilePlaced;
            }
        }
        if (centralTilePlaced) {
            queueTreeKing(level.getLevel(), centerX, centerZ, baseY);
        }
        return placed;
    }

    private static void queueTreeKing(ServerLevel serverLevel, int centerX, int centerZ, int baseY) {
        long treeKey = ((long) centerX << 32) ^ (centerZ & 0xffffffffL);
        synchronized (CLAIMED_TREE_KINGS) {
            if (!CLAIMED_TREE_KINGS.computeIfAbsent(serverLevel, ignored -> new java.util.HashSet<>()).add(treeKey)) {
                return;
            }
        }
        PENDING_KING_SPAWNS.add(new PendingKingSpawn(serverLevel, centerX, centerZ, baseY));
    }

    public static void tickPendingSpawns(ServerLevel level) {
        int pendingCount = PENDING_KING_SPAWNS.size();
        for (int index = 0; index < pendingCount; index++) {
            PendingKingSpawn pending = PENDING_KING_SPAWNS.poll();
            if (pending == null) {
                return;
            }
            if (pending.level() != level || !spawnTreeKing(pending)) {
                PENDING_KING_SPAWNS.add(pending);
            }
        }
    }

    private static boolean spawnTreeKing(PendingKingSpawn pending) {
        ServerLevel serverLevel = pending.level();
        int centerX = pending.centerX();
        int centerZ = pending.centerZ();
        int baseY = pending.baseY();
        Vec3 treeCenter = new Vec3(centerX + 0.5D, 0.0D, centerZ + 0.5D);
        AABB searchBox = new AABB(
                centerX - 512.0D, serverLevel.getMinBuildHeight(), centerZ - 512.0D,
                centerX + 512.0D, serverLevel.getMaxBuildHeight(), centerZ + 512.0D);
        List<KingEntity> treeKings = serverLevel.getEntitiesOfClass(KingEntity.class, searchBox,
                king -> king.isAlive() && king.isTreePatrolFor(treeCenter));
        if (!treeKings.isEmpty()) {
            for (int index = 1; index < treeKings.size(); index++) {
                treeKings.get(index).discard();
            }
            return true;
        }
        KingEntity king = AntarchyObjects.KING.get().create(serverLevel);
        if (king == null) {
            return true;
        }
        double minimumY = baseY + 24.0D;
        double maximumY = baseY + 304.0D;
        double spawnY = minimumY + (maximumY - minimumY) * 0.65D;
        Vec3 spawn = findSafeKingSpawn(serverLevel, king, centerX, centerZ, spawnY);
        if (spawn == null) {
            return false;
        }
        king.moveTo(spawn.x, spawn.y, spawn.z,
                (float) (Math.atan2(centerZ + 0.5D - spawn.z, centerX + 0.5D - spawn.x)
                        * 180.0D / Math.PI) - 90.0F,
                0.0F);
        king.setTreePatrolHome(treeCenter, minimumY, maximumY, 0.0D);
        king.setPersistenceRequired();
        serverLevel.addFreshEntity(king);
        return true;
    }

    private static Vec3 findSafeKingSpawn(ServerLevel level, KingEntity king,
                                          int centerX, int centerZ, double spawnY) {
        double[] radii = {96.0D, 80.0D, 64.0D};
        double[] heightOffsets = {0.0D, 18.0D, -12.0D, 32.0D};
        for (double radius : radii) {
            for (int angleIndex = 0; angleIndex < 16; angleIndex++) {
                double angle = angleIndex * Math.PI * 2.0D / 16.0D;
                double x = centerX + 0.5D + Math.cos(angle) * radius;
                double z = centerZ + 0.5D + Math.sin(angle) * radius;
                for (double heightOffset : heightOffsets) {
                    Vec3 candidate = new Vec3(x, spawnY + heightOffset, z);
                    BlockPos blockPos = BlockPos.containing(candidate);
                    if (!areCollisionChunksLoaded(level, blockPos.offset(-8, -8, -8), blockPos.offset(8, 16, 8))) {
                        continue;
                    }
                    king.moveTo(candidate.x, candidate.y, candidate.z, 0.0F, 0.0F);
                    if (level.noCollision(king, king.getBoundingBox())) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static boolean areCollisionChunksLoaded(ServerLevel level, BlockPos min, BlockPos max) {
        var chunkSource = level.getChunkSource();
        for (int chunkX = min.getX() >> 4; chunkX <= max.getX() >> 4; chunkX++) {
            for (int chunkZ = min.getZ() >> 4; chunkZ <= max.getZ() >> 4; chunkZ++) {
                if (chunkSource.getChunkNow(chunkX, chunkZ) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private record PendingKingSpawn(ServerLevel level, int centerX, int centerZ, int baseY) {
    }

    private static ResourceLocation tileLocation(int tileX, int tileZ) {
        return ResourceLocation.fromNamespaceAndPath(
                Antarchy.MODID,
                "kings_tree_tiles/" + coordinateName(tileX) + "_" + coordinateName(tileZ)
        );
    }

    private static String coordinateName(int coordinate) {
        return coordinate < 0 ? "m" + -coordinate : "p" + coordinate;
    }
}
