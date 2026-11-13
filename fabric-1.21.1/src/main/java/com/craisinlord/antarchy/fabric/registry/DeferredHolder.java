package com.craisinlord.antarchy.fabric.registry;

import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final ResourceKey<?> registryKey;
    private final ResourceLocation id;
    private T value;
    private volatile Holder<R> registeredHolder;

    DeferredHolder(ResourceKey<?> registryKey, ResourceLocation id) {
        this.registryKey = registryKey;
        this.id = id;
    }

    void bind(T value) {
        this.value = value;
    }

    Holder<R> registeredHolder() {
        return this.registeredHolder;
    }

    void cacheRegisteredHolder(Holder<R> holder) {
        this.registeredHolder = holder;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    public ResourceKey<T> getKey() {
        return (ResourceKey<T>) ResourceKey.create((ResourceKey) this.registryKey, this.id);
    }

    @Override
    public T get() {
        if (this.value == null) {
            throw new IllegalStateException("Registry object not yet bound: " + this.id);
        }
        return this.value;
    }
}
