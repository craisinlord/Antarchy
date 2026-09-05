package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

public class ManticoreEntity extends Monster implements GeoEntity {
    private static final String SUMMONER_KEY = "QueenSummoner";
    private static final String PLAYER_SUMMONER_KEY = "PlayerSummoner";
    private static final String PLAYER_SUMMON_LIFETIME_KEY = "PlayerSummonLifetime";
    private static final int LOST_SUMMONER_GRACE_TICKS = 200;
    private static final int TAKEOFF_COOLDOWN_TICKS = 40;
    private static final int STING_COOLDOWN_TICKS = 60;
    private static final float STING_CHANCE = 0.35F;
    private static final int SWARM_RETARGET_INTERVAL = 40;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().thenPlay("bite");
    private static final RawAnimation FLY_BITE_ANIM = RawAnimation.begin().thenPlay("fly_bite");
    private static final RawAnimation STING_ANIM = RawAnimation.begin().thenPlay("sting");
    private static final RawAnimation FLY_START_ANIM = RawAnimation.begin().thenPlay("fly_start");
    private static final RawAnimation FLY_LAND_ANIM = RawAnimation.begin().thenPlay("fly_land");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private UUID summonerId;
    @Nullable
    private UUID playerSummonerId;
    private int playerSummonLifetime;
    private int playerSummonAge;
    private int lostSummonerTicks;
    private int takeoffCooldown;
    private int stingCooldown;
    private boolean wasFlyingLastTick;

