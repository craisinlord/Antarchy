package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class AmericanizeRecipe extends CustomRecipe {
    private static final ResourceLocation HIGH_FRUCTOSE_CORN_SYRUP_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "high_fructose_corn_syrup");

    public static final RecipeSerializer<AmericanizeRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(AmericanizeRecipe::new);

    public AmericanizeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findFood(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack food = findFood(input);
        if (food == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = food.copyWithCount(1);
        result.set(AntarchyObjects.AMERICAN_COMPONENT.get(), Unit.INSTANCE);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (isHighFructoseCornSyrup(input.getItem(i))) {
                remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static ItemStack findFood(CraftingInput input) {
        ItemStack food = ItemStack.EMPTY;
        ItemStack hfcs = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (isHighFructoseCornSyrup(stack)) {
                if (!hfcs.isEmpty()) {
                    return null;
                }
                hfcs = stack;
            } else {
                if (!food.isEmpty()) {
                    return null;
                }
                food = stack;
            }
        }

        if (food.isEmpty() || hfcs.isEmpty() || !isEligibleFood(food)) {
            return null;
        }

        return food;
    }

    public static boolean isEligibleFood(ItemStack stack) {
        if (!AntarchySettings.americanizingEnabled()) {
            return false;
        }
        if (stack.isEmpty() || isHighFructoseCornSyrup(stack)) {
            return false;
        }
        if (stack.has(AntarchyObjects.AMERICAN_COMPONENT.get())) {
            return false;
        }
        if (stack.is(AntarchyTags.Items.HFCS_CANNOT_AMERICANIZE)) {
            return false;
        }
        return stack.get(DataComponents.FOOD) != null;
    }

    public static boolean isHighFructoseCornSyrup(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.get(HIGH_FRUCTOSE_CORN_SYRUP_ID));
    }
}
