package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlimmerOuranwoodDeerBehavior implements GlimmerVariantBehavior {
    private static final int PASSIVE_REFRESH_DURATION = 20 * 4;
    private static final int ABILITY_COOLDOWN_TICKS = 20 * 30;
    private static final int ABILITY_SCAN_INTERVAL_TICKS = 2;
    private static final double FOLLOW_SPEED_MODIFIER = 1.75D;
    private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation CHARGE_ANIM = RawAnimation.begin()
            .thenPlay("charge_start")
            .thenPlayXTimes("charge_loop", 3)
            .thenPlay("charge_end");
    private static final double CHARGE_RANGE = 12.0D;
    private static final double CHARGE_CONE_DOT = 0.5D;
    private static final double CHARGE_HIT_RADIUS = 1.5D;
    private static final double CHARGE_SPEED = 1.4D;
    private static final double CHARGE_MAX_LANE = 18.0D;
    private static final double CHARGE_MAX_HEIGHT_DIFFERENCE = 4.0D;
    private static final double RECOVERY_DRAG = 0.5D;
    private static final int WINDUP_TICKS = 8;
    private static final int ACTIVE_TICKS = 14;
    private static final int RECOVERY_TICKS = 10;
    private static final float CHARGE_DAMAGE = 14.0F;
    private static final double CHARGE_KNOCKBACK = 1.6D;
    private static final int CHARGE_ANIM_TICKS = 33;

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(Antarchy.MODID, path);
    }

    @Override
    public void registerGoals(GlimmerEntity entity) {
        entity.addGoal(0, new FloatGoal(entity));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, 1.15D));
        entity.addGoal(6, new LookAtPlayerGoal(entity, Player.class, 8.0F));
        entity.addGoal(7, new RandomLookAroundGoal(entity));
    }

    @Override
    public ResourceLocation modelGeo() {
        return rl("geo/ouranwood_deer.geo.json");
    }

    @Override
    public ResourceLocation animationFile() {
        return rl("animations/ouranwood_deer.animation.json");
    }

    @Override
    public ResourceLocation texture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/deer_glimmer.png");
    }

    @Override
    public ResourceLocation emissiveTexture(GlimmerEntity entity) {
        return rl("textures/entity/glimmer/deer_glimmer_emissive.png");
    }

    @Override
    public SoundEvent ambientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    public SoundEvent hurtSound() {
        return SoundEvents.FOX_HURT;
    }

    @Override
    public SoundEvent deathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    public EntityDimensions adultDimensions() {
        return EntityDimensions.scalable(1.35F, 2.1F);
    }

    @Override
    public EntityDimensions babyDimensions() {
        return EntityDimensions.scalable(0.9F, 1.4F);
    }

    @Override
    public void tickPassive(GlimmerEntity entity, Player owner) {
        owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PASSIVE_REFRESH_DURATION, 0, false, false, true));
    }

    @Override
    public double followSpeedModifier() {
        return FOLLOW_SPEED_MODIFIER;
    }

    @Override
    @Nullable
    public RawAnimation movingAnimation(GlimmerEntity entity) {
        return entity.isFollowingOwner() ? RUN_ANIM : null;
    }

    @Override
    public int abilityCooldownTicks() {
        return ABILITY_COOLDOWN_TICKS;
    }

    @Override
    @Nullable
    public RawAnimation abilityAnimation() {
        return CHARGE_ANIM;
    }

    @Override
    public boolean locksMovement(GlimmerEntity entity) {
        return entity.getAbilityState(ChargeState.class) != null;
    }

    @Override
    public void tickAbilityCheck(GlimmerEntity entity, Player owner) {
        if (entity.tickCount % ABILITY_SCAN_INTERVAL_TICKS != 0 || !owner.isSprinting()) {
            return;
        }

        Vec3 look = owner.getLookAngle();
        LivingEntity bestTarget = null;
        double bestDistSq = CHARGE_RANGE * CHARGE_RANGE;
        for (LivingEntity living : owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(CHARGE_RANGE), candidate -> candidate != owner)) {
            if (!GlimmerVariantBehavior.isValidFoe(owner, living)) {
                continue;
            }
            Vec3 toTarget = living.position().subtract(owner.position());
            double distSq = toTarget.lengthSqr();
            if (distSq > bestDistSq || toTarget.normalize().dot(look) < CHARGE_CONE_DOT) {
                continue;
            }
            Vec3 lane = living.position().subtract(entity.position());
            if (lane.horizontalDistance() > CHARGE_MAX_LANE || Math.abs(lane.y) > CHARGE_MAX_HEIGHT_DIFFERENCE) {
                continue;
            }
            if (!owner.hasLineOfSight(living)) {
                continue;
            }
            bestDistSq = distSq;
            bestTarget = living;
        }

        if (bestTarget != null) {
            this.startCharge(entity, owner, bestTarget, look);
        }
    }

    private void startCharge(GlimmerEntity entity, Player owner, LivingEntity target, Vec3 fallbackDirection) {
        Vec3 lane = target.position().subtract(entity.position());
        Vec3 flat = lane.horizontalDistanceSqr() > 0.25D ? new Vec3(lane.x, 0.0D, lane.z) : new Vec3(fallbackDirection.x, 0.0D, fallbackDirection.z);
        Vec3 direction = flat.normalize();

        ChargeState state = new ChargeState();
        state.dirX = direction.x;
        state.dirZ = direction.z;
        state.yaw = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F;
        state.owner = owner;
        entity.setAbilityState(state);

        entity.faceYaw(state.yaw);
        entity.playAbilityAnimation(CHARGE_ANIM_TICKS);
        entity.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.3F);
        GlimmerParticles.abilityBurst(entity);
        this.sendAbilityMessage(owner, Component.translatable("entity.antarchy.glimmer.ability.deer"));
        entity.startAbilityCooldown();
    }

    @Override
    public void customTick(GlimmerEntity entity) {
        ChargeState state = entity.getAbilityState(ChargeState.class);
        if (state == null) {
            return;
        }

        if (!entity.isAlive() || state.owner.isRemoved()) {
            entity.setAbilityState(null);
            return;
        }

        state.ticks++;
        entity.faceYaw(state.yaw);
        Vec3 motion = entity.getDeltaMovement();
        if (state.ticks <= WINDUP_TICKS) {
            entity.setDeltaMovement(0.0D, motion.y, 0.0D);
        } else if (state.ticks <= WINDUP_TICKS + ACTIVE_TICKS) {
            entity.setDeltaMovement(state.dirX * CHARGE_SPEED, motion.y, state.dirZ * CHARGE_SPEED);
            this.hitAlongLane(entity, state);
        } else if (state.ticks <= WINDUP_TICKS + ACTIVE_TICKS + RECOVERY_TICKS) {
            entity.setDeltaMovement(motion.x * RECOVERY_DRAG, motion.y, motion.z * RECOVERY_DRAG);
        } else {
            entity.setAbilityState(null);
        }
    }

    private void hitAlongLane(GlimmerEntity entity, ChargeState state) {
        AABB hitBox = entity.getBoundingBox().inflate(CHARGE_HIT_RADIUS);
        for (LivingEntity living : entity.level().getEntitiesOfClass(LivingEntity.class, hitBox, candidate -> candidate != entity)) {
            if (!GlimmerVariantBehavior.isValidFoe(state.owner, living) || !state.alreadyHit.add(living.getUUID())) {
                continue;
            }

            GlimmerVariantBehavior.hurtAsOwner(state.owner, living, CHARGE_DAMAGE);
            Vec3 away = living.position().subtract(entity.position());
            double awayDistance = Math.max(0.1D, away.horizontalDistance());
            living.knockback(CHARGE_KNOCKBACK, -away.x / awayDistance, -away.z / awayDistance);
            GlimmerParticles.abilityBurst(living);
        }
    }

    private static final class ChargeState {
        double dirX;
        double dirZ;
        float yaw;
        int ticks;
        Player owner;
        final Set<UUID> alreadyHit = new HashSet<>();
    }
}