    public ManticoreEntity(EntityType<? extends ManticoreEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.manticoreHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.manticoreAttackDamage())
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    public static boolean canSpawn(EntityType<ManticoreEntity> type, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER
                || spawnReason == MobSpawnType.COMMAND || spawnReason == MobSpawnType.MOB_SUMMONED) {
            return true;
        }
        if (pos.getY() >= 0) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());
        boolean floorSupport = !below.is(Blocks.BEDROCK) && below.isFaceSturdy(level, pos.below(), Direction.UP);
        boolean ceilingSupport = !above.is(Blocks.BEDROCK) && above.isFaceSturdy(level, pos.above(), Direction.DOWN);
        return level.getDifficulty() != Difficulty.PEACEFUL
                && (floorSupport || ceilingSupport)
                && level.isEmptyBlock(pos)
                && Monster.checkMonsterSpawnRules(type, level, spawnReason, pos, random);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ManticoreAttackGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::canTargetEntity));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnData) {
        ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.manticoreHealth());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    public void markQueenSummoned(UUID queenId) {
        this.summonerId = queenId;
        this.playerSummonerId = null;
        this.lostSummonerTicks = 0;
        this.setPersistenceRequired();
    }

    public void markPlayerSummoned(UUID playerId, int lifetimeTicks) {
        this.playerSummonerId = playerId;
        this.summonerId = null;
        this.playerSummonLifetime = lifetimeTicks;
        this.playerSummonAge = 0;
        this.lostSummonerTicks = 0;
        this.setPersistenceRequired();
    }

    public boolean isSummonedBy(UUID queenId) {
        return queenId.equals(this.summonerId);
    }

    public boolean isQueenSummoned() {
        return this.summonerId != null;
    }

    public boolean isPlayerSummoned() {
        return this.playerSummonerId != null;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity != null && this.playerSummonerId != null && entity.getUUID().equals(this.playerSummonerId)) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    public static int countSummonedBy(ServerLevel level, UUID queenId) {
        int count = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ManticoreEntity manticore && manticore.isAlive() && manticore.isSummonedBy(queenId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.summonerId == null && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return this.summonerId != null || this.playerSummonerId != null || super.requiresCustomPersistence();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        boolean flying = !this.onGround();
        if (flying != this.wasFlyingLastTick) {
            this.triggerAnim("controller", flying ? "fly_start" : "fly_land");
            this.wasFlyingLastTick = flying;
        }

        if (this.takeoffCooldown > 0) {
            this.takeoffCooldown--;
        }
        if (this.stingCooldown > 0) {
            this.stingCooldown--;
        }

        this.tickQueenSummonCleanup();

        this.tickPlayerSummon();
        this.tickQueenSummonTarget();

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            if (this.onGround() && this.takeoffCooldown <= 0 && this.distanceToSqr(target) > 6.0D) {
                this.takeoff();
            }
            if (!this.isPlayerSummoned() && !this.isQueenSummoned()
                    && this.tickCount % SWARM_RETARGET_INTERVAL == 0) {
                this.spreadSwarmTarget(target);
            }
        }
    }

    private void tickPlayerSummon() {
        if (this.playerSummonerId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (++this.playerSummonAge >= this.playerSummonLifetime) {
            this.discard();
            return;
        }
        Entity entity = serverLevel.getEntity(this.playerSummonerId);
        if (!(entity instanceof Player player) || !player.isAlive()) {
            this.discard();
            return;
        }
        LivingEntity target = player.getLastHurtMob();
        if (target == null || !target.isAlive() || !this.canTargetEntity(target)) {
            target = player.getLastHurtByMob();
        }
        if (target != null && target.isAlive() && this.canTargetEntity(target)) {
            this.setTarget(target);
        } else if (this.getTarget() != null && !this.canTargetEntity(this.getTarget())) {
            this.setTarget(null);
        }
    }

    private void tickQueenSummonTarget() {
        if (this.summonerId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity entity = serverLevel.getEntity(this.summonerId);
        if (!(entity instanceof QueenEntity queen) || !queen.isAlive()) {
            return;
        }
        LivingEntity target = queen.getTarget();
        if (target != null && target.isAlive() && this.canTargetEntity(target)) {
            this.setTarget(target);
        } else {
            this.setTarget(null);
        }
    }

    private boolean canTargetEntity(LivingEntity target) {
        if (target == null || target == this || this.isAlliedTo(target) || target.isAlliedTo(this)) {
            return false;
        }
        if (this.playerSummonerId != null) {
            if (!(this.level() instanceof ServerLevel serverLevel)) {
                return false;
            }
            Entity entity = serverLevel.getEntity(this.playerSummonerId);
            if (!(entity instanceof Player player)) {
                return false;
            }
            return target == player.getLastHurtMob() || target == player.getLastHurtByMob();
        }
        return true;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target != null && !this.canTargetEntity(target) ? null : target);
    }

    private void tickQueenSummonCleanup() {
        if (this.summonerId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity summoner = serverLevel.getEntity(this.summonerId);
        if (summoner instanceof QueenEntity queen && queen.isAlive()) {
            this.lostSummonerTicks = 0;
            return;
        }
        if (++this.lostSummonerTicks > LOST_SUMMONER_GRACE_TICKS) {
            this.discard();
        }
    }

    private void takeoff() {
        this.takeoffCooldown = TAKEOFF_COOLDOWN_TICKS;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.55D, 0.0D));
        this.hasImpulse = true;
        LivingEntity target = this.getTarget();
        if (target != null) {
            Vec3 above = target.position().add(oppositeGravity(this).scale(3.0D));
            this.getMoveControl().setWantedPosition(above.x, above.y, above.z, 1.2D);
        }
    }

    private void spreadSwarmTarget(LivingEntity currentTarget) {
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(this.getAttributeValue(Attributes.FOLLOW_RANGE)),
                player -> player.isAlive() && this.canAttack(player));
        if (nearbyPlayers.size() < 2) {
            return;
        }

        List<ManticoreEntity> swarm = this.level().getEntitiesOfClass(
                ManticoreEntity.class,
                this.getBoundingBox().inflate(32.0D),
                manticore -> manticore != this && manticore.isAlive());

        Player bestPlayer = null;
        int bestLoad = Integer.MAX_VALUE;
        for (Player player : nearbyPlayers) {
            int load = 0;
            for (ManticoreEntity manticore : swarm) {
                if (manticore.getTarget() == player) {
                    load++;
                }
            }
            if (load < bestLoad) {
                bestLoad = load;
                bestPlayer = player;
            }
        }

        int currentLoad = 0;
        for (ManticoreEntity manticore : swarm) {
            if (manticore.getTarget() == currentTarget) {
                currentLoad++;
            }
        }
        if (bestPlayer != null && bestPlayer != currentTarget && currentLoad - bestLoad >= 2) {
            this.setTarget(bestPlayer);
        }
    }

    private static Vec3 oppositeGravity(Entity entity) {
        return AntarchyGravityApi.isGravityInverted(entity) ? new Vec3(0.0D, -1.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
    }

    void performSting(LivingEntity enemy) {
        this.swing(InteractionHand.MAIN_HAND);
        if (enemy.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
            enemy.addEffect(new MobEffectInstance(MobEffects.POISON, AntarchySettings.manticoreStingPoisonTicks(), 0), this);
        }
        this.stingCooldown = STING_COOLDOWN_TICKS;
        this.triggerAnim("controller", "sting");
    }

    boolean stingReady() {
        return this.stingCooldown <= 0;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.triggerAnim("controller", this.onGround() ? "bite" : "fly_bite");
        }
        return hurt;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.summonerId != null) {
            tag.putUUID(SUMMONER_KEY, this.summonerId);
        }
        if (this.playerSummonerId != null) {
            tag.putUUID(PLAYER_SUMMONER_KEY, this.playerSummonerId);
            tag.putInt(PLAYER_SUMMON_LIFETIME_KEY, this.playerSummonLifetime);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.summonerId = tag.hasUUID(SUMMONER_KEY) ? tag.getUUID(SUMMONER_KEY) : null;
        this.playerSummonerId = tag.hasUUID(PLAYER_SUMMONER_KEY) ? tag.getUUID(PLAYER_SUMMONER_KEY) : null;
        this.playerSummonLifetime = tag.getInt(PLAYER_SUMMON_LIFETIME_KEY);
        this.playerSummonAge = 0;
        this.lostSummonerTicks = 0;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, this::locomotionPredicate)
                .triggerableAnim("bite", BITE_ANIM)
                .triggerableAnim("fly_bite", FLY_BITE_ANIM)
                .triggerableAnim("sting", STING_ANIM)
                .triggerableAnim("fly_start", FLY_START_ANIM)
                .triggerableAnim("fly_land", FLY_LAND_ANIM));
    }

    private PlayState locomotionPredicate(AnimationState<ManticoreEntity> state) {
        if (!this.onGround()) {
            return state.setAndContinue(FLY_ANIM);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private static class ManticoreAttackGoal extends MeleeAttackGoal {
        private final ManticoreEntity manticore;

        ManticoreAttackGoal(ManticoreEntity manticore) {
            super(manticore, 1.1D, true);
            this.manticore = manticore;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy) {
            if (this.canPerformAttack(enemy) && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                if (this.manticore.onGround() && this.manticore.stingReady() && this.manticore.getRandom().nextFloat() < STING_CHANCE) {
                    this.manticore.performSting(enemy);
                    return;
                }
                this.manticore.swing(InteractionHand.MAIN_HAND);
                this.manticore.doHurtTarget(enemy);
                return;
            }
            super.checkAndPerformAttack(enemy);
        }
    }
}
