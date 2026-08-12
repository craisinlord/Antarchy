package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UltimateMaceItem extends Item {
    public UltimateMaceItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.ultimateMaceEnchantability();
    }
}
