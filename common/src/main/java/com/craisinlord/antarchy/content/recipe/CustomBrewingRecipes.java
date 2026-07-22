package com.craisinlord.antarchy.content.recipe;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.glimmer.GlimmerVariant;
import com.craisinlord.antarchy.content.item.GlimmerBottleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public final class CustomBrewingRecipes {
    private static final ResourceLocation CORN_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "corn");
    private static final ResourceLocation HIGH_FRUCTOSE_CORN_SYRUP_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "high_fructose_corn_syrup");
    private static final ResourceLocation ROOT_BEER_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "root_beer");
    private static final TagKey<Item> ROOTS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath("antarchy", "roots"));

    private CustomBrewingRecipes() {
    }

    public static boolean hasMix(ItemStack input, ItemStack ingredient) {
        return !getOutput(input, ingredient).isEmpty();
    }

    public static ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (input.isEmpty() || ingredient.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (matchesCornToSyrup(input, ingredient) || matchesCornToSyrup(ingredient, input)) {
            return new ItemStack(highFructoseCornSyrupItem());
        }

        if (matchesSyrupToRootBeer(input, ingredient) || matchesSyrupToRootBeer(ingredient, input)) {
            return new ItemStack(rootBeerItem());
        }

        ItemStack glimmerAugment = getGlimmerAugmentOutput(input, ingredient);
        if (!glimmerAugment.isEmpty()) {
            return glimmerAugment;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack getGlimmerAugmentOutput(ItemStack input, ItemStack ingredient) {
        if (!input.is(AntarchyObjects.GLIMMER_BOTTLE.get())) {
            return ItemStack.EMPTY;
        }

        GlimmerVariant target = glimmerAugmentTarget(ingredient);
        if (target == null || target == GlimmerBottleItem.getVariant(input)) {
            return ItemStack.EMPTY;
        }

        return GlimmerBottleItem.create(target);
    }

    private static GlimmerVariant glimmerAugmentTarget(ItemStack ingredient) {
        if (ingredient.is(AntarchyTags.Items.GLIMMER_AUGMENT_APPLE_COW)) {
            return GlimmerVariant.APPLE_COW;
        }
        if (ingredient.is(AntarchyTags.Items.GLIMMER_AUGMENT_OURANWOOD_DEER)) {
            return GlimmerVariant.OURANWOOD_DEER;
        }
        if (ingredient.is(AntarchyTags.Items.GLIMMER_AUGMENT_FROG)) {
            return GlimmerVariant.FROG;
        }
        if (ingredient.is(AntarchyTags.Items.GLIMMER_AUGMENT_ANT)) {
            return GlimmerVariant.ANT;
        }
        if (ingredient.is(AntarchyTags.Items.GLIMMER_AUGMENT_ELKA)) {
            return GlimmerVariant.ELKA;
        }
        return null;
    }

    private static boolean matchesCornToSyrup(ItemStack base, ItemStack ingredient) {
        return isWaterBottle(base) && ingredient.is(cornItem());
    }

    private static boolean matchesSyrupToRootBeer(ItemStack base, ItemStack ingredient) {
        return base.is(highFructoseCornSyrupItem()) && ingredient.is(ROOTS);
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.is(Potions.WATER);
    }

    public static Item cornItem() {
        return BuiltInRegistries.ITEM.get(CORN_ID);
    }

    public static Item highFructoseCornSyrupItem() {
        return BuiltInRegistries.ITEM.get(HIGH_FRUCTOSE_CORN_SYRUP_ID);
    }

    public static Item rootBeerItem() {
        return BuiltInRegistries.ITEM.get(ROOT_BEER_ID);
    }

    public static TagKey<Item> rootsTag() {
        return ROOTS;
    }

    public static ItemStack waterBottleStack() {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        return stack;
    }
}
