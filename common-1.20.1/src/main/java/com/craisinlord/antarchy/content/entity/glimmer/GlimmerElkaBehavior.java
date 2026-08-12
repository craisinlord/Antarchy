package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.bloodglass.BloodglassAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.RawAnimation;

public class GlimmerElkaBehavior implements GlimmerVariantBehavior {
    private static final int PASSIVE_REFRESH_DURATION = 20 * 4;
    private static final int WARD_AMPLIFIER = 1;
    private static final int ABILITY_COOLDOWN_TICKS = 20 * 60;
    private static final int CRY_ANIM_TICKS = 40;
    private static final double SHOCKWAVE_RADIUS = 6.0D;
    private static final double SHOCKWAVE_KNOCKBACK = 1.3D;
    private static final float SHOCKWAVE_DAMAGE = 8.0F;
    private static final int IDLE_ANIMATION_CYCLE_TICKS = 60;
    private static final int QUIRK_ANIMATION_TICKS = 40;
    private static final int QUIRK_CHANCE = 60;
    private static final int SIT_MIN_TICKS = 20 * 60;
    private static final int SIT_MAX_TICKS = 20 * 60 * 3;
    private static final int SIT_COOLDOWN_TICKS = 20 * 60 * 6;
    private static final int SIT_CHECK_CHANCE_SHADE = 300;
    private static final int SIT_CHECK_CHANCE_OPEN = 1200;
    private static final double FOLLOW_BREAK_SIT_DISTANCE_SQR = 81.0D;
    private static final RawAnimation ELKA_WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ELKA_IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ELKA_SIT_IDLE_ANIM = RawAnimation.begin().thenLoop("sit_idle");
    private static final RawAnimation ELKA_QUIRK_ANIM = RawAnimation.begin().thenPlay("quirk");
    private static final RawAnimation ELKA_QUIRK_2_ANIM = RawAnimation.begin().thenPlay("quirk_2");
    private static final RawAnimation ELKA_QUIRK_3_ANIM = RawAnimation.begin().thenPlay("quirk_3");

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(Antarchy.MODID, path);
    }

    @Override
    public void registerGoals(GlimmerEntity entity) {
        entity.addGoal(0, new FloatGoal(entity));
    }

    @Override
    public ResourceLocation modelGeo() {
        return rl("geo/elka.geo.json");
    }

    @Override
    public ResourceLocation animationFile() {
        return rl("animations/elka.animation.json");
    }

    @Override
    public ResourceLocation texture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/elka_glimmer.png");
    }

    @Override
    public ResourceLocation emissiveTexture(GlimmerEntity entity) {
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
    public void tickPassiveEveryTick(GlimmerEntity entity, Player owner) {
        this.tickIdleState(entity);

        if (!(owner instanceof BloodglassAccess access)) {
            return;
        }

        long lostNow = access.antarchy$getArmorShieldLostCount() + access.antarchy$getAppleShieldLostCount();
        long lastLost = entity.getLastOwnerShieldLostCount();
        if (lastLost >= 0L && lostNow > lastLost && !entity.isAbilityOnCooldown()) {
            this.shatterWard(entity, owner);
        }
        entity.setLastOwnerShieldLostCount(lostNow);
    }

    @Override
    public void clearPassive(GlimmerEntity entity, Player owner) {
        entity.setLastOwnerShieldLostCount(-1L);
        this.stopSitting(entity);
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
                case 1 -> ELKA_QUIRK_2_ANIM;
                case 2 -> ELKA_QUIRK_3_ANIM;
                default -> ELKA_QUIRK_ANIM;
            };
        }
        if (entity.isElkaIdleSitting()) {
            return ELKA_SIT_IDLE_ANIM;
        }
        return ELKA_IDLE_ANIM;
    }

    @Override
    @Nullable
    public software.bernie.geckolib.core.animation.RawAnimation abilityAnimation() {
        return software.bernie.geckolib.core.animation.RawAnimation.begin().thenPlay("cry");
    }

    private void shatterWard(GlimmerEntity entity, Player owner) {
        entity.playAbilityAnimation(CRY_ANIM_TICKS);
        entity.playSound(SoundEvents.PANDA_CANT_BREED, 1.2F, 0.7F);
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, entity.getSoundSource(), 1.0F, 0.6F);

        AABB area = owner.getBoundingBox().inflate(SHOCKWAVE_RADIUS);
        for (Entity nearby : owner.level().getEntities(owner, area)) {
            if (nearby instanceof Projectile projectile) {
                Vec3 away = projectile.position().subtract(owner.position()).normalize();
                projectile.setDeltaMovement(away.scale(1.5D));
                continue;
            }

            if (nearby instanceof LivingEntity living && (nearby instanceof Enemy || living.getLastHurtByMob() == owner)) {
                if (nearby instanceof Enemy) {
                    living.hurt(owner.damageSources().mobAttack(entity), SHOCKWAVE_DAMAGE);
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
                }
            }
            return;
        }

        if (entity.getElkaIdleSitCooldownTicks() > 0) {
            entity.setElkaIdleSitCooldownTicks(entity.getElkaIdleSitCooldownTicks() - 1);
        }

        if (this.canStartSitting(entity)) {
            int chance = this.isNearShade(entity) ? SIT_CHECK_CHANCE_SHADE : SIT_CHECK_CHANCE_OPEN;
            if (entity.getElkaIdleSitCooldownTicks() <= 0 && entity.getRandom().nextInt(chance) == 0) {
                this.startSitting(entity);
                return;
            }
        }

        this.tickIdleQuirk(entity);
    }

    private void tickIdleQuirk(GlimmerEntity entity) {
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

        entity.setElkaIdleQuirkTicks(QUIRK_ANIMATION_TICKS);
        if (entity.isElkaIdleSitting()) {
            entity.setElkaIdleQuirkVariant(2);
        } else {
            entity.setElkaIdleQuirkVariant(entity.getRandom().nextBoolean() ? 0 : 1);
        }
    }

    private void startSitting(GlimmerEntity entity) {
        entity.setElkaIdleSitting(true);
        entity.setElkaIdleSitTicksRemaining(Mth.nextInt(entity.getRandom(), SIT_MIN_TICKS, SIT_MAX_TICKS));
        entity.getNavigation().stop();
        entity.setDeltaMovement(0.0D, entity.getDeltaMovement().y, 0.0D);
        entity.setElkaIdleAnimationCycleTicks(0);
    }

    private void stopSitting(GlimmerEntity entity) {
        if (!entity.isElkaIdleSitting()) {
            return;
        }
        entity.setElkaIdleSitting(false);
        entity.setElkaIdleSitTicksRemaining(0);
        entity.setElkaIdleSitCooldownTicks(SIT_COOLDOWN_TICKS);
        entity.setElkaIdleAnimationCycleTicks(0);
    }

    private boolean canStartSitting(GlimmerEntity entity) {
        if (entity.isOrderedToSit() || !entity.onGround() || entity.isInWaterOrBubble() || entity.isOnFire()) {
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
        if (entity.isOrderedToSit() || !entity.onGround() || entity.isInWaterOrBubble() || entity.isOnFire() || entity.getTarget() != null) {
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
