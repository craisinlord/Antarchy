package com.craisinlord.antarchy.compat.jei;

import com.craisinlord.antarchy.Antarchy;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AmericanizeJeiRecipeCategory implements IRecipeCategory<AmericanizeJeiRecipeCategory.Recipe> {
    public static final RecipeType<Recipe> TYPE = RecipeType.create(Antarchy.MODID, "americanize", Recipe.class);

    private final IDrawable icon;

    public record Recipe(List<ItemStack> foods, ItemStack hfcs, List<ItemStack> results) {
    }

    public AmericanizeJeiRecipeCategory(IGuiHelper guiHelper, ItemStack hfcsIcon) {
        this.icon = guiHelper.createDrawableItemStack(hfcsIcon);
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.antarchy.americanize");
    }

    @Override
    public int getWidth() {
        return 78;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 1)
                .setStandardSlotBackground()
                .addItemStacks(recipe.foods());
        builder.addSlot(RecipeIngredientRole.INPUT, 30, 1)
                .setStandardSlotBackground()
                .addItemStack(recipe.hfcs());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 59, 1)
                .setStandardSlotBackground()
                .addItemStacks(recipe.results());
    }
}
