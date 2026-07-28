package com.craisinlord.antarchy.content.worldgen.ocean;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.SeashellBlock;
import com.craisinlord.antarchy.content.block.entity.SeashellBlockEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class SeashellFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "chests/seashell")
    );

    public SeashellFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
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
