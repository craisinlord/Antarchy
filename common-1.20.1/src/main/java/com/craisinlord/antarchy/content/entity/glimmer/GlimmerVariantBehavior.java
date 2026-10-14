package com.craisinlord.antarchy.content.entity.glimmer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface GlimmerVariantBehavior {
    void registerGoals(GlimmerEntity entity);

    ResourceLocation modelGeo();

    ResourceLocation animationFile();

    ResourceLocation texture(GlimmerEntity entity);

    ResourceLocation emissiveTexture(GlimmerEntity entity);

    SoundEvent ambientSound();

    SoundEvent hurtSound();

    SoundEvent deathSound();

    @Nullable
    default InteractionResult onInteract(GlimmerEntity entity, Player player, ItemStack stack, InteractionHand hand) {
        return null;
    }

    default void customTick(GlimmerEntity entity) {
    }

    default void onHurtTarget(GlimmerEntity entity, Entity target) {
    }

    default void tickPassive(GlimmerEntity entity, Player owner) {
    }

    default void tickPassiveEveryTick(GlimmerEntity entity, Player owner) {
    }

    default void clearPassive(GlimmerEntity entity, Player owner) {
    }

    @Nullable
    default software.bernie.geckolib.core.animation.RawAnimation abilityAnimation() {
        return null;
    }

    @Nullable
    default software.bernie.geckolib.core.animation.RawAnimation movingAnimation(GlimmerEntity entity) {
        return null;
    }

    @Nullable
    default software.bernie.geckolib.core.animation.RawAnimation idleAnimation(GlimmerEntity entity) {
        return null;
    }

    default double followSpeedModifier() {
        return 1.15D;
    }

    default void sendAbilityMessage(Player owner, net.minecraft.network.chat.Component message) {
        owner.displayClientMessage(message, true);
    }

    default void tickAbilityCheck(GlimmerEntity entity, Player owner) {
    }

    default int abilityCooldownTicks() {
        return 20 * 120;
    }

    default double growthDecayPerTick() {
        return 0.0D;
    }

    EntityDimensions adultDimensions();

    default EntityDimensions babyDimensions() {
        return this.adultDimensions().scale(0.5F);
    }
}
