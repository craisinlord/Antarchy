package com.craisinlord.antarchy.fabric.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class DeferredRegister<T> {
    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String modId) {
            super(net.minecraft.core.registries.Registries.BLOCK, modId);
        }

        public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> supplier) {
            return this.registerTyped(name, supplier, DeferredBlock::new);
        }
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(String modId) {
            super(net.minecraft.core.registries.Registries.ITEM, modId);
        }

        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
            return this.registerTyped(name, supplier, DeferredItem::new);
        }

        public DeferredItem<Item> registerSimpleItem(String name) {
            return this.registerSimpleItem(name, new Item.Properties());
        }

        public DeferredItem<Item> registerSimpleItem(String name, Item.Properties properties) {
            return this.register(name, () -> new Item(properties));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredBlock<? extends Block> block) {
            return this.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    @FunctionalInterface
    protected interface HolderFactory<R, T extends R, H extends DeferredHolder<R, T>> {
        H create(ResourceKey<?> registryKey, ResourceLocation id);
    }

    private record PendingEntry<T>(ResourceLocation id, Supplier<? extends T> supplier, DeferredHolder<T, ? extends T> holder) {
    }

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String modId;
    private final List<PendingEntry<T>> entries = new ArrayList<>();

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public static Blocks createBlocks(String modId) {
        return new Blocks(modId);
    }

    public static Items createItems(String modId) {
        return new Items(modId);
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        return new DeferredRegister<>(registryKey, modId);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        return this.registerTyped(name, supplier, DeferredHolder::new);
    }

    protected <I extends T, H extends DeferredHolder<T, I>> H registerTyped(String name, Supplier<? extends I> supplier, HolderFactory<T, I, H> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modId, name);
        H holder = factory.create(this.registryKey, id);
        this.entries.add(new PendingEntry<>(id, supplier, holder));
        return holder;
    }

    public List<DeferredHolder<T, ? extends T>> getEntries() {
        List<DeferredHolder<T, ? extends T>> holders = new ArrayList<>(this.entries.size());
        for (PendingEntry<T> entry : this.entries) {
            holders.add(entry.holder());
        }
        return Collections.unmodifiableList(holders);
    }

    public void register() {
        Registry<T> registry = resolveRegistry(this.registryKey);
        for (PendingEntry<T> entry : this.entries) {
            T value = Registry.register(registry, entry.id(), entry.supplier().get());
            bind(entry.holder(), value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> void bind(DeferredHolder<T, ? extends T> holder, T value) {
        ((DeferredHolder) holder).bind(value);
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> resolveRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (registry == null) {
            throw new IllegalStateException("Could not resolve registry: " + registryKey.location());
        }
        return (Registry<T>) registry;
    }
}
