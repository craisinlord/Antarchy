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

/**
 * Per-variant behavior for the composite {@link GlimmerEntity}: AI, model/texture selection, and sounds.
 * One concrete implementation per {@link GlimmerVariant}, chosen via composition rather than subclassing
 * so a single EntityType can be reliably reconstructed across save/load.
 */
public interface GlimmerVariantBehavior {
    void registerGoals(GlimmerEntity entity);

    ResourceLocation modelGeo();

    ResourceLocation animationFile();

    ResourceLocation texture(GlimmerEntity entity);

    ResourceLocation emissiveTexture(GlimmerEntity entity);

    SoundEvent ambientSound();

    SoundEvent hurtSound();

    SoundEvent deathSound();

    /**
     * Optional variant-specific interaction (e.g. shearing the Apple Cow variant for a Spirit Apple).
     * Return null to fall through to the entity's default interaction handling.
     */
    @Nullable
    default InteractionResult onInteract(GlimmerEntity entity, Player player, ItemStack stack, InteractionHand hand) {
        return null;
    }

    /**
     * Optional per-tick hook for variant-specific server logic (e.g. the Basilisk's petrify gaze).
     */
    default void customTick(GlimmerEntity entity) {
    }

    /**
     * Optional hook fired after {@code entity} lands a successful hit on {@code target}
     * (e.g. the Frog variant dropping Lumen Froglight when it kills a small Magma Cube).
     */
    default void onHurtTarget(GlimmerEntity entity, Entity target) {
    }

    /**
     * Called periodically (roughly once a second) while this Glimmer is tamed and its
     * owner is nearby and loaded. Should (re)apply the variant's passive effect with a
     * short duration so it naturally lapses if the owner leaves range.
     */
    default void tickPassive(GlimmerEntity entity, Player owner) {
    }

    /**
     * Called every server tick (not just the coarse refresh interval) while nearby, for
     * passives that need finer-grained bookkeeping than a periodic effect refresh (e.g.
     * the Apple Cow's exhaustion dampening).
     */
    default void tickPassiveEveryTick(GlimmerEntity entity, Player owner) {
    }

    /**
     * Called once the tick the owner leaves range (or the companion is otherwise no
     * longer eligible) so any directly-applied (non-MobEffect) passive state can be undone.
     */
    default void clearPassive(GlimmerEntity entity, Player owner) {
    }

    /**
     * Animation (from this variant's own animation file) to play while
     * {@link GlimmerEntity#playAbilityAnimation(int)} is active. Null plays no override.
     */
    @Nullable
    default software.bernie.geckolib.animation.RawAnimation abilityAnimation() {
        return null;
    }

    /**
     * Animation to play in place of the default walk loop while moving. Null uses the
     * default walk animation (e.g. the Deer switches to its run clip while following its owner).
     */
    @Nullable
    default software.bernie.geckolib.animation.RawAnimation movingAnimation(GlimmerEntity entity) {
        return null;
    }

    /**
     * Animation to play while idle in place of the default idle loop. Null uses the
     * shared Glimmer idle animation.
     */
    @Nullable
    default software.bernie.geckolib.animation.RawAnimation idleAnimation(GlimmerEntity entity) {
        return null;
    }

    /**
     * Navigation speed multiplier used while following the owner (see {@link GlimmerFollowOwnerGoal}).
     */
    default double followSpeedModifier() {
        return 1.15D;
    }

    /**
     * Shared action-bar feedback helper for ability-use messages.
     */
    default void sendAbilityMessage(Player owner, net.minecraft.network.chat.Component message) {
        owner.displayClientMessage(message, true);
    }

    /**
     * Called every server tick while the owner is nearby and the ability is off cooldown.
     * Implementations should check their own trigger condition and, if it fires, perform
     * the ability and call {@link GlimmerEntity#startAbilityCooldown()}.
     */
    default void tickAbilityCheck(GlimmerEntity entity, Player owner) {
    }

    /**
     * Cooldown duration for this variant's active ability, in ticks.
     */
    default int abilityCooldownTicks() {
        return 20 * 120;
    }

    /**
     * Per-tick shrink/growth-recovery rate used to decay {@link GlimmerEntity#getGrowthScale()}
     * back toward 1.0 after an ability temporarily changes it. 0 means the variant never changes scale.
     */
    default double growthDecayPerTick() {
        return 0.0D;
    }

    /**
     * Hitbox size matching the real creature this variant is a "spirit" of, rather than
     * a single hitbox shared by every variant.
     */
    EntityDimensions adultDimensions();

    /**
     * Defaults to half the adult size, matching vanilla's typical baby scaling.
     */
    default EntityDimensions babyDimensions() {
        return this.adultDimensions().scale(0.5F);
    }
}
