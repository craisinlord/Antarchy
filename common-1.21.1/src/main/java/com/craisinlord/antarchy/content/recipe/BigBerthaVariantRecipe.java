package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class BigBerthaVariantRecipe extends ShapelessRecipe {
    public static final RecipeSerializer<BigBerthaVariantRecipe> SERIALIZER = new Serializer();

    public BigBerthaVariantRecipe(ShapelessRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(null), recipe.getIngredients());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack ingredient = input.getItem(slot);
            if (ingredient.getItem() instanceof BigBerthaItem) {
                result.applyComponents(ingredient.getComponentsPatch());
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
        private static final MapCodec<BigBerthaVariantRecipe> CODEC =
                RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(BigBerthaVariantRecipe::new, recipe -> recipe);
        private static final StreamCodec<RegistryFriendlyByteBuf, BigBerthaVariantRecipe> STREAM_CODEC =
                RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(BigBerthaVariantRecipe::new, recipe -> recipe);

        @Override
        public MapCodec<BigBerthaVariantRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BigBerthaVariantRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
