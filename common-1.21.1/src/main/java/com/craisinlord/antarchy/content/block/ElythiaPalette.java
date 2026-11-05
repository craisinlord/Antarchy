package com.craisinlord.antarchy.content.block;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class ElythiaPalette {
    public static final Supplier<Block> ROCKITE = () -> block("rockite");
    public static final Supplier<Block> ZEBRA_STONE = () -> block("zebra_stone");
    public static final Supplier<Block> ARKROSE = () -> block("arkrose");
    public static final Supplier<Block> TIDESTONE = () -> block("tidestone");
    public static final Supplier<Block> CONGLOMERATE = () -> block("conglomerate");
    public static final Supplier<Block> TRUFFLED_LOAM = () -> block("truffled_loam");
    public static final Supplier<Block> PRIMORDIAL_GRASS = () -> block("primordial_grass");

    private ElythiaPalette() {
    }

    public static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("antarchy", path));
    }
}
