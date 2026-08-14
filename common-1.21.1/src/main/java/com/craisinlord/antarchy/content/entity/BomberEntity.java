package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BomberEntity extends Monster implements GeoEntity {
    private static final ResourceLocation INTENTIONALLY_EMPTY_SOUND_ID = ResourceLocation.withDefaultNamespace("intentionally_empty");
    private static final EntityDataAccessor<Integer> FUSE_TICKS_DATA =
            SynchedEntityData.defineId(BomberEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DETONATING_DATA =
            SynchedEntityData.defineId(BomberEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int DEFAULT_FUSE_TICKS = 120;
    private static final int EXPLODE_ANIM_START_TICKS = 25;
    private static final int FLASH_INTERVAL_TICKS = 5;
    private static final double KNOCKBACK_MULTIPLIER = 5.5D;
    private static final float EXPLOSION_RADIUS_MULTIPLIER = 0.45F;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation EXPLODE_ANIM = RawAnimation.begin().thenPlay("explode");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int fuseTicks;
    private boolean detonating;

    public BomberEntity(EntityType<? extends BomberEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.bomberHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.bomberAttackDamage())
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean canSpawn(EntityType<BomberEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.isEmptyBlock(pos)
                && (level.isEmptyBlock(pos.above()) || level.isEmptyBlock(pos.below()))
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FUSE_TICKS_DATA, 0);
        builder.define(DETONATING_DATA, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BomberAttackGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        double maxHealth = AntarchySettings.bomberHealth();
        double attackDamage = AntarchySettings.bomberAttackDamage();
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);
        this.setHealth((float) maxHealth);
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(attackDamage);
        SpawnGroupData spawnData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.primeFuse();
        return spawnData;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity)) {
            return super.doHurtTarget(target);
        }

        this.primeFuse();
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            this.primeFuse();
        }

        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof AbstractArrow arrow && arrow.isOnFire()) {
            this.primeFuse();
        }

        this.playSound(AntarchySoundEvents.BOMBER_KNOCK.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
        this.pushAwayFrom(source);
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength * KNOCKBACK_MULTIPLIER, x, z);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.BOMBER_KNOCK.get();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.detonating) {
            return InteractionResult.PASS;
        }

        if (stack.is(Items.FLINT_AND_STEEL)) {
            if (!this.level().isClientSide) {
                this.primeFuse();
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.is(Items.FIRE_CHARGE)) {
            if (!this.level().isClientSide) {
                this.primeFuse();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement_controller", 0, this::movementAnimController)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
        controllers.add(new AnimationController<>(this, "explode_controller", 0, this::explodeAnimController)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>())
                .triggerableAnim("explode", EXPLODE_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.detonating && this.fuseTicks <= EXPLODE_ANIM_START_TICKS) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }

        if (!this.level().isClientSide && this.detonating) {
            if (this.fuseTicks > 0) {
                this.fuseTicks--;
                this.entityData.set(FUSE_TICKS_DATA, this.fuseTicks);
                if (this.fuseTicks == EXPLODE_ANIM_START_TICKS) {
                    this.triggerAnim("explode_controller", "explode");
                }
            }

            if (this.fuseTicks <= 0) {
                this.detonate();
            }
        }
    }

    public boolean isDetonating() {
        return this.entityData.get(DETONATING_DATA);
    }

    public int getFuseTicks() {
        return this.entityData.get(FUSE_TICKS_DATA);
    }

    public boolean isFlashing() {
        return this.isDetonating()
                && this.getFuseTicks() > 0
                && (this.getFuseTicks() / FLASH_INTERVAL_TICKS) % 2 == 0;
    }

    private PlayState movementAnimController(AnimationState<BomberEntity> state) {
        if (this.isDetonating() && this.getFuseTicks() <= EXPLODE_ANIM_START_TICKS) {
            return PlayState.STOP;
        }

        return state.setAndContinue(this.isMovingForAnimation() ? WALK_ANIM : IDLE_ANIM);
    }

    private PlayState explodeAnimController(AnimationState<BomberEntity> state) {
        if (this.isDetonating() && this.getFuseTicks() <= EXPLODE_ANIM_START_TICKS) {
            return state.setAndContinue(EXPLODE_ANIM);
        }

        return PlayState.STOP;
    }

    public void primeFuse() {
        if (this.detonating) {
            return;
        }

        this.detonating = true;
        this.fuseTicks = DEFAULT_FUSE_TICKS;
        this.entityData.set(DETONATING_DATA, true);
        this.entityData.set(FUSE_TICKS_DATA, this.fuseTicks);
        this.playSound(AntarchySoundEvents.BOMBER_EXPLODE.get(), 1.0F, 1.0F);
    }

    private void detonate() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        Vec3 explosionCenter = this.position().add(this.toWorld(0.0D, this.getBbHeight() * 0.0625D, 0.0D));
        double x = explosionCenter.x;
        double y = explosionCenter.y;
        double z = explosionCenter.z;
        float radius = (float) (AntarchySettings.bomberExplosionRadius() * EXPLOSION_RADIUS_MULTIPLIER);
        serverLevel.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.55F, 1.0F);
        serverLevel.explode(
                this,
                null,
                null,
                x,
                y,
                z,
                radius,
                false,
                ExplosionInteraction.TNT,
                ParticleTypes.EXPLOSION_EMITTER,
                ParticleTypes.EXPLOSION,
                BuiltInRegistries.SOUND_EVENT.getHolder(INTENTIONALLY_EMPTY_SOUND_ID).orElseThrow()
        );

        double extraDamage = AntarchySettings.bomberExplosionDamage();
        if (extraDamage > 0.0D) {
            AABB damageBox = this.getBoundingBox().inflate(radius * 2.0D);
            for (LivingEntity livingEntity : serverLevel.getEntitiesOfClass(LivingEntity.class, damageBox, entity -> entity.isAlive() && entity != this)) {
                double distance = explosionCenter.distanceTo(livingEntity.position());
                double falloff = radius <= 0.0F ? 0.0D : Math.max(0.0D, 1.0D - distance / radius);
                if (falloff <= 0.0D) {
                    continue;
                }

                livingEntity.hurt(this.level().damageSources().generic(), (float) (extraDamage * falloff));
            }
        }

        this.detonating = false;
        this.fuseTicks = 0;
        this.entityData.set(DETONATING_DATA, false);
        this.entityData.set(FUSE_TICKS_DATA, 0);
        this.discard();
    }

    private void pushAwayFrom(DamageSource source) {
        Entity sourceEntity = source.getDirectEntity();
        if (sourceEntity == null) {
            sourceEntity = source.getEntity();
        }
        if (sourceEntity == null || sourceEntity == this) {
            return;
        }

        Vec3 localAway = this.toLocalPlane(this.position().subtract(sourceEntity.position()));
        double distance = localAway.length();
        if (distance < 1.0E-4D) {
            return;
        }

        double strength = 0.4D * KNOCKBACK_MULTIPLIER;
        Vec3 localImpulse = localAway.scale(strength / distance);
        Vec3 worldImpulse = this.toWorld(localImpulse.x, 0.08D, localImpulse.z);
        this.push(worldImpulse.x, worldImpulse.y, worldImpulse.z);
    }

    private boolean isMovingForAnimation() {
        return this.toLocal(this.getDeltaMovement()).horizontalDistanceSqr() > 1.0E-4D || this.getNavigation().isInProgress();
    }

    private AntarchyGravityDirection gravityDirection() {
        return AntarchyGravityApi.getGravityDirection(this);
    }

    private Vec3 toLocal(Vec3 worldVector) {
        return AntarchyGravityRotationUtil.vecWorldToPlayer(worldVector, this.gravityDirection());
    }

    private Vec3 toWorld(double x, double y, double z) {
        return AntarchyGravityRotationUtil.vecPlayerToWorld(x, y, z, this.gravityDirection());
    }

    private Vec3 toLocalPlane(Vec3 worldVector) {
        Vec3 local = this.toLocal(worldVector);
        return new Vec3(local.x, 0.0D, local.z);
    }

    private final class BomberAttackGoal extends MeleeAttackGoal {
        private BomberAttackGoal() {
            super(BomberEntity.this, 1.1D, true);
        }

        @Override
        public boolean canUse() {
            return !BomberEntity.this.isDetonating() || BomberEntity.this.getFuseTicks() > EXPLODE_ANIM_START_TICKS && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !BomberEntity.this.isDetonating() || BomberEntity.this.getFuseTicks() > EXPLODE_ANIM_START_TICKS && super.canContinueToUse();
        }
    }
}
