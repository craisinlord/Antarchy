package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class AmericanizeRecipe extends CustomRecipe {
    private static final ResourceLocation HIGH_FRUCTOSE_CORN_SYRUP_ID = new ResourceLocation("antarchy", "high_fructose_corn_syrup");

    public static final RecipeSerializer<AmericanizeRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(AmericanizeRecipe::new);

    public AmericanizeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return findFood(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        ItemStack food = findFood(input);
        if (food == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = food.copyWithCount(1);
        AntarchyObjects.AMERICAN_COMPONENT.set(result, Unit.INSTANCE);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < input.getContainerSize(); i++) {
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

    private static ItemStack findFood(CraftingContainer input) {
        ItemStack food = ItemStack.EMPTY;
        ItemStack hfcs = ItemStack.EMPTY;
        for (int i = 0; i < input.getContainerSize(); i++) {
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
        if (AntarchyObjects.AMERICAN_COMPONENT.has(stack)) {
            return false;
        }
        if (stack.is(AntarchyTags.Items.HFCS_CANNOT_AMERICANIZE)) {
            return false;
        }
        return stack.getItem().isEdible();
    }

    public static boolean isHighFructoseCornSyrup(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.get(HIGH_FRUCTOSE_CORN_SYRUP_ID));
    }
}
