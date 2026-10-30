package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ThrowbackNerdPoleFeature extends Feature<NoneFeatureConfiguration> {
    public ThrowbackNerdPoleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        Optional<ThrowbackZones.WildZone> found = ThrowbackZones.wildZoneInChunk(level.getSeed(), ChunkPos.containing(context.origin()));
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
