package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ThrowbackNerdPoleFeature implements Feature {
    public static final MapCodec<ThrowbackNerdPoleFeature> CODEC = MapCodec.unit(ThrowbackNerdPoleFeature::new);

    @Override
    public MapCodec<ThrowbackNerdPoleFeature> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Optional<ThrowbackZones.WildZone> found = ThrowbackZones.wildZoneInChunk(level.getSeed(), ChunkPos.containing(origin));
        if (found.isEmpty()) {
            return false;
        }
        ThrowbackZones.WildZone zone = found.get();
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, zone.x(), zone.z());
        BlockPos base = new BlockPos(zone.x(), surface, zone.z());
        BlockPos ground = base.below();
        if (!ThrowbackZones.isWildZoneBiome(level, base)
                || !level.getChunk(base).getAllReferences().isEmpty()
                || !level.getFluidState(base).isEmpty()
                || !level.getFluidState(ground).isEmpty()
                || !level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
            return false;
        }
        int placed = 0;
        for (int i = 0; i < zone.poleHeight(); i++) {
            BlockPos pos = base.above(i);
            if (!level.getBlockState(pos).canBeReplaced()) {
                break;
            }
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
            placed++;
        }
        if (placed == 0) {
            return false;
        }
        BlockPos top = base.above(placed);
        if (level.getBlockState(top).canBeReplaced()) {
            level.setBlock(top, Blocks.TORCH.defaultBlockState(), 2);
        }
        return true;
    }
}
