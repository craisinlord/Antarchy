package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import com.craisinlord.anthology.multipart.MultipartEntityOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GlimmerFrogBehavior implements GlimmerVariantBehavior {
    private static final int SMALL_SLIME_SIZE = 1;
    private static final TagKey<EntityType<?>> NO_EAT_TAG =
            TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Antarchy.MODID, "frog_glimmer_no_eat"));
    private static final int PASSIVE_REFRESH_DURATION = 20 * 4;
    private static final int ABILITY_COOLDOWN_TICKS = 20 * 60;
    private static final int ABILITY_SCAN_INTERVAL_TICKS = 5;
    private static final double EAT_RANGE = 8.0D;
    private static final double RELEASE_RANGE = 16.0D;
    private static final float SWALLOW_MAX_HEALTH = 30.0F;
    private static final float GRAB_MAX_HEALTH = 150.0F;
    private static final float LASH_DAMAGE = 12.0F;
    private static final float SWALLOW_DAMAGE = 1.0E6F;
    private static final float GROWTH_PER_EAT_MIN = 0.05F;
    private static final float GROWTH_PER_EAT_MAX = 0.6F;
    private static final float GROWTH_MAX_SCALE = 2.2F;
    private static final float GROWN_SCALE = 1.25F;
    private static final double GROWTH_DECAY_PER_TICK = 0.0006D;
    private static final double PULL_STRENGTH = 0.45D;
    private static final int TONGUE_ANIM_TICKS = 10;
    private static final int TONGUE_GRAB_TICK = 6;

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, path);
    }

    @Override
    public void registerGoals(GlimmerEntity entity) {
        entity.addGoal(0, new FloatGoal(entity));
        entity.addGoal(1, new MeleeAttackGoal(entity, 1.1D, true));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, 1.0D));
        entity.addGoal(6, new LookAtPlayerGoal(entity, Player.class, 8.0F));
        entity.addGoal(7, new RandomLookAroundGoal(entity));
        entity.addTargetGoal(2, new NearestAttackableTargetGoal<>(entity, MagmaCube.class, 10, true, false,
                (magmaCube, level) -> ((MagmaCube) magmaCube).getSize() == SMALL_SLIME_SIZE));
    }

    @Override
    public void onHurtTarget(GlimmerEntity entity, Entity target) {
        if (!(target instanceof MagmaCube magmaCube) || magmaCube.isAlive() || magmaCube.getSize() != SMALL_SLIME_SIZE) {
            return;
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            entity.spawnAtLocation(serverLevel, AntarchyObjects.LUMEN_FROGLIGHT.get());
        }
    }

    @Override
    public Identifier modelGeo() {
        return rl("apple_cow");
    }

    @Override
    public Identifier animationFile() {
        return rl("apple_cow");
    }

    @Override
    public Identifier texture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/frog_glimmer.png");
    }

    @Override
    public Identifier emissiveTexture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/frog_glimmer_emissive.png");
    }

    @Override
    public SoundEvent ambientSound() {
        return SoundEvents.FROG_AMBIENT;
    }

    @Override
    public SoundEvent hurtSound() {
        return SoundEvents.FROG_HURT;
    }

    @Override
    public SoundEvent deathSound() {
        return SoundEvents.FROG_DEATH;
    }

    @Override
    public EntityDimensions adultDimensions() {
        return EntityDimensions.scalable(0.5F, 0.5F);
    }

    @Override
    public void tickPassive(GlimmerEntity entity, Player owner) {
        boolean grown = entity.getGrowthScale() >= GROWN_SCALE;
        owner.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, PASSIVE_REFRESH_DURATION, grown ? 1 : 0, false, false, true));
        if (grown) {
            owner.addEffect(new MobEffectInstance(MobEffects.STRENGTH, PASSIVE_REFRESH_DURATION, 0, false, false, true));
        }
    }

    @Override
    public boolean locksMovement(GlimmerEntity entity) {
        return entity.getAbilityState(PendingEat.class) != null;
    }

    @Override
    public int abilityCooldownTicks() {
        return ABILITY_COOLDOWN_TICKS;
    }

    @Override
    public double growthDecayPerTick() {
        return GROWTH_DECAY_PER_TICK;
    }

    @Override
    public void tickAbilityCheck(GlimmerEntity entity, Player owner) {
        if (entity.tickCount % ABILITY_SCAN_INTERVAL_TICKS != 0 || entity.getAbilityState(PendingEat.class) != null) {
            return;
        }
        LivingEntity target = findEdibleTarget(entity, owner);
        if (target != null) {
            this.startEat(entity, owner, target);
        }
    }

    @Override
    public void customTick(GlimmerEntity entity) {
        PendingEat pending = entity.getAbilityState(PendingEat.class);
        if (pending == null) {
            return;
        }

        if (!pending.target.isAlive() || pending.owner.isRemoved() || pending.target.level() != entity.level()
                || entity.distanceToSqr(pending.target) > RELEASE_RANGE * RELEASE_RANGE) {
            this.endEat(entity);
            return;
        }

        entity.faceYaw(yawToward(entity, pending.target));
        pending.ticksRemaining--;
        if (pending.ticksRemaining == TONGUE_ANIM_TICKS - TONGUE_GRAB_TICK) {
            if (!entity.hasLineOfSight(pending.target)) {
                this.endEat(entity);
                return;
            }
            entity.playSound(SoundEvents.FROG_TONGUE, 1.0F, 1.0F);
        }
        if (pending.ticksRemaining <= TONGUE_ANIM_TICKS - TONGUE_GRAB_TICK) {
            Vec3 mouth = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            pending.target.setDeltaMovement(mouth.subtract(pending.target.position()).scale(PULL_STRENGTH));
            pending.target.hurtMarked = true;
        }

        if (pending.ticksRemaining <= 0) {
            this.endEat(entity);
            this.finishEat(entity, pending.owner, pending.target);
        }
    }

    private void endEat(GlimmerEntity entity) {
        entity.setAbilityState(null);
        entity.setAbilityTarget(null);
    }

    private static float yawToward(GlimmerEntity entity, LivingEntity target) {
        Vec3 delta = target.position().subtract(entity.position());
        return (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private static LivingEntity findEdibleTarget(GlimmerEntity entity, Player owner) {
        AABB area = owner.getBoundingBox().inflate(EAT_RANGE);
        LivingEntity best = null;
        double bestDistSq = EAT_RANGE * EAT_RANGE;
        for (LivingEntity living : owner.level().getEntitiesOfClass(LivingEntity.class, area, candidate -> candidate != entity)) {
            if (!GlimmerVariantBehavior.isValidFoe(owner, living) || isExcluded(living)) {
                continue;
            }
            if (living.distanceToSqr(owner) > EAT_RANGE * EAT_RANGE) {
                continue;
            }
            double distSq = living.distanceToSqr(entity);
            if (distSq > bestDistSq || !entity.hasLineOfSight(living)) {
                continue;
            }
            bestDistSq = distSq;
            best = living;
        }
        return best;
    }

    private static boolean isExcluded(LivingEntity target) {
        if (target instanceof MultipartEntityOwner || target instanceof EnderDragon || target instanceof WitherBoss) {
            return true;
        }
        return target.isPassenger() || target.getMaxHealth() >= GRAB_MAX_HEALTH || target.is(NO_EAT_TAG);
    }

    private void startEat(GlimmerEntity entity, Player owner, LivingEntity target) {
        PendingEat pending = new PendingEat();
        pending.target = target;
        pending.owner = owner;
        pending.ticksRemaining = TONGUE_ANIM_TICKS;
        entity.setAbilityState(pending);
        entity.setAbilityTarget(target);

        entity.faceYaw(yawToward(entity, target));
        entity.playAbilityAnimation(TONGUE_ANIM_TICKS);
        entity.playSound(SoundEvents.FROG_LONG_JUMP, 0.8F, 1.4F);
        entity.startAbilityCooldown();
    }

    private void finishEat(GlimmerEntity entity, Player owner, LivingEntity target) {
        if (target.getHealth() > SWALLOW_MAX_HEALTH) {
            entity.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.2F);
            GlimmerVariantBehavior.hurtAsOwner(owner, target, LASH_DAMAGE);
            GlimmerParticles.abilityBurst(target);
            return;
        }

        Component targetName = target.getDisplayName();
        float maxHealth = target.getMaxHealth();
        GlimmerParticles.abilityBurst(target);
        GlimmerVariantBehavior.hurtAsOwner(owner, target, SWALLOW_DAMAGE);
        if (target.isAlive()) {
            return;
        }

        float growthAdd = Mth.clamp(maxHealth / 40.0F, GROWTH_PER_EAT_MIN, GROWTH_PER_EAT_MAX);
        entity.setGrowthScale(Math.min(GROWTH_MAX_SCALE, entity.getGrowthScale() + growthAdd));
        entity.playSound(SoundEvents.FROG_EAT, 1.2F, 0.9F);
        GlimmerParticles.abilityBurst(entity);
        this.sendAbilityMessage(owner, Component.translatable("entity.antarchy.glimmer.ability.frog", targetName));
    }

    private static final class PendingEat {
        LivingEntity target;
        Player owner;
        int ticksRemaining;
    }
}
