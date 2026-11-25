package com.craisinlord.antarchy.compat.alexscaves;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;

public final class AlexsCavesSpawnGuard {
    private static final float MAX_SAFE_PROBABILITY = 0.5F;
    private static final int STEP = 8;
    private static final boolean ACTIVE = BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.fromNamespaceAndPath(AlexsCavesIntegration.MOD_ID, "raycat"))
            && Arrays.stream(MobCategory.values()).noneMatch(category -> category.getSerializedName().equals("alexscaves:cave_creature"));

    private AlexsCavesSpawnGuard() {
    }

    public static boolean neverEnds(ServerLevelAccessor level, ChunkPos chunk) {
        if (!ACTIVE) {
            return false;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y += STEP) {
            if (level.getBiome(pos.setY(y)).value().getMobSettings().getCreatureProbability() > MAX_SAFE_PROBABILITY) {
                return true;
            }
        }
        return false;
    }
}
