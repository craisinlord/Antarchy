package com.craisinlord.antarchy.content.entity.kraken;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class TentacleEntity extends Mob implements GeoEntity {
    private static final RawAnimation GRASP_ANIM = RawAnimation.begin().thenLoop("animation");
    private static final float SPAWN_SOUND_VOLUME = 0.35F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int lifetimeTicks;
    private UUID ownerId;

    public TentacleEntity(EntityType<? extends TentacleEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoAi(true);
        this.noPhysics = true;
        this.noCulling = true;
        this.xpReward = 0;
        this.setInvulnerable(true);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    public void push(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public static void spawnAt(ServerLevel level, Vec3 pos, LivingEntity owner) {
        TentacleEntity tentacle = new TentacleEntity(com.craisinlord.antarchy.content.AntarchyObjects.TENTACLE.get(), level);
        tentacle.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        if (owner != null) {
            tentacle.ownerId = owner.getUUID();
        }
        level.addFreshEntity(tentacle);
        level.playSound(null, pos.x, pos.y, pos.z, AntarchySoundEvents.KRAKEN_ATTACK.get(), SoundSource.HOSTILE, SPAWN_SOUND_VOLUME, 1.0F);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this, level);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        this.lifetimeTicks++;
        if (this.lifetimeTicks >= AntarchySettings.krakensGraspTentacleDurationTicks()) {
            this.discard();
            return;
        }

        int interval = Math.max(1, AntarchySettings.krakensGraspTentacleDamageIntervalTicks());
        if (this.lifetimeTicks % interval == 0) {
            this.grabNearbyEntities();
        }
    }

    private void grabNearbyEntities() {
        double radius = AntarchySettings.krakensGraspTentacleRadius();
        AABB area = this.getBoundingBox().inflate(radius, radius * 0.6D, radius);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && !entity.isSpectator() && !entity.getUUID().equals(this.ownerId)
                        && !(entity instanceof Player player && player.isCreative()));

        for (LivingEntity victim : victims) {
            if (this.level() instanceof ServerLevel serverLevel) {
                victim.hurt(this.damageSources().mobAttack(this), (float) AntarchySettings.krakensGraspTentacleDamage());
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,
                        victim.getX(), victim.getY(0.6D), victim.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
            }

            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, AntarchySettings.krakensGraspTentacleDamageIntervalTicks() + 10, 1, false, true, true));

            Vec3 pull = this.position().subtract(victim.position()).multiply(1.0D, 0.0D, 1.0D);
            if (pull.lengthSqr() > 1.0E-4D) {
                victim.setDeltaMovement(victim.getDeltaMovement().add(pull.normalize().scale(0.12D)));
                victim.hurtMarked = true;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "grasp_controller", 0, this::graspAnimController));
    }

    private PlayState graspAnimController(AnimationState<TentacleEntity> state) {
        return state.setAndContinue(GRASP_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
