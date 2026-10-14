package com.craisinlord.antarchy.content.component;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class AntarchyItemTag {
    private AntarchyItemTag() {
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> updater) {
        CompoundTag tag = stack.getOrCreateTag();
        updater.accept(tag);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static CompoundTag getOrEmpty(ItemStack stack) {
        return stack.hasTag() ? stack.getTag() : new CompoundTag();
    }
}
