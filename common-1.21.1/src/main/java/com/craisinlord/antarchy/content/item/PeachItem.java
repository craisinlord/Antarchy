package com.craisinlord.antarchy.content.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class PeachItem extends Item {
    public PeachItem() {
        super(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(4)
                .saturationModifier(1.4F)
                .build()));
    }
}
