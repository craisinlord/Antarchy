package com.craisinlord.antarchy.compat.item;

import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class AntarchyItemComponent<T> {
    private final String key;
    private final Function<CompoundTag, T> reader;
    private final Function<T, Object> writer;

    public AntarchyItemComponent(String key, Function<CompoundTag, T> reader, Function<T, Object> writer) {
        this.key = key;
        this.reader = reader;
        this.writer = writer;
    }

    public boolean has(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(this.key);
    }

    public T get(ItemStack stack) {
        if (!this.has(stack)) {
            return null;
        }
        return this.reader.apply(stack.getTag());
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        T value = this.get(stack);
        return value != null ? value : fallback;
    }

    public void set(ItemStack stack, T value) {
        if (value == null) {
            this.remove(stack);
            return;
        }
        Object encoded = this.writer.apply(value);
        CompoundTag tag = stack.getOrCreateTag();
        if (encoded instanceof Integer integer) tag.putInt(this.key, integer);
        else if (encoded instanceof String string) tag.putString(this.key, string);
        else if (encoded instanceof Boolean bool) tag.putBoolean(this.key, bool);
    }

    public void remove(ItemStack stack) {
        if (stack.hasTag()) stack.getTag().remove(this.key);
    }

    public static <T> AntarchyItemComponent<T> string(String key, Function<String, T> decoder, Function<T, String> encoder) {
        return new AntarchyItemComponent<>(key, tag -> decoder.apply(tag.getString(key)), encoder::apply);
    }

    public static AntarchyItemComponent<Integer> integer(String key) {
        return new AntarchyItemComponent<>(key, tag -> tag.getInt(key), value -> value);
    }

    public static AntarchyItemComponent<net.minecraft.util.Unit> marker(String key) {
        return new AntarchyItemComponent<>(key, tag -> net.minecraft.util.Unit.INSTANCE, value -> true);
    }
}
