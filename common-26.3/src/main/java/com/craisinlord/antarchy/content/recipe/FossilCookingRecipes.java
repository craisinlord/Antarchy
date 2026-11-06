package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.content.block.Fossils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public final class FossilCookingRecipes {
    public static final RecipeSerializer<Smelting> SMELTING_SERIALIZER = new RecipeSerializer<>(AbstractCookingRecipe.cookingMapCodec(Smelting::new), AbstractCookingRecipe.cookingStreamCodec(Smelting::new));
    public static final RecipeSerializer<Blasting> BLASTING_SERIALIZER = new RecipeSerializer<>(AbstractCookingRecipe.cookingMapCodec(Blasting::new), AbstractCookingRecipe.cookingStreamCodec(Blasting::new));

    private FossilCookingRecipes() {
    }

    public static final class Smelting extends SmeltingRecipe {
        public Smelting(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
            super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
        }

        @Override
        @SuppressWarnings("unchecked")
        public RecipeSerializer<SmeltingRecipe> getSerializer() {
            return (RecipeSerializer<SmeltingRecipe>) (RecipeSerializer<?>) SMELTING_SERIALIZER;
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input) {
            return Fossils.spawnEggStack(Fossils.entityOf(input.item()));
        }
    }

    public static final class Blasting extends BlastingRecipe {
        public Blasting(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
            super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
        }

        @Override
        @SuppressWarnings("unchecked")
        public RecipeSerializer<BlastingRecipe> getSerializer() {
            return (RecipeSerializer<BlastingRecipe>) (RecipeSerializer<?>) BLASTING_SERIALIZER;
        }

        @Override
        public ItemStack assemble(SingleRecipeInput input) {
            return Fossils.spawnEggStack(Fossils.entityOf(input.item()));
        }
    }
}
