package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record BoulderPaletteConfiguration(ResourceLocation block, ResourceLocation slab, boolean capped) implements FeatureConfiguration {
    private static final ResourceLocation DEFAULT_BLOCK = new ResourceLocation("antarchy", "rockite");
    private static final ResourceLocation DEFAULT_SLAB = new ResourceLocation("antarchy", "rockite_slab");
    public static final Codec<BoulderPaletteConfiguration> CODEC = RecordCodecBuilder.<BoulderPaletteConfiguration>create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("block", DEFAULT_BLOCK).forGetter(BoulderPaletteConfiguration::block),
            ResourceLocation.CODEC.optionalFieldOf("slab", DEFAULT_SLAB).forGetter(BoulderPaletteConfiguration::slab),
            Codec.BOOL.optionalFieldOf("capped", true).forGetter(BoulderPaletteConfiguration::capped)
    ).apply(instance, BoulderPaletteConfiguration::new)).flatXmap(BoulderPaletteConfiguration::validated, BoulderPaletteConfiguration::validated);

    private static ResourceLocation expectedSlab(ResourceLocation block) {
        return block.equals(new ResourceLocation("minecraft", "tuff")) ? new ResourceLocation("minecraft", "andesite_slab") : block.withSuffix("_slab");
    }

    private static DataResult<BoulderPaletteConfiguration> validated(BoulderPaletteConfiguration palette) {
        return !palette.capped || palette.slab.equals(expectedSlab(palette.block))
                ? DataResult.success(palette)
                : DataResult.error(() -> "Boulder slab must match its raw block: " + palette.block + " requires " + expectedSlab(palette.block));
    }

    public Palette resolve() {
        Block base = BuiltInRegistries.BLOCK.getOptional(block).orElseThrow(() -> new IllegalStateException("Missing boulder block: " + block));
        if (!capped) {
            return new Palette(base.defaultBlockState(), null);
        }
        Block cap = BuiltInRegistries.BLOCK.getOptional(slab).orElseThrow(() -> new IllegalStateException("Missing boulder slab: " + slab));
        if (!(cap instanceof SlabBlock)) {
            throw new IllegalStateException("Boulder cap is not a slab: " + slab);
        }
        return new Palette(base.defaultBlockState(), cap.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
    }

    public record Palette(BlockState block, BlockState slab) {
        public boolean matches(BlockState state) {
            return state.is(block.getBlock());
        }
    }
}
