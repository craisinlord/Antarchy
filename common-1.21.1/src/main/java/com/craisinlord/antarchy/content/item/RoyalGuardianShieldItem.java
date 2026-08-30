package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.Ingredient;

public class RoyalGuardianShieldItem extends ShieldItem {
    private final Supplier<Ingredient> repairIngredient;

    public RoyalGuardianShieldItem(Item.Properties properties, Supplier<Ingredient> repairIngredient) {
        super(properties);
        this.repairIngredient = repairIngredient;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return this.repairIngredient.get().test(repairCandidate) || super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.royalWeaponEnchantability();
    }
}
