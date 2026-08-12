package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Locale;
import java.util.Objects;
import java.util.function.IntFunction;

public class JerryEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> STAGE =
            SynchedEntityData.defineId(JerryEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(JerryEntity.class, EntityDataSerializers.INT);

    private static final String STAGE_KEY = "JerryStage";
    private static final String STAGE_AGE_KEY = "JerryStageAge";
    private static final int GROWTH_TICKS = 24000;
    private static final int ATTACH_DAMAGE_INTERVAL = 20;
    private static final int ATTACK_ANIM_DURATION = 16;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLOAT_ANIM = RawAnimation.begin().thenLoop("float");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int stageAgeTicks;
    private int attachDamageTicks;
    private int attackCooldownTicks;
    private int alphaDiveCooldownTicks;
    private boolean attachedToTarget;

    public JerryEntity(EntityType<? extends JerryEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.jerryInfantHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.jerryInfantAttackDamage())
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.FLYING_SPEED, 0.45D);
    }

    public static boolean canSpawn(EntityType<JerryEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random)
                && level.getMaxLocalRawBrightness(pos) <= 2;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STAGE, Stage.INFANT.id());
        this.entityData.define(ATTACK_ANIM_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new GammaMeleeGoal());
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData, @org.jetbrains.annotations.Nullable net.minecraft.nbt.CompoundTag dataTag) {
        if (spawnReason == MobSpawnType.SPAWN_EGG) {
            this.setStage(this.random.nextBoolean() ? Stage.ALPHA : Stage.GAMMA);
        }
        this.applyStageAttributes(this.getStage(), true);
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData, dataTag);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getStage() == Stage.GAMMA) {
            this.setNoGravity(false);
        } else {
            this.setNoGravity(true);
            this.fallDistance = 0.0F;
        }

        int attackTicks = this.entityData.get(ATTACK_ANIM_TICKS);
        if (attackTicks > 0) {
            this.setAttackAnimTicks(attackTicks - 1);
        }

        if (this.level().isClientSide) {
            return;
        }

        this.tickGrowth();
        if (this.attackCooldownTicks > 0) {
            this.attackCooldownTicks--;
        }
        if (this.alphaDiveCooldownTicks > 0) {
            this.alphaDiveCooldownTicks--;
        }

        Stage stage = this.getStage();
        if (stage == Stage.INFANT || stage == Stage.MATURE) {
            this.tickAttachedForm(stage);
        } else if (stage == Stage.ALPHA) {
            this.tickAlpha();
        }
    }

    private void tickGrowth() {
        Stage stage = this.getStage();
        if (stage == Stage.GAMMA) {
            return;
        }
        this.stageAgeTicks += this.growthTickAmount();
        if (this.stageAgeTicks < GROWTH_TICKS) {
            return;
        }
        this.stageAgeTicks = 0;
        this.setStage(switch (stage) {
            case INFANT -> Stage.MATURE;
            case MATURE -> Stage.ALPHA;
            case ALPHA -> Stage.GAMMA;
            case GAMMA -> Stage.GAMMA;
        });
    }

    private int growthTickAmount() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return 1;
        }

        int hordeLevel = 0;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.isSpectator() || player.distanceToSqr(this) > 96.0D * 96.0D) {
                continue;
            }
            hordeLevel = Math.max(hordeLevel, CavarynHordeManager.getHordeLevel(player));
        }

        float intensity = Mth.clamp(hordeLevel / (float) CavarynHordeManager.maxHordeLevel(), 0.0F, 1.0F);
        return 1 + Math.round(intensity * intensity * 99.0F);
    }

    private void tickAttachedForm(Stage stage) {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.attachedToTarget = false;
            this.drift();
            return;
        }

        Vec3 targetPos = target.getEyePosition().add(0.0D, -0.15D, 0.0D);
        Vec3 toTarget = targetPos.subtract(this.position());
        double distance = toTarget.length();
        if (distance > 0.01D) {
            Vec3 motion = toTarget.normalize().scale(stage == Stage.INFANT ? 0.16D : 0.19D);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.55D).add(motion));
        }
        if (distance < 1.15D) {
            this.attachedToTarget = true;
            this.setPos(targetPos.x, targetPos.y, targetPos.z);
            this.setDeltaMovement(Vec3.ZERO);
            if (++this.attachDamageTicks >= ATTACH_DAMAGE_INTERVAL) {
                this.attachDamageTicks = 0;
                Vec3 targetMotion = target.getDeltaMovement();
                this.playSound(this.attackSoundForCurrentStage(), 0.85F, this.voicePitch());
                target.hurt(this.damageSources().mobAttack(this), (float) stage.attackDamage());
                target.setDeltaMovement(targetMotion);
                target.hurtMarked = true;
            }
        } else {
            this.attachedToTarget = false;
            this.attachDamageTicks = 0;
        }
    }

    private void tickAlpha() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.drift();
            return;
        }

        Stage stage = this.getStage();
        Vec3 targetPos = target.getEyePosition();
        Vec3 hoverPos = targetPos.add(0.0D, 5.0D, 0.0D);
        Vec3 toHover = hoverPos.subtract(this.position());
        Vec3 toTarget = targetPos.subtract(this.position());
        double hoverDistance = toHover.length();
        double targetDistance = toTarget.length();
        double horizontalDistance = this.position().subtract(targetPos).horizontalDistance();
        this.faceTarget(target);
        if (this.alphaDiveCooldownTicks > 0) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D).add(toTarget.normalize().scale(0.28D)).add(0.0D, -0.08D, 0.0D));
        } else if (hoverDistance > 0.01D) {
            Vec3 desiredMotion = toHover.normalize().scale(0.20D);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.72D).add(desiredMotion));
        }

        if (this.alphaDiveCooldownTicks <= 0 && targetDistance < 9.0D && horizontalDistance < 7.0D) {
            this.alphaDiveCooldownTicks = 30;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.55D, 0.0D).add(toTarget.normalize().scale(0.42D)));
        }

        if (targetDistance < 2.3D && this.attackCooldownTicks <= 0) {
            this.faceTarget(target);
            this.attackCooldownTicks = 24;
            this.setAttackAnimTicks(ATTACK_ANIM_DURATION);
            this.playSound(this.attackSoundForCurrentStage(), 1.0F, this.voicePitch());
            target.hurt(this.damageSources().mobAttack(this), (float) stage.attackDamage());
        }
    }

    private void faceTarget(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(this.position());
        if (toTarget.horizontalDistanceSqr() <= 0.0001D) {
            return;
        }
        float targetYaw = (float) (Mth.atan2(toTarget.z, toTarget.x) * (180.0F / Math.PI)) - 90.0F;
        this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, 30.0F));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private void drift() {
        if (this.tickCount % 45 == 0) {
            this.setDeltaMovement(
                    (this.random.nextDouble() - 0.5D) * 0.12D,
                    (this.random.nextDouble() - 0.5D) * 0.08D,
                    (this.random.nextDouble() - 0.5D) * 0.12D
            );
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.setAttackAnimTicks(ATTACK_ANIM_DURATION);
        if (target instanceof LivingEntity living) {
            this.faceTarget(living);
        }
        this.playSound(this.attackSoundForCurrentStage(), 1.0F, this.voicePitch());
        return target instanceof LivingEntity living
                ? living.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))
                : super.doHurtTarget(target);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.isAdultStage() ? AntarchySoundEvents.JERRY_ADULT_IDLE.get() : AntarchySoundEvents.JERRY_YOUNG_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.isAdultStage() ? AntarchySoundEvents.JERRY_ADULT_HURT.get() : AntarchySoundEvents.JERRY_YOUNG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isAdultStage() ? AntarchySoundEvents.JERRY_ADULT_DEATH.get() : AntarchySoundEvents.JERRY_YOUNG_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return this.isAdultStage() ? 1.0F : 0.8F;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(BuiltInRegistries.ITEM.get(new ResourceLocation(com.craisinlord.antarchy.Antarchy.MODID, "jerry_nucleus")))) {
            if (!this.level().isClientSide) {
                Stage nextStage = this.nextGrowthStage();
                if (nextStage != this.getStage()) {
                    this.setStage(nextStage);
                    this.stageAgeTicks = 0;
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (stack.getItem() instanceof SpawnEggItem spawnEggItem && spawnEggItem.getType(stack.getTag()) == this.getType()) {
            if (this.level() instanceof ServerLevel serverLevel && this.getType().create(serverLevel) instanceof JerryEntity infant) {
                infant.setStage(this.isAdultStage() ? Stage.INFANT : this.randomAdultStage());
                infant.moveTo(this.getX(), this.getY(), this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
                infant.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(infant.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                serverLevel.addFreshEntity(infant);
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
        controllers.add(new AnimationController<>(this, "main_controller", 3, this::mainAnimController)
                .triggerableAnim("attack", ATTACK_ANIM));
        controllers.add(new AnimationController<>(this, "attack_controller", 0, this::attackAnimController)
                .triggerableAnim("attack", ATTACK_ANIM));
    }

    private PlayState mainAnimController(AnimationState<JerryEntity> state) {
        Stage stage = this.getStage();
        if (stage == Stage.GAMMA) {
            return state.setAndContinue(state.isMoving() ? WALK_ANIM : IDLE_ANIM);
        }
        if (stage == Stage.ALPHA && this.entityData.get(ATTACK_ANIM_TICKS) > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        if (stage == Stage.ALPHA) {
            return state.setAndContinue(state.isMoving() ? FLOAT_ANIM : IDLE_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    private PlayState attackAnimController(AnimationState<JerryEntity> state) {
        if (this.getStage() == Stage.GAMMA && this.entityData.get(ATTACK_ANIM_TICKS) > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return !this.isAttachedYoungStage() && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (this.isAttachedYoungStage()) {
            return;
        }
        super.push(entity);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isAttachedYoungStage() && super.canBeCollidedWith();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, net.minecraft.core.BlockPos pos) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(STAGE_KEY, this.getStage().getSerializedName());
        tag.putInt(STAGE_AGE_KEY, this.stageAgeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setStage(Stage.byName(tag.getString(STAGE_KEY)));
        this.stageAgeTicks = tag.getInt(STAGE_AGE_KEY);
        this.applyStageAttributes(this.getStage(), false);
    }

    public Stage getStage() {
        return Stage.byId(this.entityData.get(STAGE));
    }

    public void setStage(Stage stage) {
        Stage previous = this.getStage();
        this.entityData.set(STAGE, stage.id());
        this.applyStageAttributes(stage, true);
        if (previous != stage) {
            this.stageAgeTicks = 0;
            this.refreshDimensions();
        }
    }

    private void setAttackAnimTicks(int ticks) {
        int clamped = Math.max(0, ticks);
        int previous = this.entityData.get(ATTACK_ANIM_TICKS);
        this.entityData.set(ATTACK_ANIM_TICKS, clamped);
        if (previous <= 0 && clamped > 0) {
            this.triggerAnim("main_controller", "attack");
            this.triggerAnim("attack_controller", "attack");
        }
    }

    private void applyStageAttributes(Stage stage, boolean fillIfNew) {
        double oldMaxHealth = this.getMaxHealth();
        float healthRatio = oldMaxHealth > 0.0D ? this.getHealth() / (float) oldMaxHealth : 1.0F;
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(stage.maxHealth());
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(stage.attackDamage());
        if (fillIfNew && this.getHealth() <= 0.0F) {
            this.setHealth(this.getMaxHealth());
        } else {
            this.setHealth(Mth.clamp((float) (this.getMaxHealth() * healthRatio), 1.0F, this.getMaxHealth()));
        }
    }

    private boolean isAdultStage() {
        Stage stage = this.getStage();
        return stage == Stage.ALPHA || stage == Stage.GAMMA;
    }

    private SoundEvent attackSoundForCurrentStage() {
        return this.isAdultStage() ? AntarchySoundEvents.JERRY_ADULT_ATTACK.get() : AntarchySoundEvents.JERRY_YOUNG_ATTACK.get();
    }

    private float voicePitch() {
        return this.isAdultStage() ? 0.9F + this.random.nextFloat() * 0.2F : 1.1F + this.random.nextFloat() * 0.25F;
    }

    private Stage nextGrowthStage() {
        return switch (this.getStage()) {
            case INFANT -> Stage.MATURE;
            case MATURE -> Stage.ALPHA;
            case ALPHA -> Stage.GAMMA;
            case GAMMA -> Stage.GAMMA;
        };
    }

    private Stage randomAdultStage() {
        return this.random.nextBoolean() ? Stage.ALPHA : Stage.GAMMA;
    }

    private boolean isAttachedYoungStage() {
        Stage stage = this.getStage();
        return this.attachedToTarget && (stage == Stage.INFANT || stage == Stage.MATURE);
    }

    private class GammaMeleeGoal extends MeleeAttackGoal {
        GammaMeleeGoal() {
            super(JerryEntity.this, 1.35D, false);
        }

        @Override
        public boolean canUse() {
            return JerryEntity.this.getStage() == Stage.GAMMA && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return JerryEntity.this.getStage() == Stage.GAMMA && super.canContinueToUse();
        }
    }

    public enum Stage implements StringRepresentable {
        INFANT(0, "infant"),
        MATURE(1, "mature"),
        ALPHA(2, "alpha"),
        GAMMA(3, "gamma");

        private static final IntFunction<Stage> BY_ID = ByIdMap.continuous(Stage::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        private final int id;
        private final String name;

        Stage(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int id() {
            return this.id;
        }

        public double maxHealth() {
            return switch (this) {
                case INFANT -> AntarchySettings.jerryInfantHealth();
                case MATURE -> AntarchySettings.jerryMatureHealth();
                case ALPHA -> AntarchySettings.jerryAlphaHealth();
                case GAMMA -> AntarchySettings.jerryGammaHealth();
            };
        }

        public double attackDamage() {
            return switch (this) {
                case INFANT -> AntarchySettings.jerryInfantAttackDamage();
                case MATURE -> AntarchySettings.jerryMatureAttackDamage();
                case ALPHA -> AntarchySettings.jerryAlphaAttackDamage();
                case GAMMA -> AntarchySettings.jerryGammaAttackDamage();
            };
        }

        public static Stage byId(int id) {
            return BY_ID.apply(id);
        }

        public static Stage byName(String name) {
            for (Stage stage : values()) {
                if (stage.name.equals(name.toLowerCase(Locale.ROOT))) {
                    return stage;
                }
            }
            return INFANT;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
