package com.craisinlord.antarchy.content.entity.glimmer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.RawAnimation;

public interface GlimmerVariantBehavior {
    static boolean isValidFoe(Player owner, LivingEntity target) {
        if (!(target instanceof Enemy) || !target.isAlive() || target == owner || target.isInvulnerable()) {
            return false;
        }
        if (target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID())) {
            return false;
        }
        return !owner.isAlliedTo(target);
    }

    static boolean hurtAsOwner(Player owner, LivingEntity target, float amount) {
        return target.hurt(owner.damageSources().playerAttack(owner), amount);
    }

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

    default boolean locksMovement(GlimmerEntity entity) {
        return false;
    }

    @Nullable
    default RawAnimation abilityAnimation() {
        return null;
    }

    @Nullable
    default RawAnimation movingAnimation(GlimmerEntity entity) {
        return null;
    }

    @Nullable
    default RawAnimation idleAnimation(GlimmerEntity entity) {
        return null;
    }

    default double followSpeedModifier() {
        return 1.15D;
    }

    default void sendAbilityMessage(Player owner, Component message) {
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
