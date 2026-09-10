package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Places the authored King's Tree as independently generated chunk slices on a fixed world grid. */
public final class KingsTreeGridFeature extends Feature<NoneFeatureConfiguration> {
    private static final int TREE_MIN_X = -191, TREE_MAX_X = 192;
    private static final int TREE_MIN_Z = -227, TREE_MAX_Z = 227;
    private static final int TREE_BOTTOM_Y = -22, TREE_TOP_Y = 335;

    public KingsTreeGridFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(context.origin());
        int centerX = KingsTreeGrid.nearestCoordinate(chunk.getMiddleBlockX(), true);
        int centerZ = KingsTreeGrid.nearestCoordinate(chunk.getMiddleBlockZ(), true);
        if (centerX == 0 || centerZ == 0) return false;
        int relativeMinX = chunk.getMinBlockX() - centerX, relativeMaxX = chunk.getMaxBlockX() - centerX;
        int relativeMinZ = chunk.getMinBlockZ() - centerZ, relativeMaxZ = chunk.getMaxBlockZ() - centerZ;
        if (relativeMaxX < TREE_MIN_X || relativeMinX > TREE_MAX_X
                || relativeMaxZ < TREE_MIN_Z || relativeMinZ > TREE_MAX_Z) return false;
        int minTileX = Math.floorDiv(relativeMinX, 16), maxTileX = Math.floorDiv(relativeMaxX, 16);
        int minTileZ = Math.floorDiv(relativeMinZ, 16), maxTileZ = Math.floorDiv(relativeMaxZ, 16);
        int surfaceY = context.chunkGenerator().getBaseHeight(centerX, centerZ,
                Heightmap.Types.WORLD_SURFACE_WG, level, level.getLevel().getChunkSource().randomState());
        int minimumBaseY = level.getMinBuildHeight() - TREE_BOTTOM_Y;
        int maximumBaseY = level.getMaxBuildHeight() - 1 - TREE_TOP_Y;
        int baseY = Mth.clamp(surfaceY, minimumBaseY, maximumBaseY);
        BlockPos treeBottomOrigin = new BlockPos(0, baseY + TREE_BOTTOM_Y, 0);
        BoundingBox chunkBounds = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
        StructurePlaceSettings settings = new StructurePlaceSettings().setBoundingBox(chunkBounds)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        StructureTemplateManager templates = level.getLevel().getStructureManager();
        RandomSource random = context.random();
        boolean placed = false;
        for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
            for (int tileZ = minTileZ; tileZ <= maxTileZ; tileZ++) {
                Optional<StructureTemplate> template = templates.get(tileLocation(tileX, tileZ));
                if (template.isEmpty()) continue;
                BlockPos tileOrigin = treeBottomOrigin.offset(centerX + tileX * 16, 0, centerZ + tileZ * 16);
                placed |= template.get().placeInWorld(level, tileOrigin, tileOrigin, settings, random, 2);
            }
        }
        return placed;
    }

    private static ResourceLocation tileLocation(int tileX, int tileZ) {
        return new ResourceLocation(Antarchy.MODID, "kings_tree_tiles/" + coordinateName(tileX) + "_" + coordinateName(tileZ));
    }

    private static String coordinateName(int coordinate) { return coordinate < 0 ? "m" + -coordinate : "p" + coordinate; }
}
