package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.effect.StinkyBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StinkBugEntity extends Animal implements GeoEntity {
    private static final String MAIN_CONTROLLER = "main_controller";
    private static final String FART_CONTROLLER = "fart_controller";
    private static final String FART_TRIGGER = "fart";
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FART_ANIM = RawAnimation.begin().thenPlay("fart");
    private static final int FART_ANIMATION_TICKS = 30;
    private static final int FART_RELEASE_TICKS = 6;
    private static final int STINK_BURST_PARTICLES = 32;
    private static final double STINK_BURST_RADIUS = 8.0D;
    private static final int STINK_BURST_DURATION_TICKS = 1200;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int fartAnimationTicks;
    private boolean pendingFartBurst;

    public StinkBugEntity(EntityType<? extends StinkBugEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 8.0D);
    }

    public static boolean canSpawn(EntityType<StinkBugEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        BlockPos belowPos = pos.below();
        BlockPos abovePos = pos.above();
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return level.getBlockState(pos).isAir()
                && (level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.below()).isAir())
                && level.getFluidState(pos).isEmpty()
                && (level.getFluidState(pos.above()).isEmpty() || level.getFluidState(pos.below()).isEmpty())
                && (level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)
                || level.getBlockState(abovePos).isFaceSturdy(level, abovePos, Direction.DOWN));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.fartAnimationTicks > 0) {
            this.fartAnimationTicks--;
            if (this.fartAnimationTicks == FART_RELEASE_TICKS && this.pendingFartBurst && !this.level().isClientSide()) {
                this.playSound(AntarchySoundEvents.STINK_BUG_FART.get(), 1.7F, 0.9F + this.random.nextFloat() * 0.1F);
                StinkyBehavior.emitBurst(this, STINK_BURST_PARTICLES);
                StinkyBehavior.applyBurstStinkyEffect(this, STINK_BURST_RADIUS, STINK_BURST_DURATION_TICKS);
                this.pendingFartBurst = false;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.pendingFartBurst) {
            this.triggerFartAnimation();
            this.pendingFartBurst = true;
        }
        return hurt;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return AntarchyObjects.STINK_BUG.get().create(level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, MAIN_CONTROLLER, 2, this::mainAnimController));
        controllers.add(new AnimationController<>(this, FART_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(FART_TRIGGER, FART_ANIM));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.STINK_BUG_IDLE.get();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <T extends StinkBugEntity> PlayState mainAnimController(AnimationState<T> animationState) {
        animationState.setAnimation(animationState.isMoving() ? WALK_ANIM : IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    private void triggerFartAnimation() {
        this.fartAnimationTicks = FART_ANIMATION_TICKS;
        if (!this.level().isClientSide()) {
            this.triggerAnim(FART_CONTROLLER, FART_TRIGGER);
        }
    }
}
