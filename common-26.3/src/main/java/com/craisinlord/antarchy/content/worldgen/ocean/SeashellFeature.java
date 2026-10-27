package com.craisinlord.antarchy.content.worldgen.ocean;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.SeashellBlock;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public record SeashellFeature() implements Feature {
    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "chests/seashell")
    );

    public static final MapCodec<SeashellFeature> CODEC = MapCodec.unit(SeashellFeature::new);

    @Override
    public MapCodec<SeashellFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean placedAny = false;

        for (int i = 0; i < 8; i++) {
            int x = origin.getX() + random.nextInt(17) - 8;
            int z = origin.getZ() + random.nextInt(17) - 8;
            int floorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            BlockPos shellPos = new BlockPos(x, floorY, z);

            if (!level.getFluidState(shellPos).isSource()) {
                continue;
            }

            BlockState state = AntarchyObjects.SEASHELL.get().defaultBlockState()
                    .setValue(SeashellBlock.FACING, net.minecraft.core.Direction.Plane.HORIZONTAL.getRandomDirection(random))
                    .setValue(SeashellBlock.WATERLOGGED, true)
                    .setValue(SeashellBlock.POWERED, false);
            if (!state.canSurvive(level, shellPos)) {
                continue;
            }

            level.setBlock(shellPos, state, 2);
            if (level.getBlockEntity(shellPos) instanceof SeashellBlockEntity seashell) {
                seashell.setLootTable(LOOT_TABLE, random.nextLong());
                seashell.setChanged();
            }
            placedAny = true;
        }

        return placedAny;
    }
}
