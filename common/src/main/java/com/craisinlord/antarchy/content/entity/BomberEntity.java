package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import software.bernie.geckolib.util.GeckoLibUtil;

public class BomberEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> FUSE_TICKS_DATA =
            SynchedEntityData.defineId(BomberEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DETONATING_DATA =
            SynchedEntityData.defineId(BomberEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int DEFAULT_FUSE_TICKS = 80;
    private static final int EXPLODE_ANIM_START_TICKS = 25;
    private static final int FLASH_INTERVAL_TICKS = 5;

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
                && level.isEmptyBlock(pos.above())
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
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return super.doHurtTarget(target);
        }

        livingTarget.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        this.primeFuse();
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            this.primeFuse();
            return false;
        }
        if (this.detonating) {
            Entity attacker = source.getDirectEntity();
            if (attacker != null) {
                Vec3 knockDir = this.position().subtract(attacker.position()).normalize();
                this.push(knockDir.x * 0.5, 0.2, knockDir.z * 0.5);
                this.hurtMarked = true;
            }
            return false;
        }

        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof AbstractArrow arrow && arrow.isOnFire()) {
            this.primeFuse();
        }

        return super.hurt(source, amount);
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
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.detonating) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            this.hasImpulse = false;

            if (this.fuseTicks > 0) {
                this.fuseTicks--;
                this.entityData.set(FUSE_TICKS_DATA, this.fuseTicks);
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

    private PlayState mainAnimController(AnimationState<BomberEntity> state) {
        if (this.isDetonating() && this.getFuseTicks() <= EXPLODE_ANIM_START_TICKS) {
            return state.setAndContinue(EXPLODE_ANIM);
        }

        return state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
    }

    private void primeFuse() {
        if (this.detonating) {
            return;
        }

        this.detonating = true;
        this.fuseTicks = DEFAULT_FUSE_TICKS;
        this.entityData.set(DETONATING_DATA, true);
        this.entityData.set(FUSE_TICKS_DATA, this.fuseTicks);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = false;
        this.playSound(SoundEvents.TNT_PRIMED, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    private void detonate() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        double x = this.getX();
        double y = this.getY(0.0625D);
        double z = this.getZ();
        float radius = (float) AntarchySettings.bomberExplosionRadius();
        serverLevel.explode(this, x, y, z, radius, ExplosionInteraction.TNT);

        double extraDamage = AntarchySettings.bomberExplosionDamage();
        if (extraDamage > 0.0D) {
            AABB damageBox = this.getBoundingBox().inflate(radius * 2.0D);
            for (LivingEntity livingEntity : serverLevel.getEntitiesOfClass(LivingEntity.class, damageBox, entity -> entity.isAlive() && entity != this)) {
                double distance = Math.sqrt(this.distanceToSqr(livingEntity));
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

    private final class BomberAttackGoal extends MeleeAttackGoal {
        private BomberAttackGoal() {
            super(BomberEntity.this, 1.1D, true);
        }

        @Override
        public boolean canUse() {
            return !BomberEntity.this.detonating && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !BomberEntity.this.detonating && super.canContinueToUse();
        }
    }
}
