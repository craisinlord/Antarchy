package com.craisinlord.antarchy.fabric.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
    DeferredBlock(ResourceKey<?> registryKey, Identifier id) {
        super(registryKey, id);
    }
}
