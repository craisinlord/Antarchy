package com.craisinlord.antarchy.fabric.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
    DeferredItem(ResourceKey<?> registryKey, Identifier id) {
        super(registryKey, id);
    }
}
