package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public final class SaucedMeals {
    private static final int DURATION_TICKS = 1200;
    private static final float BONUS_MULTIPLIER = 0.5F;

    private SaucedMeals() {
    }

    public static void applySauced(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(AntarchyObjects.SAUCED_EFFECT.get(), DURATION_TICKS, 0));
    }

    public static void onMealEaten(Player player, ItemStack stack, FoodProperties food) {
        Holder<MobEffect> sauced = AntarchyObjects.SAUCED_EFFECT.get();
        if (!stack.is(ItemTags.MEAT) || !player.hasEffect(sauced)) {
            return;
        }
        if (AntarchySettings.throwbackClassicFood() && player.hasEffect(AntarchyObjects.REMEMBRANCE_EFFECT.get())) {
            return;
        }
        int bonusNutrition = Math.max(1, Math.round(food.nutrition() * BONUS_MULTIPLIER));
        float bonusSaturation = food.saturation() * BONUS_MULTIPLIER;
        player.getFoodData().eat(bonusNutrition, bonusSaturation / (2.0F * bonusNutrition));
        player.removeEffect(sauced);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(SoundEvents.HONEY_DRINK, player.getSoundSource(),
                    player.getX(), player.getY(), player.getZ(), 0.8F, 1.2F, player.getRandom().nextLong()));
        }
    }
}
