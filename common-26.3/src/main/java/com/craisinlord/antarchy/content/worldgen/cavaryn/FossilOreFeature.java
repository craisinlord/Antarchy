package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;

public record FossilOreFeature(List<BlockReplacement> targets, int size, float discardChanceOnAirExposure) implements Feature {
    public static final MapCodec<FossilOreFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockReplacement.CODEC.listOf().fieldOf("targets").forGetter(FossilOreFeature::targets),
            Codec.intRange(1, 64).fieldOf("size").forGetter(FossilOreFeature::size),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("discard_chance_on_air_exposure", 0.0F).forGetter(FossilOreFeature::discardChanceOnAirExposure)
    ).apply(instance, FossilOreFeature::new));

    @Override
    public MapCodec<FossilOreFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!AntarchySettings.fossilsEnabled()) {
            return false;
        }
        return new OreFeature(targets, size, discardChanceOnAirExposure).place(level, generator, random, origin);
    }
}