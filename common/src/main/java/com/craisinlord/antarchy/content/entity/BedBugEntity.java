package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.BedBugEggBlock;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.EnumSet;

public class BedBugEntity extends Animal implements GeoEntity {
    private static final byte BITE_ANIM_EVENT = 18;
    private static final String BITE_TICKS_KEY = "BiteTicks";
    private static final String HAS_PENDING_EGGS_KEY = "HasPendingEggs";
    private static final String LAY_EGG_COOLDOWN_KEY = "LayEggCooldown";
    private static final String EGG_NEST_X_KEY = "EggNestX";
    private static final String EGG_NEST_Y_KEY = "EggNestY";
    private static final String EGG_NEST_Z_KEY = "EggNestZ";
    private static final int BITE_ANIM_TICKS = 10;
    private static final int ATTACK_HIT_TICK = 5;
    private static final int ATTACK_COOLDOWN_TICKS = 12;
    private static final int MIN_LAY_EGG_DELAY = 80;
    private static final int MAX_LAY_EGG_DELAY = 160;
    private static final double FOOD_SEARCH_RADIUS = 10.0D;
    private static final double EGG_GUARD_RADIUS = 7.0D;
    private static final double ATTACK_START_REACH_BUFFER = 1.15D;
    private static final double ATTACK_REACH_BUFFER = 0.3D;
    private static final double ATTACK_LUNGE_HORIZONTAL_SPEED = 0.42D;
    private static final double ATTACK_LUNGE_VERTICAL_SPEED = 0.1D;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().thenPlay("bite");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private BlockPos eggNestPos;
    private int biteAnimationTicks;
    private int attackAnimTicks;
    private int attackCooldownTicks;
    private boolean attackDamageApplied;
    @Nullable
    private LivingEntity attackTarget;
    private boolean hasPendingEggs;
    private int layEggCooldown;
    @Nullable
    private ItemEntity cachedRottenFlesh;
    private int nextFoodSearchTick;
    private int guardNestCooldown;

