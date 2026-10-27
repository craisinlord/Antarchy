package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

public class UmbralMossBlock extends Block implements BonemealableBlock {
    private static final ResourceKey<Feature> UMBRAL_MOSS_PATCH_BONEMEAL = ResourceKey.create(
            Registries.FEATURE,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "umbral_moss_patch_bonemeal")
    );

    public UmbralMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        Holder.Reference<Feature> feature = level.registryAccess()
                .lookupOrThrow(Registries.FEATURE)
                .getOrThrow(UMBRAL_MOSS_PATCH_BONEMEAL);
        feature.value().place(level, level.getChunkSource().getGenerator(), random, pos);
    }
}
