package com.craisinlord.antarchy.content.creative;

import java.util.Comparator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public final class CreativeTabOrder {
    private CreativeTabOrder() {}

    public static final Comparator<Item> COMPARATOR =
            Comparator.comparingInt(CreativeTabContents::orderIndex)
                    .thenComparing(CreativeTabOrder::path);

    public static int group(Item item) {
        return CreativeTabContents.orderIndex(item);
    }

    public static int subOrder(Item item) {
        return 0;
    }

    private static String path(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }
}
