package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CreepingHorrorEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> CLIMBING =
            SynchedEntityData.defineId(JumpyBugEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("attack");
    private static final byte ATTACK_ANIM_EVENT = 4;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int attackAnimTicks = 0;

    public CreepingHorrorEntity(EntityType<? extends CreepingHorrorEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public float maxUpStep() {
        return 1.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.creepingHorrorHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.creepingHorrorAttackDamage())
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean onClimbable() {
        return this.horizontalCollision;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 3, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<CreepingHorrorEntity> state) {
        if (attackAnimTicks > 0) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result) {
            attackAnimTicks = 20;
            this.level().broadcastEntityEvent(this, ATTACK_ANIM_EVENT);
        }
        return result;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == ATTACK_ANIM_EVENT) {
            attackAnimTicks = 20;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();
        if (attackAnimTicks > 0) attackAnimTicks--;
    }

    //TODO IT NEVER ACTUALLY SETS TO CLIMBING
    public boolean isClimbing() {
        return this.entityData.get(CLIMBING);
    }
}
