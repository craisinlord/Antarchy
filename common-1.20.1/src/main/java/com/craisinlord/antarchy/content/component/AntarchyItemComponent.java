package com.craisinlord.antarchy.content.component;

import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class AntarchyItemComponent<T> {
    private final String key;
    private final Function<T, Tag> writer;
    private final Function<Tag, T> reader;

    private AntarchyItemComponent(String key, Function<T, Tag> writer, Function<Tag, T> reader) {
        this.key = key;
        this.writer = writer;
        this.reader = reader;
    }

    public static AntarchyItemComponent<Integer> ofInt(String key) {
        return new AntarchyItemComponent<>(key, net.minecraft.nbt.IntTag::valueOf, tag -> ((net.minecraft.nbt.IntTag) tag).getAsInt());
    }

    public static AntarchyItemComponent<String> ofString(String key) {
        return new AntarchyItemComponent<>(key, net.minecraft.nbt.StringTag::valueOf, Tag::getAsString);
    }

    public static <T> AntarchyItemComponent<T> of(String key, Function<T, Tag> writer, Function<Tag, T> reader) {
        return new AntarchyItemComponent<>(key, writer, reader);
    }

    public static AntarchyItemComponent<net.minecraft.util.Unit> ofMarker(String key) {
        return new AntarchyItemComponent<>(
                key,
                unit -> net.minecraft.nbt.ByteTag.ONE,
                tag -> net.minecraft.util.Unit.INSTANCE);
    }

    public boolean has(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(this.key);
    }

    public T get(ItemStack stack) {
        if (!this.has(stack)) {
            return null;
        }
        return this.reader.apply(stack.getTag().get(this.key));
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        T value = this.get(stack);
        return value != null ? value : fallback;
    }

    public void set(ItemStack stack, T value) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(this.key, this.writer.apply(value));
    }

    public void remove(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(this.key);
        }
    }
}
