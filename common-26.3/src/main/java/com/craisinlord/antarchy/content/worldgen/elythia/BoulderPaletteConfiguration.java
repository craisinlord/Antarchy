package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public record BoulderPaletteConfiguration(Identifier block, Identifier slab, boolean capped) {
    private static final Identifier DEFAULT_BLOCK = Identifier.fromNamespaceAndPath("antarchy", "rockite");
    private static final Identifier DEFAULT_SLAB = Identifier.fromNamespaceAndPath("antarchy", "rockite_slab");
    public static final MapCodec<BoulderPaletteConfiguration> CODEC = RecordCodecBuilder.<BoulderPaletteConfiguration>mapCodec(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("block", DEFAULT_BLOCK).forGetter(BoulderPaletteConfiguration::block),
            Identifier.CODEC.optionalFieldOf("slab", DEFAULT_SLAB).forGetter(BoulderPaletteConfiguration::slab),
            Codec.BOOL.optionalFieldOf("capped", true).forGetter(BoulderPaletteConfiguration::capped)
    ).apply(instance, BoulderPaletteConfiguration::new)).validate(palette -> !palette.capped || palette.slab.equals(palette.block.withSuffix("_slab"))
            ? DataResult.success(palette)
            : DataResult.error(() -> "Boulder slab must match its raw block: " + palette.block + " requires " + palette.block.withSuffix("_slab")));

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
