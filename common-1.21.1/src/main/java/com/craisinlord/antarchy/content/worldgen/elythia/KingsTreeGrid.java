package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class KingsTreeGrid {
    public static final int SPACING = 5_000;
    public static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    public static final ResourceKey<Structure> STRUCTURE_KEY = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kings_tree")
    );

    private KingsTreeGrid() {
    }

    public static int nearestCoordinate(int coordinate, boolean permitZero) {
        long gridIndex = Math.round(coordinate / (double) SPACING);
        if (!permitZero && gridIndex == 0) {
            gridIndex = coordinate < 0 ? -1 : 1;
        }
        return Math.toIntExact(gridIndex * SPACING);
    }

    public static BlockPos nearestTreeCenter(BlockPos origin) {
        return new BlockPos(
                nearestCoordinate(origin.getX(), false),
                origin.getY(),
                nearestCoordinate(origin.getZ(), false)
        );
    }
}
