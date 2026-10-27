package com.craisinlord.antarchy.fabric.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeferredRegister<T> {
    private static final ThreadLocal<ResourceKey<?>> CURRENT_KEY = new ThreadLocal<>();

    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String modId) {
            super(Registries.BLOCK, modId);
        }

        public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> supplier) {
            return this.registerTyped(name, supplier, DeferredBlock::new);
        }
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(String modId) {
            super(Registries.ITEM, modId);
        }

        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
            return this.registerTyped(name, supplier, DeferredItem::new);
        }

        public DeferredItem<Item> registerSimpleItem(String name) {
            return this.register(name, () -> new Item(itemProperties()));
        }

        public DeferredItem<Item> registerSimpleItem(String name, Item.Properties properties) {
            return this.register(name, () -> new Item(withItemId(properties)));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredBlock<? extends Block> block) {
            return this.register(block.getId().getPath(), () -> new BlockItem(block.get(), blockItemProperties()));
        }

        public DeferredItem<BlockItem> registerSimpleBlockItem(DeferredBlock<? extends Block> block, Item.Properties properties) {
            return this.register(block.getId().getPath(), () -> new BlockItem(block.get(), withItemId(properties).useBlockDescriptionPrefix()));
        }
    }

    @FunctionalInterface
    protected interface HolderFactory<R, T extends R, H extends DeferredHolder<R, T>> {
        H create(ResourceKey<?> registryKey, Identifier id);
    }

    private record PendingEntry<T>(Identifier id, Supplier<? extends T> supplier, DeferredHolder<T, ? extends T> holder) {
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

    public static Item.Properties itemProperties() {
        return withItemId(new Item.Properties());
    }

    public static Item.Properties blockItemProperties() {
        return itemProperties().useBlockDescriptionPrefix();
    }

    public static Item.Properties withItemId(Item.Properties properties) {
        ResourceKey<Item> key = currentKey(Registries.ITEM);
        return key == null ? properties : properties.setId(key);
    }

    public static BlockBehaviour.Properties blockProperties() {
        return withBlockId(BlockBehaviour.Properties.of());
    }

    public static BlockBehaviour.Properties copyBlockProperties(BlockBehaviour block) {
        return withBlockId(BlockBehaviour.Properties.ofFullCopy(block));
    }

    @SuppressWarnings("deprecation")
    public static BlockBehaviour.Properties legacyCopyBlockProperties(BlockBehaviour block) {
        return withBlockId(BlockBehaviour.Properties.ofLegacyCopy(block));
    }

    public static BlockBehaviour.Properties withBlockId(BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = currentKey(Registries.BLOCK);
        return key == null ? properties : properties.setId(key);
    }

    public static <V> ResourceKey<V> currentKey(ResourceKey<? extends Registry<V>> registry) {
        ResourceKey<?> key = CURRENT_KEY.get();
        if (key == null || !key.isFor(registry)) {
            return null;
        }
        return key.cast(registry).orElse(null);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        return this.registerTyped(name, supplier, DeferredHolder::new);
    }

    protected <I extends T, H extends DeferredHolder<T, I>> H registerTyped(String name, Supplier<? extends I> supplier, HolderFactory<T, I, H> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(this.modId, name);
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
            ResourceKey<T> key = ResourceKey.create(this.registryKey, entry.id());
            ResourceKey<?> previous = CURRENT_KEY.get();
            CURRENT_KEY.set(key);
            T created;
            try {
                created = entry.supplier().get();
            } finally {
                if (previous == null) {
                    CURRENT_KEY.remove();
                } else {
                    CURRENT_KEY.set(previous);
                }
            }
            T value = Registry.register(registry, key, created);
            bind(entry.holder(), value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> void bind(DeferredHolder<T, ? extends T> holder, T value) {
        ((DeferredHolder) holder).bind(value);
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> resolveRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        if (registry == null) {
            throw new IllegalStateException("Could not resolve registry: " + registryKey.identifier());
        }
        return (Registry<T>) registry;
    }
}
