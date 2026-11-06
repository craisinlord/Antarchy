package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ThrowbackPlayerMixin {
    @Unique
    private static final int ANTARCHY$FOOD_HEAL_INTERVAL_TICKS = 10;
    @Unique
    private static final float ANTARCHY$FOOD_HEAL_STEP = 1.0F;

    @Unique
    private float antarchy$pendingFoodHealing;

    @ModifyReturnValue(method = "getAttackStrengthScale", at = @At("RETURN"))
    private float antarchy$throwbackNoAttackCooldown(float scale) {
        if (AntarchySettings.throwbackNoAttackCooldown() && this.antarchy$hasRemembrance()) {
            return 1.0F;
        }
        return scale;
    }

    @ModifyReturnValue(method = "getHurtSound", at = @At("RETURN"))
    private SoundEvent antarchy$throwbackClassicHurtSound(SoundEvent sound, DamageSource source) {
        if (AntarchySettings.throwbackClassicHurtSound() && this.antarchy$hasRemembrance()) {
            return AntarchySoundEvents.PLAYER_HURT_CLASSIC.get();
        }
        return sound;
    }

    @ModifyReturnValue(method = "canEat", at = @At("RETURN"))
    private boolean antarchy$throwbackEatToHeal(boolean canEat, boolean canAlwaysEat) {
        if (canEat || !this.antarchy$classicFood()) {
            return canEat;
        }
        Player player = (Player) (Object) this;
        return player.getHealth() < player.getMaxHealth();
    }

    @WrapOperation(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void antarchy$throwbackFoodHeals(FoodData foodData, Item item, ItemStack stack, LivingEntity entity, Operation<Void> original) {
        if (!this.antarchy$classicFood()) {
            original.call(foodData, item, stack, entity);
            return;
        }
        Player player = (Player) (Object) this;
        FoodProperties food = stack.getFoodProperties(player);
        if (!player.level().isClientSide && item.isEdible() && food != null) {
            this.antarchy$pendingFoodHealing += food.getNutrition();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$throwbackHealOverTime(CallbackInfo ci) {
        if (this.antarchy$pendingFoodHealing <= 0.0F) {
            return;
        }
        Player player = (Player) (Object) this;
        if (!this.antarchy$classicFood() || !player.isAlive() || player.getHealth() >= player.getMaxHealth()) {
            this.antarchy$pendingFoodHealing = 0.0F;
            return;
        }
        if (player.tickCount % ANTARCHY$FOOD_HEAL_INTERVAL_TICKS == 0) {
            float amount = Math.min(ANTARCHY$FOOD_HEAL_STEP, this.antarchy$pendingFoodHealing);
            player.heal(amount);
            this.antarchy$pendingFoodHealing -= amount;
        }
    }

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void antarchy$throwbackNoExhaustion(float exhaustion, CallbackInfo ci) {
        if (this.antarchy$classicFood()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean antarchy$classicFood() {
        return AntarchySettings.throwbackClassicFood() && this.antarchy$hasRemembrance();
    }

    @Unique
    private boolean antarchy$hasRemembrance() {
        return ((Player) (Object) this).hasEffect(AntarchyObjects.REMEMBRANCE_EFFECT.get());
    }
}
