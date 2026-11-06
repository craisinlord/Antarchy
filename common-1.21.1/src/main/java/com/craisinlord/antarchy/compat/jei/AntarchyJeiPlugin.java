package com.craisinlord.antarchy.compat.jei;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.recipe.AmericanizeRecipe;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.recipe.CustomBrewingRecipes;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import com.craisinlord.antarchy.content.item.GlimmerBottleItem;
import com.craisinlord.antos.content.AntOSObjects;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

@JeiPlugin
public class AntarchyJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(AntarchyObjects.GLIMMER_BOTTLE.get(), componentSubtype(AntarchyObjects.GLIMMER_VARIANT_COMPONENT.get()));
        registration.registerSubtypeInterpreter(AntOSObjects.FLOPPY_DISK.get(), componentSubtype(AntOSObjects.FLOPPY_DISK_COMPONENT.get()));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        ItemStack hfcs = new ItemStack(CustomBrewingRecipes.highFructoseCornSyrupItem());
        registration.addRecipeCategories(new AmericanizeJeiRecipeCategory(registration.getJeiHelpers().getGuiHelper(), hfcs));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        List<IJeiBrewingRecipe> recipes = new ArrayList<>();

        recipes.add(factory.createBrewingRecipe(
                List.of(new ItemStack(CustomBrewingRecipes.cornItem())),
                CustomBrewingRecipes.waterBottleStack(),
                new ItemStack(CustomBrewingRecipes.highFructoseCornSyrupItem())));

        recipes.add(factory.createBrewingRecipe(
                tagItems(CustomBrewingRecipes.rootsTag()),
                new ItemStack(CustomBrewingRecipes.highFructoseCornSyrupItem()),
                new ItemStack(CustomBrewingRecipes.rootBeerItem())));

        ItemStack glimmerBottleInput = GlimmerBottleItem.create(GlimmerVariant.APPLE_COW);
        recipes.add(factory.createBrewingRecipe(tagItems(AntarchyTags.Items.GLIMMER_AUGMENT_OURANWOOD_DEER),
                glimmerBottleInput, GlimmerBottleItem.create(GlimmerVariant.OURANWOOD_DEER)));
        recipes.add(factory.createBrewingRecipe(tagItems(AntarchyTags.Items.GLIMMER_AUGMENT_FROG),
                glimmerBottleInput, GlimmerBottleItem.create(GlimmerVariant.FROG)));
        recipes.add(factory.createBrewingRecipe(tagItems(AntarchyTags.Items.GLIMMER_AUGMENT_ANT),
                glimmerBottleInput, GlimmerBottleItem.create(GlimmerVariant.ANT)));
        recipes.add(factory.createBrewingRecipe(tagItems(AntarchyTags.Items.GLIMMER_AUGMENT_ELKA),
                glimmerBottleInput, GlimmerBottleItem.create(GlimmerVariant.ELKA)));

        ItemStack glimmerBottleDeer = GlimmerBottleItem.create(GlimmerVariant.OURANWOOD_DEER);
        recipes.add(factory.createBrewingRecipe(tagItems(AntarchyTags.Items.GLIMMER_AUGMENT_APPLE_COW),
                glimmerBottleDeer, GlimmerBottleItem.create(GlimmerVariant.APPLE_COW)));

        registration.addRecipes(RecipeTypes.BREWING, recipes);
        if (AntarchySettings.americanizingEnabled()) {
            registration.addRecipes(AmericanizeJeiRecipeCategory.TYPE, List.of(americanizeDisplay()));
        }
    }

    private static AmericanizeJeiRecipeCategory.Recipe americanizeDisplay() {
        List<ItemStack> foods = new ArrayList<>();
        List<ItemStack> results = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (AmericanizeRecipe.isEligibleFood(stack)) {
                foods.add(stack);
                ItemStack result = stack.copyWithCount(1);
                result.set(AntarchyObjects.AMERICAN_COMPONENT.get(), Unit.INSTANCE);
                results.add(result);
            }
        }

        ItemStack hfcs = new ItemStack(CustomBrewingRecipes.highFructoseCornSyrupItem());
        return new AmericanizeJeiRecipeCategory.Recipe(foods, hfcs, results);
    }

    private static ISubtypeInterpreter<ItemStack> componentSubtype(DataComponentType<?> type) {
        return new ISubtypeInterpreter<>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return stack.get(type);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                Object value = stack.get(type);
                return value == null ? "" : value.toString();
            }
        };
    }

    private static List<ItemStack> tagItems(TagKey<Item> tag) {
        return List.of(Ingredient.of(tag).getItems());
    }
}
