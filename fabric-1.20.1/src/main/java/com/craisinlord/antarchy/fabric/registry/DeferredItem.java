package com.craisinlord.antarchy.fabric.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
    DeferredItem(ResourceKey<?> registryKey, ResourceLocation id) {
        super(registryKey, id);
    }
}
