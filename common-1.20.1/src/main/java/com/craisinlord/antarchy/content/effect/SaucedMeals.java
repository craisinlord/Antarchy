package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SaucedMeals {
    private static final TagKey<Item> MEAT = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "meat"));
    private static final int DURATION_TICKS = 1200;
    private static final float BONUS_MULTIPLIER = 0.5F;

    private SaucedMeals() {
    }

    public static void applySauced(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(AntarchyObjects.SAUCED_EFFECT.get(), DURATION_TICKS, 0));
    }

    public static void onMealEaten(Player player, ItemStack stack, FoodProperties food) {
        MobEffect sauced = AntarchyObjects.SAUCED_EFFECT.get();
        if (!(stack.is(MEAT) || food.isMeat()) || !player.hasEffect(sauced)) {
            return;
        }
        if (AntarchySettings.throwbackClassicFood() && player.hasEffect(AntarchyObjects.REMEMBRANCE_EFFECT.get())) {
            return;
        }
        int bonusNutrition = Math.max(1, Math.round(food.getNutrition() * BONUS_MULTIPLIER));
        float bonusSaturation = food.getSaturationModifier() * BONUS_MULTIPLIER;
        player.getFoodData().eat(bonusNutrition, bonusSaturation / (2.0F * bonusNutrition));
        player.removeEffect(sauced);
        player.playNotifySound(SoundEvents.HONEY_DRINK, player.getSoundSource(), 0.8F, 1.2F);
    }
}
