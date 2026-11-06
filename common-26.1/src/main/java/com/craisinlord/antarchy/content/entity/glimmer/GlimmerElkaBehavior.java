package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animation.RawAnimation;

public class GlimmerElkaBehavior implements GlimmerVariantBehavior {
    private static final int PASSIVE_REFRESH_DURATION = 20 * 4;
    private static final int WARD_AMPLIFIER = 1;
    private static final int ABILITY_COOLDOWN_TICKS = 20 * 60;
    private static final int CRY_ANIM_TICKS = 40;
    private static final double SHOCKWAVE_RADIUS = 6.0D;
    private static final double SHOCKWAVE_KNOCKBACK = 1.3D;
    private static final float SHOCKWAVE_DAMAGE = 8.0F;
    private static final int IDLE_ANIMATION_CYCLE_TICKS = 60;
    private static final int QUIRK_ANIMATION_TICKS = 80;
    private static final int QUIRK_2_ANIMATION_TICKS = 60;
    private static final int QUIRK_3_ANIMATION_TICKS = 80;
    private static final int SIT_TRANSITION_TICKS = 20;
    private static final int QUIRK_VARIANT_SECOND = 1;
    private static final int QUIRK_VARIANT_SITTING = 2;
    private static final int VARIANT_SIT_DOWN = 3;
    private static final int VARIANT_GET_UP = 4;
    private static final int QUIRK_CHANCE = 60;
    private static final int SIT_MIN_TICKS = 20 * 60;
    private static final int SIT_MAX_TICKS = 20 * 60 * 3;
    private static final int SIT_COOLDOWN_TICKS = 20 * 60 * 6;
    private static final int SIT_CHECK_CHANCE_SHADE = 300;
    private static final int SIT_CHECK_CHANCE_OPEN = 1200;
    private static final double FOLLOW_BREAK_SIT_DISTANCE_SQR = 64.0D;
    private static final double STROLL_SPEED = 0.8D;
    private static final RawAnimation ELKA_SIT_DOWN_ANIM = RawAnimation.begin().thenPlay("sit");
    private static final RawAnimation ELKA_GET_UP_ANIM = RawAnimation.begin().thenPlay("get_up");
    private static final RawAnimation ELKA_WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ELKA_IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ELKA_SIT_IDLE_ANIM = RawAnimation.begin().thenLoop("sit_idle");
    private static final RawAnimation ELKA_QUIRK_ANIM = RawAnimation.begin().thenPlay("quirk");
    private static final RawAnimation ELKA_QUIRK_2_ANIM = RawAnimation.begin().thenPlay("quirk_2");
    private static final RawAnimation ELKA_QUIRK_3_ANIM = RawAnimation.begin().thenPlay("quirk_3");

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, path);
    }

    @Override
    public void registerGoals(GlimmerEntity entity) {
        entity.addGoal(0, new FloatGoal(entity));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, STROLL_SPEED) {
            @Override
            public boolean canUse() {
                return !entity.isElkaIdleSitting() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !entity.isElkaIdleSitting() && super.canContinueToUse();
            }
        });
        entity.addGoal(6, new LookAtPlayerGoal(entity, Player.class, 8.0F));
        entity.addGoal(7, new RandomLookAroundGoal(entity));
    }

    @Override
    public Identifier modelGeo() {
        return rl("elka");
    }

    @Override
    public Identifier animationFile() {
        return rl("elka");
    }

    @Override
    public Identifier texture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/elka_glimmer.png");
    }

    @Override
    public Identifier emissiveTexture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/elka_glimmer_emissive.png");
    }

    @Override
    public SoundEvent ambientSound() {
        return SoundEvents.PANDA_AMBIENT;
    }

    @Override
    public SoundEvent hurtSound() {
        return SoundEvents.PANDA_BITE;
    }

    @Override
    public SoundEvent deathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Override
    public EntityDimensions adultDimensions() {
        return EntityDimensions.scalable(2.2F, 3.5F);
    }

    @Override
    public EntityDimensions babyDimensions() {
        return EntityDimensions.scalable(1.0F, 1.5F);
    }

    @Override
    public void tickPassive(GlimmerEntity entity, Player owner) {
        owner.addEffect(new MobEffectInstance(AntarchyObjects.BLOODGLASS_WARD.get(), PASSIVE_REFRESH_DURATION, WARD_AMPLIFIER, false, false, true));
    }

    @Override
    public void customTick(GlimmerEntity entity) {
        this.tickIdleState(entity);
    }

    @Override
    public void tickPassiveEveryTick(GlimmerEntity entity, Player owner) {
        if (!(owner instanceof BloodglassAccess access)) {
            return;
        }

        long lostNow = access.antarchy$getArmorShieldLostCount() + access.antarchy$getAppleShieldLostCount();
        long lastLost = entity.getLastOwnerShieldLostCount();
        if (lastLost >= 0L && lostNow > lastLost && entity.canUseAbility()) {
            this.shatterWard(entity, owner);
        }
        entity.setLastOwnerShieldLostCount(lostNow);
    }

    @Override
    public void clearPassive(GlimmerEntity entity, Player owner) {
        entity.setLastOwnerShieldLostCount(-1L);
    }

    @Override
    public int abilityCooldownTicks() {
        return ABILITY_COOLDOWN_TICKS;
    }

    @Override
    @Nullable
    public RawAnimation movingAnimation(GlimmerEntity entity) {
        return ELKA_WALK_ANIM;
    }

    @Override
    @Nullable
    public RawAnimation idleAnimation(GlimmerEntity entity) {
        if (entity.getElkaIdleQuirkTicks() > 0) {
            return switch (entity.getElkaIdleQuirkVariant()) {
                case QUIRK_VARIANT_SECOND -> ELKA_QUIRK_2_ANIM;
                case QUIRK_VARIANT_SITTING -> ELKA_QUIRK_3_ANIM;
                case VARIANT_SIT_DOWN -> ELKA_SIT_DOWN_ANIM;
                case VARIANT_GET_UP -> ELKA_GET_UP_ANIM;
                default -> ELKA_QUIRK_ANIM;
            };
        }
        if (entity.isElkaIdleSitting() || entity.isOrderedToSit()) {
            return ELKA_SIT_IDLE_ANIM;
        }
        return ELKA_IDLE_ANIM;
    }

    @Override
    @Nullable
    public com.geckolib.animation.RawAnimation abilityAnimation() {
        return com.geckolib.animation.RawAnimation.begin().thenPlay("cry");
    }

    private void shatterWard(GlimmerEntity entity, Player owner) {
        entity.playAbilityAnimation(CRY_ANIM_TICKS);
        entity.playSound(SoundEvents.PANDA_CANT_BREED, 1.2F, 0.7F);
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, entity.getSoundSource(), 1.0F, 0.6F);
        GlimmerParticles.ring(owner, SHOCKWAVE_RADIUS);
        GlimmerParticles.abilityBurst(entity);

        AABB area = owner.getBoundingBox().inflate(SHOCKWAVE_RADIUS);
        for (Entity nearby : owner.level().getEntities(owner, area)) {
            if (nearby instanceof Projectile projectile) {
                if (projectile.getOwner() != owner) {
                    Vec3 away = projectile.position().subtract(owner.position()).normalize();
                    projectile.setDeltaMovement(away.scale(1.5D));
                }
                continue;
            }

            if (nearby instanceof LivingEntity living) {
                boolean foe = GlimmerVariantBehavior.isValidFoe(owner, living);
                if (!foe && living.getLastHurtByMob() != owner) {
                    continue;
                }
                if (foe) {
                    GlimmerVariantBehavior.hurtAsOwner(owner, living, SHOCKWAVE_DAMAGE);
                }
                Vec3 away = living.position().subtract(owner.position());
                double horizontalDistance = Math.max(0.1D, away.horizontalDistance());
                living.knockback(SHOCKWAVE_KNOCKBACK, -away.x / horizontalDistance, -away.z / horizontalDistance);
            }
        }

        this.sendAbilityMessage(owner, Component.translatable("entity.antarchy.glimmer.ability.elka"));
        entity.startAbilityCooldown();
    }

    private void tickIdleState(GlimmerEntity entity) {
        int quirkTicks = entity.getElkaIdleQuirkTicks();
        if (quirkTicks > 0) {
            entity.setElkaIdleQuirkTicks(quirkTicks - 1);
        }

        if (entity.isElkaIdleSitting()) {
            if (!this.canRemainSitting(entity)) {
                this.stopSitting(entity);
                return;
            }

            int sitTicks = entity.getElkaIdleSitTicksRemaining();
            if (sitTicks > 0) {
                entity.setElkaIdleSitTicksRemaining(sitTicks - 1);
                if (sitTicks - 1 <= 0) {
                    this.stopSitting(entity);
                    return;
                }
            }
            this.tickIdleQuirk(entity);
            return;
        }

        if (entity.getElkaIdleSitCooldownTicks() > 0) {
            entity.setElkaIdleSitCooldownTicks(entity.getElkaIdleSitCooldownTicks() - 1);
        }

        if (entity.getElkaIdleSitCooldownTicks() <= 0
                && this.canStartSitting(entity)
                && entity.getRandom().nextInt(SIT_CHECK_CHANCE_SHADE) == 0
                && (this.isNearShade(entity) || entity.getRandom().nextInt(SIT_CHECK_CHANCE_OPEN / SIT_CHECK_CHANCE_SHADE) == 0)) {
            this.startSitting(entity);
            return;
        }

        this.tickIdleQuirk(entity);
    }

    private void tickIdleQuirk(GlimmerEntity entity) {
        if (entity.isOrderedToSit()) {
            return;
        }
        if (entity.getElkaIdleQuirkTicks() > 0
                || entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D
                || entity.getNavigation().isInProgress()
                || entity.getTarget() != null
                || entity.isFollowingOwner() && entity.getOwner() != null && entity.distanceToSqr(entity.getOwner()) > 9.0D) {
            entity.setElkaIdleAnimationCycleTicks(0);
            return;
        }

        entity.setElkaIdleAnimationCycleTicks(entity.getElkaIdleAnimationCycleTicks() + 1);
        if (entity.getElkaIdleAnimationCycleTicks() < IDLE_ANIMATION_CYCLE_TICKS) {
            return;
        }
        entity.setElkaIdleAnimationCycleTicks(0);

        if (entity.getRandom().nextInt(QUIRK_CHANCE) != 0) {
            return;
        }

        if (entity.isElkaIdleSitting()) {
            entity.setElkaIdleQuirkVariant(QUIRK_VARIANT_SITTING);
            entity.setElkaIdleQuirkTicks(QUIRK_3_ANIMATION_TICKS);
        } else if (entity.getRandom().nextBoolean()) {
            entity.setElkaIdleQuirkVariant(QUIRK_VARIANT_SECOND);
            entity.setElkaIdleQuirkTicks(QUIRK_2_ANIMATION_TICKS);
        } else {
            entity.setElkaIdleQuirkVariant(0);
            entity.setElkaIdleQuirkTicks(QUIRK_ANIMATION_TICKS);
        }
    }

    private void startSitting(GlimmerEntity entity) {
        entity.setElkaIdleSitting(true);
        entity.setElkaIdleSitTicksRemaining(Mth.nextInt(entity.getRandom(), SIT_MIN_TICKS, SIT_MAX_TICKS));
        entity.getNavigation().stop();
        entity.setDeltaMovement(0.0D, entity.getDeltaMovement().y, 0.0D);
        entity.setElkaIdleAnimationCycleTicks(0);
        entity.setElkaIdleQuirkVariant(VARIANT_SIT_DOWN);
        entity.setElkaIdleQuirkTicks(SIT_TRANSITION_TICKS);
    }

    private void stopSitting(GlimmerEntity entity) {
        if (!entity.isElkaIdleSitting()) {
            return;
        }
        entity.setElkaIdleSitting(false);
        entity.setElkaIdleSitTicksRemaining(0);
        entity.setElkaIdleSitCooldownTicks(SIT_COOLDOWN_TICKS);
        entity.setElkaIdleAnimationCycleTicks(0);
        entity.setElkaIdleQuirkVariant(VARIANT_GET_UP);
        entity.setElkaIdleQuirkTicks(entity.isOrderedToSit() ? 0 : SIT_TRANSITION_TICKS);
    }

    private boolean canStartSitting(GlimmerEntity entity) {
        if (entity.isOrderedToSit() || !entity.onGround() || entity.isInWater() || entity.isOnFire()) {
            return false;
        }
        if (entity.getTarget() != null || entity.getNavigation().isInProgress() || entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            return false;
        }
        if (entity.isFollowingOwner() && entity.getOwner() != null && entity.distanceToSqr(entity.getOwner()) > 9.0D) {
            return false;
        }
        return true;
    }

    private boolean canRemainSitting(GlimmerEntity entity) {
        if (entity.isOrderedToSit() || !entity.onGround() || entity.isInWater() || entity.isOnFire() || entity.getTarget() != null
                || entity.getNavigation().isInProgress()) {
            return false;
        }
        if (entity.isFollowingOwner() && entity.getOwner() != null && entity.distanceToSqr(entity.getOwner()) > FOLLOW_BREAK_SIT_DISTANCE_SQR) {
            return false;
        }
        return entity.hurtTime <= 0;
    }

    private boolean isNearShade(GlimmerEntity entity) {
        BlockPos base = entity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-3, -1, -3), base.offset(3, 3, 3))) {
            if (entity.level().getBlockState(pos).is(BlockTags.LEAVES)) {
                return true;
            }
        }
        return false;
    }
}
