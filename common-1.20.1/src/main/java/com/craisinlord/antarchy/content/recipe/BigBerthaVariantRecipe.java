package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class BigBerthaVariantRecipe extends ShapelessRecipe {
    public static final RecipeSerializer<BigBerthaVariantRecipe> SERIALIZER = new Serializer();

    public BigBerthaVariantRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        ItemStack result = super.assemble(input, registries);
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            ItemStack ingredient = input.getItem(slot);
            if (ingredient.getItem() instanceof BigBerthaItem) {
                if (ingredient.hasTag()) {
                    result.getOrCreateTag().merge(ingredient.getTag().copy());
                }
                break;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static final class Serializer implements RecipeSerializer<BigBerthaVariantRecipe> {
        @Override
        public BigBerthaVariantRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new BigBerthaVariantRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json));
        }

        @Override
        public BigBerthaVariantRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new BigBerthaVariantRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BigBerthaVariantRecipe recipe) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
