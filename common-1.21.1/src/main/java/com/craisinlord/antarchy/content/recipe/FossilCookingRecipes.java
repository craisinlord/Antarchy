package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.content.block.Fossils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public final class FossilCookingRecipes {
    public static final RecipeSerializer<Smelting> SMELTING_SERIALIZER = new SimpleCookingSerializer<>(Smelting::new, 200);
    public static final RecipeSerializer<Blasting> BLASTING_SERIALIZER = new SimpleCookingSerializer<>(Blasting::new, 100);

    private FossilCookingRecipes() {
    }

    public static final class Smelting extends SmeltingRecipe {
        public Smelting(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
            super(group, category, ingredient, result, experience, cookingTime);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
            return Fossils.spawnEggStack(Fossils.entityOf(input.item()));
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return SMELTING_SERIALIZER;
        }
    }

    public static final class Blasting extends BlastingRecipe {
        public Blasting(String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
            super(group, category, ingredient, result, experience, cookingTime);
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
            return Fossils.spawnEggStack(Fossils.entityOf(input.item()));
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return BLASTING_SERIALIZER;
        }
    }
}