    public BedBugEntity(EntityType<? extends BedBugEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    public static boolean canSpawn(EntityType<BedBugEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        return level.getDifficulty() != Difficulty.PEACEFUL
                && Animal.checkAnimalSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.0D, Ingredient.of(Items.ROTTEN_FLESH), false));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.9D));
        this.goalSelector.addGoal(3, new SeekRottenFleshGoal());
        this.goalSelector.addGoal(4, new GuardEggNestGoal());
        this.goalSelector.addGoal(5, new BedBugMeleeAttackGoal());
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                entity -> entity instanceof Player player && this.shouldTargetPlayer(player)
        ));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 2, this::mainAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.biteAnimationTicks > 0) {
            this.biteAnimationTicks--;
            if (this.biteAnimationTicks <= 0) {
                this.biteAnimationTicks = 0;
            }
        }

        if (this.level().isClientSide) {
            return;
        }

        if (this.attackCooldownTicks > 0) {
            this.attackCooldownTicks--;
        }

        if (this.attackAnimTicks > 0) {
            this.tickAttackWindup();
            this.attackAnimTicks--;
            if (this.attackAnimTicks <= 0) {
                this.resetAttackState();
            }
        }

        if (this.guardNestCooldown > 0) {
            this.guardNestCooldown--;
        }

        if (this.hasFoodPriorityTarget() && !(this.getTarget() instanceof Player)) {
            this.setTarget(null);
        }

        if (this.eggNestPos != null && !this.level().getBlockState(this.eggNestPos).is(AntarchyObjects.BED_BUG_EGG.get())) {
            this.eggNestPos = this.findNearbyEggNest();
        }

        if (this.hasPendingEggs && --this.layEggCooldown <= 0) {
            this.layClutch();
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return false;
        }

        boolean hurt = this.level() instanceof ServerLevel serverLevel
                ? livingTarget.hurt(AntarchyDamageSources.bedBugBite(serverLevel, this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))
                : super.doHurtTarget(target);
        if (hurt) {
            this.playSound(AntarchySoundEvents.BED_BUG_ATTACK.get(), 0.45F, 1.0F + this.random.nextFloat() * 0.1F);
        }
        return hurt;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.ROTTEN_FLESH);
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof BedBugEntity
                && otherAnimal != this
                && this.isInLove()
                && otherAnimal.isInLove();
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return AntarchyObjects.BED_BUG.get().create(level);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal otherParent) {
        this.setAge(6000);
        otherParent.setAge(6000);
        this.resetLove();
        otherParent.resetLove();
        this.hasPendingEggs = true;
        this.layEggCooldown = MIN_LAY_EGG_DELAY + this.random.nextInt(MAX_LAY_EGG_DELAY - MIN_LAY_EGG_DELAY + 1);
        this.level().broadcastEntityEvent(this, (byte) 18);
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            level.addFreshEntity(new ExperienceOrb(level, this.getX(), this.getY(), this.getZ(), this.random.nextInt(7) + 1));
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        BedBugSpawnData bedBugSpawnData = spawnGroupData instanceof BedBugSpawnData data ? data : new BedBugSpawnData();
        super.finalizeSpawn(level, difficulty, spawnReason, null);

        if (bedBugSpawnData.eggNestPos == null && (spawnReason == MobSpawnType.NATURAL || spawnReason == MobSpawnType.CHUNK_GENERATION) && this.random.nextFloat() < 0.55F) {
            bedBugSpawnData.eggNestPos = this.createNaturalEggNest(level);
        }

        this.eggNestPos = bedBugSpawnData.eggNestPos;
        return bedBugSpawnData;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(BITE_TICKS_KEY, this.biteAnimationTicks);
        tag.putBoolean(HAS_PENDING_EGGS_KEY, this.hasPendingEggs);
        tag.putInt(LAY_EGG_COOLDOWN_KEY, this.layEggCooldown);
        if (this.eggNestPos != null) {
            tag.putInt(EGG_NEST_X_KEY, this.eggNestPos.getX());
            tag.putInt(EGG_NEST_Y_KEY, this.eggNestPos.getY());
            tag.putInt(EGG_NEST_Z_KEY, this.eggNestPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.biteAnimationTicks = Math.max(0, tag.getInt(BITE_TICKS_KEY));
        this.hasPendingEggs = tag.getBoolean(HAS_PENDING_EGGS_KEY);
        this.layEggCooldown = tag.getInt(LAY_EGG_COOLDOWN_KEY);
        if (tag.contains(EGG_NEST_X_KEY) && tag.contains(EGG_NEST_Y_KEY) && tag.contains(EGG_NEST_Z_KEY)) {
            this.eggNestPos = new BlockPos(tag.getInt(EGG_NEST_X_KEY), tag.getInt(EGG_NEST_Y_KEY), tag.getInt(EGG_NEST_Z_KEY));
        } else {
            this.eggNestPos = null;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.BED_BUG_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.BED_BUG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.BED_BUG_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.45F;
    }

    @Override
    public float getVoicePitch() {
        return 1.25F + this.random.nextFloat() * 0.1F;
    }

    public void setHomeEggPos(BlockPos eggPos) {
        this.eggNestPos = eggPos.immutable();
    }

    @Nullable
    public BlockPos getHomeEggPos() {
        return this.eggNestPos;
    }

    private PlayState mainAnimController(AnimationState<BedBugEntity> state) {
        if (this.biteAnimationTicks > 0) {
            return state.setAndContinue(BITE_ANIM);
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.003D) {
            state.getController().setAnimationSpeed(1.15D);
            return state.setAndContinue(WALK_ANIM);
        }

        state.getController().setAnimationSpeed(1.0D);
        return state.setAndContinue(IDLE_ANIM);
    }

    private void startBiteAnimation() {
        this.biteAnimationTicks = BITE_ANIM_TICKS;
        this.level().broadcastEntityEvent(this, BITE_ANIM_EVENT);
    }

    private void beginAttack(LivingEntity target) {
        this.attackTarget = target;
        this.attackAnimTicks = BITE_ANIM_TICKS;
        this.attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
        this.attackDamageApplied = false;
        this.getNavigation().stop();
        this.startBiteAnimation();
        this.commitLunge(target, ATTACK_LUNGE_HORIZONTAL_SPEED, ATTACK_LUNGE_VERTICAL_SPEED);
    }

    private boolean shouldTargetPlayer(@Nullable Player player) {
        return player != null
                && !player.isCreative()
                && !player.isSpectator()
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && !this.hasFoodPriorityTarget();
    }

    private boolean hasFoodPriorityTarget() {
        return !this.isAttackLocked() && this.nearestRottenFlesh() != null;
    }

    private void tickAttackWindup() {
        if (this.attackTarget == null || !this.attackTarget.isAlive()) {
            this.resetAttackState();
            return;
        }

        this.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
        if (!this.attackDamageApplied
                && this.attackAnimTicks == ATTACK_HIT_TICK
                && this.distanceToSqr(this.attackTarget) <= this.getAttackReachSqr(this.attackTarget)) {
            this.attackDamageApplied = true;
            this.doHurtTarget(this.attackTarget);
        }
    }

    private void commitLunge(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        Vec3 lunge = target.position().subtract(this.position());
        Vec3 horizontal = new Vec3(lunge.x, 0.0D, lunge.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return;
        }
        horizontal = horizontal.normalize().scale(horizontalSpeed);
        this.setDeltaMovement(this.getDeltaMovement().add(horizontal.x, verticalSpeed, horizontal.z));
        this.hasImpulse = true;
    }

    private double getAttackStartReachSqr(LivingEntity target) {
        double reach = this.getBbWidth() * 1.7D + target.getBbWidth() + ATTACK_START_REACH_BUFFER;
        return reach * reach + 1.0D;
    }

    private double getAttackReachSqr(LivingEntity target) {
        double reach = this.getBbWidth() * 1.55D + target.getBbWidth() + ATTACK_REACH_BUFFER;
        return reach * reach + 1.0D;
    }

    private void resetAttackState() {
        this.attackAnimTicks = 0;
        this.attackDamageApplied = false;
        this.attackTarget = null;
    }

    private boolean isAttackLocked() {
        return this.attackAnimTicks > 0;
    }

    private boolean shouldSnapBackToCombat() {
        if (this.getHealth() < this.getMaxHealth() * 0.4F) {
            return true;
        }
        LivingEntity lastAttacker = this.getLastHurtByMob();
        return lastAttacker != null && lastAttacker.isAlive() && this.distanceToSqr(lastAttacker) < 9.0D;
    }

    @Nullable
    private ItemEntity nearestRottenFlesh() {
        if (this.tickCount >= this.nextFoodSearchTick || (this.cachedRottenFlesh != null && !this.cachedRottenFlesh.isAlive())) {
            this.cachedRottenFlesh = this.findNearestRottenFlesh();
            this.nextFoodSearchTick = this.tickCount + 5;
        }
        return this.cachedRottenFlesh;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == BITE_ANIM_EVENT) {
            this.biteAnimationTicks = BITE_ANIM_TICKS;
            return;
        }
        super.handleEntityEvent(id);
    }

    private void invalidateFoodCache() {
        this.cachedRottenFlesh = null;
        this.nextFoodSearchTick = 0;
    }

    @Nullable
    private ItemEntity findNearestRottenFlesh() {
        return this.level().getEntitiesOfClass(
                        ItemEntity.class,
                        this.getBoundingBox().inflate(FOOD_SEARCH_RADIUS, 2.0D, FOOD_SEARCH_RADIUS),
                        itemEntity -> itemEntity.isAlive()
                                && !itemEntity.getItem().isEmpty()
                                && itemEntity.getItem().is(Items.ROTTEN_FLESH))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Nullable
    private BlockPos findNearbyEggNest() {
        BlockPos origin = this.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        double bestDistance = Double.MAX_VALUE;
        BlockPos bestPos = null;

        for (int x = -10; x <= 10; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -10; z <= 10; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!this.level().getBlockState(cursor).is(AntarchyObjects.BED_BUG_EGG.get())) {
                        continue;
                    }

                    double distance = cursor.distSqr(origin);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestPos = cursor.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    @Nullable
    private BlockPos createNaturalEggNest(ServerLevelAccessor level) {
        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 12; attempt++) {
            BlockPos candidate = origin.offset(this.random.nextInt(9) - 4, 0, this.random.nextInt(9) - 4);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            if (!level.isEmptyBlock(surface) || !level.getBlockState(surface.below()).isFaceSturdy(level, surface.below(), net.minecraft.core.Direction.UP)) {
                continue;
            }

            int eggs = 1 + this.random.nextInt(4);
            level.setBlock(surface, AntarchyObjects.BED_BUG_EGG.get().defaultBlockState().setValue(BedBugEggBlock.EGGS, eggs), Block.UPDATE_ALL);
            return surface.immutable();
        }

        return null;
    }

    private void layClutch() {
        BlockPos eggPos = this.findEggLayPos();
        if (eggPos == null) {
            this.layEggCooldown = 40;
            return;
        }

        int eggs = 1 + this.random.nextInt(4);
        this.level().setBlock(
                eggPos,
                AntarchyObjects.BED_BUG_EGG.get().defaultBlockState().setValue(BedBugEggBlock.EGGS, eggs),
                3
        );
        this.eggNestPos = eggPos;
        this.hasPendingEggs = false;
        this.playSound(SoundEvents.TURTLE_LAY_EGG, 0.8F, 1.15F + this.random.nextFloat() * 0.08F);
    }

    @Nullable
    private BlockPos findEggLayPos() {
        if (this.eggNestPos != null && this.level().getBlockState(this.eggNestPos).isAir() && this.level().getBlockState(this.eggNestPos.below()).isFaceSturdy(this.level(), this.eggNestPos.below(), net.minecraft.core.Direction.UP)) {
            return this.eggNestPos;
        }

        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 12; attempt++) {
            BlockPos candidate = origin.offset(this.random.nextInt(7) - 3, 0, this.random.nextInt(7) - 3);
            BlockPos surface = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            if (this.level().isEmptyBlock(surface) && this.level().getBlockState(surface.below()).isFaceSturdy(this.level(), surface.below(), net.minecraft.core.Direction.UP)) {
                return surface.immutable();
            }
        }

        return null;
    }

    private final class BedBugMeleeAttackGoal extends MeleeAttackGoal {
        private BedBugMeleeAttackGoal() {
            super(BedBugEntity.this, 1.0D, true);
        }

        @Override
        public boolean canUse() {
            return !BedBugEntity.this.hasFoodPriorityTarget() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !BedBugEntity.this.hasFoodPriorityTarget()
                    && (BedBugEntity.this.isAttackLocked() || super.canContinueToUse());
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy) {
            if (BedBugEntity.this.isAttackLocked()) {
                return;
            }
            if (BedBugEntity.this.distanceToSqr(enemy) <= BedBugEntity.this.getAttackStartReachSqr(enemy)) {
                if (this.isTimeToAttack() && BedBugEntity.this.attackCooldownTicks <= 0) {
                    this.resetAttackCooldown();
                    BedBugEntity.this.beginAttack(enemy);
                }
                return;
            }
            super.checkAndPerformAttack(enemy);
        }
    }

    private final class SeekRottenFleshGoal extends Goal {
        @Nullable
        private ItemEntity targetFood;
        private double lastDistanceSqr;
        private int stuckTicks;

        private SeekRottenFleshGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.targetFood = BedBugEntity.this.nearestRottenFlesh();
            return this.targetFood != null
                    && BedBugEntity.this.biteAnimationTicks <= 0
                    && !BedBugEntity.this.isAttackLocked()
                    && BedBugEntity.this.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            if (BedBugEntity.this.shouldSnapBackToCombat()) {
                BedBugEntity.this.invalidateFoodCache();
                return false;
            }
            return this.targetFood != null
                    && this.targetFood.isAlive()
                    && !this.targetFood.getItem().isEmpty()
                    && this.targetFood.getItem().is(Items.ROTTEN_FLESH)
                    && BedBugEntity.this.getTarget() == null
                    && !BedBugEntity.this.isAttackLocked();
        }

        @Override
        public void start() {
            BedBugEntity.this.setTarget(null);
            this.lastDistanceSqr = Double.MAX_VALUE;
            this.stuckTicks = 0;
        }

        @Override
        public void stop() {
            BedBugEntity.this.getNavigation().stop();
            this.targetFood = null;
            this.lastDistanceSqr = Double.MAX_VALUE;
            this.stuckTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetFood == null) {
                return;
            }

            BedBugEntity.this.getLookControl().setLookAt(this.targetFood, 30.0F, 30.0F);
            BedBugEntity.this.getNavigation().moveTo(this.targetFood, 1.0D);
            double distanceSqr = BedBugEntity.this.distanceToSqr(this.targetFood);

            if (distanceSqr < this.lastDistanceSqr - 0.2D) {
                this.lastDistanceSqr = distanceSqr;
                this.stuckTicks = 0;
            } else if (BedBugEntity.this.getNavigation().isDone() && distanceSqr > 4.0D) {
                if (++this.stuckTicks >= 20) {
                    BedBugEntity.this.invalidateFoodCache();
                    this.targetFood = null;
                    return;
                }
            } else {
                this.lastDistanceSqr = distanceSqr;
            }

            if (distanceSqr <= 2.0D) {
                ItemStack stack = this.targetFood.getItem();
                stack.shrink(1);
                if (stack.isEmpty()) {
                    this.targetFood.discard();
                } else {
                    this.targetFood.setItem(stack);
                }
                BedBugEntity.this.heal(2.0F);
                BedBugEntity.this.startBiteAnimation();
                BedBugEntity.this.playSound(SoundEvents.GENERIC_EAT, 0.45F, 1.2F + BedBugEntity.this.random.nextFloat() * 0.2F);
                BedBugEntity.this.invalidateFoodCache();
                this.targetFood = null;
                BedBugEntity.this.getNavigation().stop();
            }
        }
    }

    private final class GuardEggNestGoal extends Goal {
        @Nullable
        private Vec3 patrolPos;

        private GuardEggNestGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (BedBugEntity.this.guardNestCooldown > 0
                    || BedBugEntity.this.eggNestPos == null
                    || BedBugEntity.this.hasFoodPriorityTarget()
                    || BedBugEntity.this.getTarget() != null
                    || BedBugEntity.this.hasPendingEggs) {
                return false;
            }

            this.patrolPos = this.findPatrolPos();
            return this.patrolPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.patrolPos != null
                    && BedBugEntity.this.getTarget() == null
                    && !BedBugEntity.this.hasFoodPriorityTarget()
                    && !BedBugEntity.this.getNavigation().isDone();
        }

        @Override
        public void start() {
            if (this.patrolPos != null) {
                BedBugEntity.this.getNavigation().moveTo(this.patrolPos.x, this.patrolPos.y, this.patrolPos.z, 0.9D);
            }
        }

        @Override
        public void tick() {
            if (this.patrolPos != null) {
                BedBugEntity.this.getLookControl().setLookAt(this.patrolPos.x, this.patrolPos.y, this.patrolPos.z, 20.0F, 20.0F);
            }
        }

        @Override
        public void stop() {
            BedBugEntity.this.getNavigation().stop();
            BedBugEntity.this.guardNestCooldown = 20 + BedBugEntity.this.random.nextInt(20);
            this.patrolPos = null;
        }

        @Nullable
        private Vec3 findPatrolPos() {
            if (BedBugEntity.this.eggNestPos == null) {
                return null;
            }

            double distanceToNest = BedBugEntity.this.distanceToSqr(Vec3.atCenterOf(BedBugEntity.this.eggNestPos));
            if (distanceToNest < 9.0D && BedBugEntity.this.random.nextInt(6) != 0) {
                return null;
            }

            for (int attempt = 0; attempt < 10; attempt++) {
                BlockPos candidate = BedBugEntity.this.eggNestPos.offset(BedBugEntity.this.random.nextInt(11) - 5, 0, BedBugEntity.this.random.nextInt(11) - 5);
                BlockPos surface = BedBugEntity.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
                if (surface.distSqr(BedBugEntity.this.eggNestPos) > EGG_GUARD_RADIUS * EGG_GUARD_RADIUS) {
                    continue;
                }
                return Vec3.atBottomCenterOf(surface);
            }

            return Vec3.atBottomCenterOf(BedBugEntity.this.eggNestPos);
        }
    }

    private static final class BedBugSpawnData implements SpawnGroupData {
        @Nullable
        private BlockPos eggNestPos;
    }
}
