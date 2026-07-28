package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class OuranwoodDeerEntity extends Animal implements GeoEntity {
    public enum Variant { BUCK, DOE }

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(OuranwoodDeerEntity.class, EntityDataSerializers.INT);
    private static final String VARIANT_KEY = "Variant";

    private static final float ADULT_HITBOX_WIDTH = 1.0F;
    private static final float ADULT_HITBOX_HEIGHT = 2.0F;
    private static final float BABY_HITBOX_WIDTH = 0.5F;
    private static final float BABY_HITBOX_HEIGHT = 1.0F;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    private static final String QUIRK_CONTROLLER = "quirk_controller";
    private static final String QUIRK_TRIGGER = "quirk";

    private static final double WALK_SPEED_THRESHOLD_SQR = 0.0009D;
    private static final double RUN_SPEED_THRESHOLD_SQR = 0.0035D;
    private static final double WALK_STROLL_SPEED = 1.0D;
    private static final double RUN_STROLL_SPEED = 1.45D;

    private static final int MIN_QUIRK_COOLDOWN = 20 * 8;
    private static final int MAX_QUIRK_COOLDOWN = 20 * 25;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int quirkCooldown = createQuirkCooldown();

    public OuranwoodDeerEntity(EntityType<? extends OuranwoodDeerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.ouranwoodDeerHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, Variant.DOE.ordinal());
    }

    public Variant getVariant() {
        return Variant.values()[Mth.clamp(this.entityData.get(VARIANT), 0, Variant.values().length - 1)];
    }

    public void setVariant(Variant variant) {
        this.entityData.set(VARIANT, variant.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.8D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, Ingredient.of(AntarchyTags.Items.OURANWOOD_DEER_FOOD), false));
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.1D, 1.5D));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(6, new OuranwoodDeerStrollGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.OURANWOOD_DEER_FOOD);
    }

    @Override
    public OuranwoodDeerEntity getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return AntarchyObjects.OURANWOOD_DEER.get().create(level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.quirkCooldown > 0) {
            this.quirkCooldown--;
        }

        if (this.level().isClientSide) {
            return;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(VARIANT_KEY, this.getVariant().name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(VARIANT_KEY)) {
            try {
                this.setVariant(Variant.valueOf(tag.getString(VARIANT_KEY)));
            } catch (IllegalArgumentException ignored) {
                this.setVariant(Variant.DOE);
            }
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        DeerSpawnData deerSpawnData = spawnGroupData instanceof DeerSpawnData data ? data : new DeerSpawnData();
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);

        if (spawnReason == MobSpawnType.NATURAL || spawnReason == MobSpawnType.CHUNK_GENERATION || spawnReason == MobSpawnType.SPAWN_EGG) {
            this.setVariant(Variant.values()[this.random.nextInt(Variant.values().length)]);
        }

        if ((spawnReason == MobSpawnType.NATURAL || spawnReason == MobSpawnType.CHUNK_GENERATION)) {
            if (deerSpawnData.adultSpawned) {
                this.setAge(-24000);
            } else {
                deerSpawnData.adultSpawned = true;
            }
        }

        return result != null ? result : deerSpawnData;
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
        if (this.isBaby()) {
            return net.minecraft.world.entity.EntityDimensions.scalable(BABY_HITBOX_WIDTH, BABY_HITBOX_HEIGHT);
        }
        return net.minecraft.world.entity.EntityDimensions.scalable(
                ADULT_HITBOX_WIDTH,
                ADULT_HITBOX_HEIGHT
        );
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        this.refreshDimensions();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
        controllers.add(new AnimationController<>(this, QUIRK_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(QUIRK_TRIGGER, RawAnimation.begin().thenPlay("quirk")));
    }

    private PlayState mainAnimController(AnimationState<OuranwoodDeerEntity> state) {
        double speedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        if (this.hurtTime > 0 || speedSqr > RUN_SPEED_THRESHOLD_SQR) {
            return state.setAndContinue(RUN_ANIM);
        }
        if (speedSqr > WALK_SPEED_THRESHOLD_SQR) {
            return state.setAndContinue(WALK_ANIM);
        }

        if (this.quirkCooldown <= 0) {
            this.quirkCooldown = this.createQuirkCooldown();
            this.triggerAnim(QUIRK_CONTROLLER, QUIRK_TRIGGER);
        }

        return state.setAndContinue(IDLE_ANIM);
    }

    private int createQuirkCooldown() {
        return MIN_QUIRK_COOLDOWN + this.random.nextInt(MAX_QUIRK_COOLDOWN - MIN_QUIRK_COOLDOWN + 1);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private static final class DeerSpawnData implements SpawnGroupData {
        private boolean adultSpawned;
    }

    private final class OuranwoodDeerStrollGoal extends Goal {
        private static final int RUN_CHANCE = 5;
        @Nullable
        private Vec3 targetPos;
        private double speedModifier;

        private OuranwoodDeerStrollGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (OuranwoodDeerEntity.this.isVehicle() || !OuranwoodDeerEntity.this.getNavigation().isDone()) {
                return false;
            }
            if (OuranwoodDeerEntity.this.getRandom().nextInt(reducedTickDelay(60)) != 0) {
                return false;
            }

            this.targetPos = DefaultRandomPos.getPos(OuranwoodDeerEntity.this, 10, 7);
            if (this.targetPos == null) {
                return false;
            }

            this.speedModifier = this.pickSpeed();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPos != null && !OuranwoodDeerEntity.this.getNavigation().isDone();
        }

        @Override
        public void start() {
            if (this.targetPos != null) {
                OuranwoodDeerEntity.this.getNavigation().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.targetPos = null;
        }

        private double pickSpeed() {
            if (OuranwoodDeerEntity.this.hurtTime > 0) {
                return RUN_STROLL_SPEED;
            }
            return OuranwoodDeerEntity.this.random.nextInt(RUN_CHANCE) == 0 ? RUN_STROLL_SPEED : WALK_STROLL_SPEED;
        }
    }
}
