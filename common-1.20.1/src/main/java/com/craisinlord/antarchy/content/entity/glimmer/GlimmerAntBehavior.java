package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.entity.EntityReach;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.RawAnimation;

public class GlimmerAntBehavior implements GlimmerVariantBehavior {
    private static final ResourceLocation REACH_BOOST_ID =
            new ResourceLocation(Antarchy.MODID, "glimmer_ant_reach_boost");
    private static final double REACH_BOOST_AMOUNT = 1.5D;
    private static final float LARGE_ENEMY_MIN_HEALTH = 40.0F;
    private static final int ABILITY_COOLDOWN_TICKS = 20 * 60;
    private static final int STRENGTH_BURST_DURATION = 20 * 12;
    private static final int ATTACK_ANIM_TICKS = 20;
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(Antarchy.MODID, path);
    }

    public static void clearReach(Player owner) {
        AttributeInstance instance = EntityReach.instance(owner);
        if (instance != null) {
            instance.removeModifier(EntityReach.modifierId(REACH_BOOST_ID));
        }
    }

    @Override
    public void registerGoals(GlimmerEntity entity) {
        entity.addGoal(0, new FloatGoal(entity));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, 1.1D));
        entity.addGoal(6, new LookAtPlayerGoal(entity, Player.class, 8.0F));
        entity.addGoal(7, new RandomLookAroundGoal(entity));
    }

    @Override
    public ResourceLocation modelGeo() {
        return rl("geo/ant.geo.json");
    }

    @Override
    public ResourceLocation animationFile() {
        return rl("animations/ant.animation.json");
    }

    @Override
    public ResourceLocation texture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/glimmer_ant.png");
    }

    @Override
    public ResourceLocation emissiveTexture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/glimmer_ant_emissive.png");
    }

    @Override
    public SoundEvent ambientSound() {
        return AntarchySoundEvents.ANT_AMBIENT.get();
    }

    @Override
    public SoundEvent hurtSound() {
        return AntarchySoundEvents.ANT_HURT.get();
    }

    @Override
    public SoundEvent deathSound() {
        return AntarchySoundEvents.ANT_HURT.get();
    }

    @Override
    public EntityDimensions adultDimensions() {
        return EntityDimensions.scalable(0.6375F, 0.2125F);
    }

    @Override
    public void tickPassiveEveryTick(GlimmerEntity entity, Player owner) {
        AttributeInstance instance = EntityReach.instance(owner);
        if (instance != null && instance.getModifier(EntityReach.modifierId(REACH_BOOST_ID)) == null) {
            instance.addTransientModifier(EntityReach.modifier(REACH_BOOST_ID, REACH_BOOST_AMOUNT, AttributeModifier.Operation.ADDITION));
        }

        int timestamp = owner.getLastHurtMobTimestamp();
        int lastSeen = entity.getLastOwnerHurtMobTimestamp();
        entity.setLastOwnerHurtMobTimestamp(timestamp);
        if (lastSeen >= 0 && timestamp != lastSeen && entity.canUseAbility()) {
            this.tryBurst(entity, owner);
        }
    }

    @Override
    public void clearPassive(GlimmerEntity entity, Player owner) {
        clearReach(owner);
        entity.setLastOwnerHurtMobTimestamp(-1);
    }

    @Override
    public int abilityCooldownTicks() {
        return ABILITY_COOLDOWN_TICKS;
    }

    @Override
    @Nullable
    public RawAnimation abilityAnimation() {
        return ATTACK_ANIM;
    }

    private void tryBurst(GlimmerEntity entity, Player owner) {
        LivingEntity target = owner.getLastHurtMob();
        if (!(target instanceof Enemy) || target.getMaxHealth() < LARGE_ENEMY_MIN_HEALTH) {
            return;
        }

        owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_BURST_DURATION, 1, false, true, true));
        owner.level().playSound(null, owner.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, owner.getSoundSource(), 1.0F, 0.8F);
        entity.playAbilityAnimation(ATTACK_ANIM_TICKS);
        GlimmerParticles.abilityBurst(entity);
        GlimmerParticles.abilityBurst(owner);
        this.sendAbilityMessage(owner, Component.translatable("entity.antarchy.glimmer.ability.ant", target.getDisplayName()));
        entity.startAbilityCooldown();
    }
}
