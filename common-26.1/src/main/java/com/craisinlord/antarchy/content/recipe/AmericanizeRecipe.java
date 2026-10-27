package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AmericanizeRecipe extends CustomRecipe {
    private static final Identifier HIGH_FRUCTOSE_CORN_SYRUP_ID = Identifier.fromNamespaceAndPath("antarchy", "high_fructose_corn_syrup");

    private static final MapCodec<AmericanizeRecipe> MAP_CODEC = CraftingBookCategory.CODEC.fieldOf("category")
            .orElse(CraftingBookCategory.MISC)
            .xmap(AmericanizeRecipe::new, AmericanizeRecipe::category);
    private static final StreamCodec<RegistryFriendlyByteBuf, AmericanizeRecipe> STREAM_CODEC =
            CraftingBookCategory.STREAM_CODEC.map(AmericanizeRecipe::new, AmericanizeRecipe::category).cast();

    public static final RecipeSerializer<AmericanizeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final CraftingBookCategory category;

    public AmericanizeRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findFood(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack food = findFood(input);
        if (food == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = food.copyWithCount(1);
        result.set(AntarchyObjects.AMERICAN_COMPONENT.get(), Unit.INSTANCE);
        return result;
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
    public RecipeSerializer<AmericanizeRecipe> getSerializer() {
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
        return !stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.getValue(HIGH_FRUCTOSE_CORN_SYRUP_ID));
    }
}
