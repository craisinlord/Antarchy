package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.StinkyBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FART_ANIM = RawAnimation.begin().thenPlay("fart");
    private static final int FART_ANIMATION_TICKS = 30;
    private static final int STINK_BURST_PARTICLES = 32;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int fartAnimationTicks;

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
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Animal.checkAnimalSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.fartAnimationTicks > 0) {
            this.fartAnimationTicks--;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            this.triggerFartAnimation();
            StinkyBehavior.emitBurst(this, STINK_BURST_PARTICLES);
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
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <T extends StinkBugEntity> PlayState mainAnimController(AnimationState<T> animationState) {
        if (this.fartAnimationTicks > 0) {
            animationState.setAnimation(FART_ANIM);
            return PlayState.CONTINUE;
        }
        animationState.setAnimation(animationState.isMoving() ? WALK_ANIM : IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    private void triggerFartAnimation() {
        this.fartAnimationTicks = FART_ANIMATION_TICKS;
    }
}
