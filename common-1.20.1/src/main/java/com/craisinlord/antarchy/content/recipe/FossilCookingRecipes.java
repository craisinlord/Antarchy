package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.content.block.Fossils;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public final class FossilCookingRecipes {
    public static final RecipeSerializer<Smelting> SMELTING_SERIALIZER = new SmeltingSerializer();
    public static final RecipeSerializer<Blasting> BLASTING_SERIALIZER = new BlastingSerializer();

    private FossilCookingRecipes() {
    }

    public static final class Smelting extends SmeltingRecipe {
        public Smelting(SmeltingRecipe recipe) {
            super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getIngredients().get(0),
                    recipe.getResultItem(RegistryAccess.EMPTY), recipe.getExperience(), recipe.getCookingTime());
        }

        @Override
        public ItemStack assemble(Container input, RegistryAccess registries) {
            return Fossils.spawnEggStack(Fossils.entityOf(input.getItem(0)));
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return SMELTING_SERIALIZER;
        }
    }

    public static final class Blasting extends BlastingRecipe {
        public Blasting(BlastingRecipe recipe) {
            super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getIngredients().get(0),
                    recipe.getResultItem(RegistryAccess.EMPTY), recipe.getExperience(), recipe.getCookingTime());
        }

        @Override
        public ItemStack assemble(Container input, RegistryAccess registries) {
            return Fossils.spawnEggStack(Fossils.entityOf(input.getItem(0)));
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return BLASTING_SERIALIZER;
        }
    }

    private static final class SmeltingSerializer implements RecipeSerializer<Smelting> {
        @Override
        public Smelting fromJson(ResourceLocation id, JsonObject json) {
            return new Smelting(RecipeSerializer.SMELTING_RECIPE.fromJson(id, json));
        }

        @Override
        public Smelting fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new Smelting(RecipeSerializer.SMELTING_RECIPE.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, Smelting recipe) {
            RecipeSerializer.SMELTING_RECIPE.toNetwork(buffer, recipe);
        }
    }

    private static final class BlastingSerializer implements RecipeSerializer<Blasting> {
        @Override
        public Blasting fromJson(ResourceLocation id, JsonObject json) {
            return new Blasting(RecipeSerializer.BLASTING_RECIPE.fromJson(id, json));
        }

        @Override
        public Blasting fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new Blasting(RecipeSerializer.BLASTING_RECIPE.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, Blasting recipe) {
            RecipeSerializer.BLASTING_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
