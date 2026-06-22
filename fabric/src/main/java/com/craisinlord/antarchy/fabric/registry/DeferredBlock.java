package com.craisinlord.antarchy.fabric.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public final class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
    DeferredBlock(ResourceKey<?> registryKey, ResourceLocation id) {
        super(registryKey, id);
    }
}
