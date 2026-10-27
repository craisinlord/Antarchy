package com.craisinlord.antarchy.content.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public final class SimpleToolTier {
    private SimpleToolTier() {
    }

    public static ToolMaterial of(
            int uses,
            float speed,
            float attackDamageBonus,
            TagKey<Block> incorrectBlocksForDrops,
            int enchantmentValue,
            TagKey<Item> repairItems
    ) {
        return new ToolMaterial(incorrectBlocksForDrops, uses, speed, attackDamageBonus, enchantmentValue, repairItems);
    }
}
