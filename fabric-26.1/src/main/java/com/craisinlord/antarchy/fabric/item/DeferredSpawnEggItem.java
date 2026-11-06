package com.craisinlord.antarchy.fabric.item;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class DeferredSpawnEggItem extends SpawnEggItem {
    private final int backgroundColor;
    private final int highlightColor;

    public DeferredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> typeSupplier, int backgroundColor, int highlightColor, Item.Properties properties) {
        super(properties.spawnEgg(typeSupplier.get()));
        this.backgroundColor = backgroundColor;
        this.highlightColor = highlightColor;
    }

    public int getColor(int tintIndex) {
        return tintIndex == 0 ? 0xFF000000 | this.backgroundColor : 0xFF000000 | this.highlightColor;
    }
}
