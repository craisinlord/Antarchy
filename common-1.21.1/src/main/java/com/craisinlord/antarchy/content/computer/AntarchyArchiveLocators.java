package com.craisinlord.antarchy.content.computer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.command.QueenLocateCommand;
import com.craisinlord.antarchy.content.worldgen.elythia.KingsTreeGrid;
import com.craisinlord.antos.api.archive.ArchiveLocatorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;

/** Archive coordinate lookups for royals that live on grids rather than in vanilla structures. */
public final class AntarchyArchiveLocators {
    private AntarchyArchiveLocators() {
    }

    public static void register() {
        ArchiveLocatorRegistry.register(id("queen"), (level, origin) -> {
            QueenLocateCommand.NearestSite nearest = QueenLocateCommand.findNearestSite(level, origin.getX(), origin.getZ());
            return nearest == null ? null : nearest.marker().getSpawnPos();
        });
        ArchiveLocatorRegistry.register(id("kings_tree"), (level, origin) -> {
            BlockPos center = KingsTreeGrid.nearestTreeCenter(origin);
            int surfaceY = level.getChunkSource().getGenerator().getBaseHeight(center.getX(), center.getZ(),
                    Heightmap.Types.WORLD_SURFACE_WG, level, level.getChunkSource().randomState());
            return new BlockPos(center.getX(), surfaceY, center.getZ());
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
    }
}
