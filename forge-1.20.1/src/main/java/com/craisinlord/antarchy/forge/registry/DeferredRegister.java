package com.craisinlord.antarchy.forge.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.function.Supplier;

public class DeferredRegister<T> {
    private final net.minecraftforge.registries.DeferredRegister<T> delegate;

    protected DeferredRegister(net.minecraftforge.registries.DeferredRegister<T> delegate) {
        this.delegate = delegate;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        return new DeferredRegister<>(net.minecraftforge.registries.DeferredRegister.create(registryKey, modId));
    }

    public static Items createItems(String modId) {
        return new Items(net.minecraftforge.registries.DeferredRegister.create(Registries.ITEM, modId));
    }

    public static Blocks createBlocks(String modId) {
        return new Blocks(net.minecraftforge.registries.DeferredRegister.create(Registries.BLOCK, modId));
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        return this.delegate.register(name, supplier);
    }

    public void register(IEventBus modEventBus) {
        this.delegate.register(modEventBus);
    }

    public Collection<RegistryObject<T>> getEntries() {
        return this.delegate.getEntries();
    }

    public TagKey<T> createTagKey(String name) {
        return this.delegate.createTagKey(name);
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(net.minecraftforge.registries.DeferredRegister<Item> delegate) {
            super(delegate);
        }

        public RegistryObject<Item> registerSimpleItem(String name) {
            return this.registerSimpleItem(name, new Item.Properties());
        }

        public RegistryObject<Item> registerSimpleItem(String name, Item.Properties properties) {
            return this.register(name, () -> new Item(properties));
        }

        public RegistryObject<BlockItem> registerSimpleBlockItem(RegistryObject<? extends Block> block) {
            return this.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }

        public RegistryObject<BlockItem> registerSimpleBlockItem(RegistryObject<? extends Block> block, Item.Properties properties) {
            return this.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
        }
    }

    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(net.minecraftforge.registries.DeferredRegister<Block> delegate) {
            super(delegate);
        }
    }
}
