package com.craisinlord.antarchy.compat.item;

import net.minecraft.world.item.ArmorItem;

public final class ArmorDurability {
    private ArmorDurability() {
    }

    public static int forType(ArmorItem.Type type, int multiplier) {
        return switch (type) {
            case HELMET -> 11 * multiplier;
            case CHESTPLATE -> 16 * multiplier;
            case LEGGINGS -> 15 * multiplier;
            case BOOTS -> 13 * multiplier;
        };
    }
}
