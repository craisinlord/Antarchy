package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public class BigBerthaVariantRecipe implements CraftingRecipe {
    private static final MapCodec<BigBerthaVariantRecipe> CODEC =
            ShapelessRecipe.MAP_CODEC.xmap(BigBerthaVariantRecipe::new, recipe -> recipe.recipe);
    private static final StreamCodec<RegistryFriendlyByteBuf, BigBerthaVariantRecipe> STREAM_CODEC =
            ShapelessRecipe.STREAM_CODEC.map(BigBerthaVariantRecipe::new, recipe -> recipe.recipe);
    public static final RecipeSerializer<BigBerthaVariantRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    private final ShapelessRecipe recipe;

    public BigBerthaVariantRecipe(ShapelessRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.recipe.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = this.recipe.assemble(input);
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
    public boolean showNotification() {
        return this.recipe.showNotification();
    }

    @Override
    public String group() {
        return this.recipe.group();
    }

    @Override
    public CraftingBookCategory category() {
        return this.recipe.category();
    }

    @Override
    public PlacementInfo placementInfo() {
        return this.recipe.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {
        return this.recipe.display();
    }

    @Override
    public RecipeSerializer<BigBerthaVariantRecipe> getSerializer() {
        return SERIALIZER;
    }
}
