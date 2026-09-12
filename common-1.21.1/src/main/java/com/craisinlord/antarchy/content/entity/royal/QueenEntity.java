package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackLane;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalEffectController;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class QueenEntity extends RoyalBossEntity {
    static final double TIME_FIELD_RADIUS_SCALE = 3.0D;
    private static final String SUMMON_COOLDOWN_KEY = "ManticoreSummonCooldownTicks";
    private static final int FAILED_SUMMON_RETRY_TICKS = 20;
    private static final int POSITION_ATTEMPTS_PER_MANTICORE = 8;

    private static final ResourceLocation ACCEL_SPEED_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "queen_royal_acceleration_speed");
    private static final ResourceLocation ACCEL_FLY_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "queen_royal_acceleration_fly");
    private static final DustParticleOptions ACCEL_DUST = new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.15F, 0.15F), 2.0F);

    private static final int GRAVITY_STOMP_COOLDOWN = 180;
    private static final int MOMENTUM_LOCK_COOLDOWN = 420;
    private static final int MOMENTUM_LOCK_DURATION = 90;
    private static final double MOMENTUM_LOCK_RADIUS = 26.0D;
    private static final float MOMENTUM_LOCK_EARLY_HEALTH_THRESHOLD = 0.80F;
    private static final int CRUSHING_GRAVITY_COOLDOWN = 360;
    private static final int CRUSHING_GRAVITY_DURATION = 120;
    private static final double CRUSHING_GRAVITY_RADIUS = 18.0D;
    private static final double CRUSHING_GRAVITY_STRENGTH = 0.12D;
    private static final int ACCELERATION_COOLDOWN = 500;
    private static final int ACCELERATION_DURATION = 140;
    private static final int BLACK_HOLE_COOLDOWN = 340;

    private int manticoreSummonCooldownTicks;
    private int gravityStompCooldownTicks;
    private int momentumLockCooldownTicks;
    private int crushingGravityCooldownTicks;
    private int accelerationCooldownTicks;
    private int blackHoleCooldownTicks;
    private final int[] dreamFireballCooldownTicks = new int[3];
    private int idleWanderCooldownTicks;
    private final RoyalEffectController royalEffects = new RoyalEffectController();
    private final Map<UUID, Vec3> frozenVelocities = new HashMap<>();
    private long naturalTrailSiteId = Long.MIN_VALUE;
    @Nullable
    private BlockPos naturalTrailHome;

    public QueenEntity(EntityType<? extends QueenEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.queenHealth(), AntarchySettings.queenAttackDamage());
    }

    @Override
    protected String geoName() {
        return "queen";
    }

    @Override
    protected boolean isFlyingBoss() {
        return true;
    }

    @Override
    protected double groundCombatBias() {
        return 0.72D;
    }

    @Override
    protected double biteApproachSpeed() {
        return 1.55D;
    }

    protected void registerRoyalTargetGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, KingEntity.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false, this::canAttack));
    }

    @Override
    protected int selectBeamVolleyLimit(Phase phase) {
        return switch (phase) {
            case ONE -> AntarchySettings.queenBeamPhaseOneCap();
            case TWO -> AntarchySettings.queenBeamPhaseTwoCap();
            case THREE -> this.random.nextDouble() < AntarchySettings.queenBeamPhaseThreeMaxVolleyChance()
                    ? AntarchySettings.queenBeamPhaseThreeCap()
                    : Math.min(2, AntarchySettings.queenBeamPhaseThreeCap());
        };
    }

    @Override
    protected boolean shouldDistributeHeadTargets(LivingEntity primaryTarget) {
        return !(primaryTarget instanceof KingEntity);
    }

    public boolean canAttack(LivingEntity target) {
        return !target.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK)
                && super.canAttack(target);
    }

    public boolean canDamageWithRoyalAttack(LivingEntity target) {
        return target != this
                && target.isAlive()
                && (!(target instanceof Player player) || !player.isCreative() && !player.isSpectator())
                && !target.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK)
                && !this.isAlliedTo(target);
    }

    @Override
    protected SoundEvent royalFlyLoopSound() {
        return AntarchySoundEvents.QUEEN_FLY_LOOP.get();
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.PURPLE;
    }

    @Override
    protected SoundEvent royalIdleSound() {
        return AntarchySoundEvents.QUEEN_IDLE.get();
    }

    @Override
    protected SoundEvent royalHurtSound() {
        return AntarchySoundEvents.QUEEN_HURT.get();
    }

    @Override
    protected SoundEvent royalDeathSound() {
        return AntarchySoundEvents.QUEEN_DEATH.get();
    }

    @Override
    protected SoundEvent royalBiteSound() {
        return AntarchySoundEvents.QUEEN_BITE.get();
    }

    @Override
    protected SoundEvent royalBeamShootSound() { return AntarchySoundEvents.QUEEN_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamStartSound() { return AntarchySoundEvents.QUEEN_BEAM_START.get(); }

    @Override
    protected SoundEvent royalBeamLoopSound() { return AntarchySoundEvents.QUEEN_BEAM_LOOP.get(); }

    @Override
    protected SoundEvent royalBeamEndSound() { return AntarchySoundEvents.QUEEN_BEAM_END.get(); }

    @Override
    protected RoyalBeamSettings royalBeamSettings() {
        return new RoyalBeamSettings(AntarchySettings.queenBeamRange(), AntarchySettings.queenBeamTracking(), 7.5D,
                AntarchySettings.queenBeamDurationTicks(), AntarchySettings.queenBeamTravelTicks(), AntarchySettings.queenBeamCooldownTicks(), 6.0F, 6.0F,
                (float) AntarchySettings.queenBeamDamage(), 1.0F, 3, 100.0D,
                (float) AntarchySettings.queenBeamTerrainRadius(), 4.0F, AntarchySettings.queenBeamTerrainCap(),
                1.0F, 0.08F, 15.0F, true, true);
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode() {
        return RoyalBeamTerrainMode.DESTROY;
    }

    @Override
    protected RoyalBeamElement royalBeamElement(@Nullable RoyalHead head) {
        if (head == null) {
            return RoyalBeamElement.QUEEN_BLACK;
        }
        return switch (head.slot()) {
            case LEFT -> RoyalBeamElement.QUEEN_PURPLE;
            case CENTER -> RoyalBeamElement.QUEEN_BLACK;
            case RIGHT -> RoyalBeamElement.QUEEN_RED;
        };
    }

    public static boolean isFieldImmune(Entity entity) {
        return entity.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.isDeadOrDying()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target)) {
            this.tickIdleWander();
        }
        this.tickManticoreSummon();
        this.tickQueenAbilities();
        this.tickDreamFireballs();
        this.tickFinalAcceleration();
        this.royalEffects.tick();
    }

    private void tickDreamFireballs() {
        if (!(this.level() instanceof ServerLevel level)) return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target)) return;
        for (RoyalHead head : new RoyalHead[] {this.royalHead(RoyalHead.Slot.LEFT), this.royalHead(RoyalHead.Slot.CENTER), this.royalHead(RoyalHead.Slot.RIGHT)}) {
            int index = head.slot().ordinal();
            if (this.dreamFireballCooldownTicks[index] > 0) {
                this.dreamFireballCooldownTicks[index]--;
                continue;
            }
            if (!head.readyToAttack() || this.distanceTo(target) < 12.0D || this.distanceTo(target) > 52.0D) continue;
            int cooldown = this.cooldown(180);
            RoyalHead.Slot slot = head.slot();
            if (!this.beginRoyalAttack("dream_fireball_" + slot.name(), this.headLane(head), cooldown, 12, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            head.startShoot();
                            QueenEntity.this.triggerAnim(slot.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            Vec3 origin = QueenEntity.this.headAnchor(head);
                            Vec3 direction = target.getEyePosition().subtract(origin).normalize();
                            RoyalElementalProjectileEntity projectile = RoyalElementalProjectileEntity.create(
                                    level, QueenEntity.this, RoyalBeamElement.DREAM_FIRE, origin, direction);
                            level.addFreshEntity(projectile);
                            QueenEntity.this.playRoyalSound(AntarchySoundEvents.QUEEN_BEAM_SHOOT.get(), 0.9F);
                        }
                        @Override public void onComplete() {
                            head.stopShoot();
                        }
                    })) {
                this.dreamFireballCooldownTicks[index] = cooldown;
            }
        }
    }

    private void tickIdleWander() {
        if (this.idleWanderCooldownTicks-- > 0) {
            return;
        }
        this.idleWanderCooldownTicks = 80 + this.random.nextInt(80);
        if (this.naturalTrailHome != null) {
            double distanceSquared = this.distanceToSqr(
                    this.naturalTrailHome.getX() + 0.5D,
                    this.naturalTrailHome.getY() + 0.5D,
                    this.naturalTrailHome.getZ() + 0.5D
            );
            if (distanceSquared > 48.0D * 48.0D) {
                this.getMoveControl().setWantedPosition(
                        this.naturalTrailHome.getX() + 0.5D,
                        this.naturalTrailHome.getY() + 0.5D,
                        this.naturalTrailHome.getZ() + 0.5D,
                        1.0D
                );
                return;
            }
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double radius = 6.0D + this.random.nextDouble() * 22.0D;
            double wx = this.naturalTrailHome.getX() + 0.5D + Math.cos(angle) * radius;
            double wz = this.naturalTrailHome.getZ() + 0.5D + Math.sin(angle) * radius;
            double wy = this.naturalTrailHome.getY() - 2.0D - this.random.nextDouble() * 7.0D;
            this.getMoveControl().setWantedPosition(wx, wy, wz, 0.7D);
            return;
        }
        double angle = this.random.nextDouble() * Mth.TWO_PI;
        double radius = 6.0D + this.random.nextDouble() * 14.0D;
        double wx = this.getX() + Math.cos(angle) * radius;
        double wz = this.getZ() + Math.sin(angle) * radius;
        this.getMoveControl().setWantedPosition(wx, this.groundYBelow(wx, wz), wz, 0.7D);
    }

    @Nullable
    public static QueenEntity spawnFromUndersideTrail(ServerLevel level, BlockPos spawnPos, BlockPos homePos, long siteId, float yaw) {
        if (!ThoraxisUndersideManager.isThoraxis(level) || spawnPos.getY() >= 0) {
            return null;
        }
        AABB siteBounds = new AABB(homePos).inflate(160.0D);
        java.util.List<QueenEntity> existing = level.getEntitiesOfClass(QueenEntity.class, siteBounds,
                queen -> queen.naturalTrailSiteId == siteId && queen.isAlive());
        if (!existing.isEmpty()) {
            return existing.getFirst();
        }
        if (!level.hasChunksAt(spawnPos.offset(-16, -2, -16), spawnPos.offset(16, 18, 16))) {
            return null;
        }
        QueenEntity queen = AntarchyObjects.QUEEN.get().create(level);
        if (queen == null) {
            return null;
        }
        queen.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        if (!level.noCollision(queen, queen.getBoundingBox())) {
            return null;
        }
        queen.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.CHUNK_GENERATION, null);
        queen.naturalTrailSiteId = siteId;
        queen.naturalTrailHome = homePos.immutable();
        queen.setPersistenceRequired();
        queen.setNoGravity(false);
        ThoraxisUndersideManager.applyUndersideInversion(queen);
        AntarchyGravityApi.setAirborneGravityDirection(queen, AntarchyGravityDirection.UP, true, new AntarchyGravityTransition(12));
        queen.resetFallDistance();
        return level.addFreshEntity(queen) ? queen : null;
    }

    private int cooldown(int base) {
        return this.royalEffects.active("acceleration") ? Math.max(20, base / 2) : base;
    }

    private void tickManticoreSummon() {
        if (this.manticoreSummonCooldownTicks > 0) {
            this.manticoreSummonCooldownTicks--;
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target)) {
            return;
        }

        int currentCount = ManticoreEntity.countSummonedBy(serverLevel, this.getUUID());
        int cap = AntarchySettings.queenManticoreCap();
        if (currentCount >= cap) {
            this.manticoreSummonCooldownTicks = FAILED_SUMMON_RETRY_TICKS;
            return;
        }

        int summonCount = Math.min(AntarchySettings.queenManticoreSummonCount(), cap - currentCount);
        int spawned = this.spawnManticoreTears(serverLevel, target, summonCount);

        this.manticoreSummonCooldownTicks = spawned > 0
                ? this.cooldown(Math.max(20, AntarchySettings.queenManticoreSummonCooldownTicks()))
                : FAILED_SUMMON_RETRY_TICKS;
        if (spawned > 0) {
            this.triggerAnim("body_action", "minion_spawn");
            this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.8F + this.random.nextFloat() * 0.12F);
            serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 2.0D, this.getZ(),
                    36, 3.0D, 2.0D, 3.0D, 0.15D);
        }
    }

    private int spawnManticoreTears(ServerLevel level, LivingEntity target, int count) {
        int pairs = Math.max(1, Math.min(3, (count + 1) / 2));
        int created = 0;
        Vec3 towardTarget = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (towardTarget.lengthSqr() < 1.0E-4D) {
            towardTarget = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        towardTarget = towardTarget.normalize();
        Vec3 behindTarget = target.position().add(towardTarget.scale(8.0D));
        Vec3 lateral = new Vec3(-towardTarget.z, 0.0D, towardTarget.x);
        double vertical = AntarchyGravityApi.isGravityInverted(this) ? -2.0D : 2.0D;
        for (int i = 0; i < pairs; i++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double distance = Math.max(8.0D, AntarchySettings.queenManticoreSummonRange() * 0.65D);
            double spread = (i - (pairs - 1) * 0.5D) * 5.0D;
            Vec3 pairCenter = behindTarget.add(lateral.scale(spread)).add(0.0D, vertical, 0.0D);
            Vec3 firstPos = pairCenter.add(lateral.scale(3.0D));
            Vec3 secondPos = pairCenter.add(lateral.scale(-3.0D));
            DimensionalTearEntity first = DimensionalTearEntity.createQueenManticoreTear(level, firstPos,
                    (float) Math.toDegrees(angle), 240, this.getUUID(), Math.min(3, count));
            DimensionalTearEntity second = DimensionalTearEntity.createQueenManticoreTear(level, secondPos,
                    (float) Math.toDegrees(angle + Math.PI), 240, this.getUUID(), Math.min(3, count));
            first.linkTo(second);
            second.linkTo(first);
            level.addFreshEntity(first);
            level.addFreshEntity(second);
            created++;
        }
        return created;
    }

    private void tickQueenAbilities() {
        if (this.gravityStompCooldownTicks > 0) this.gravityStompCooldownTicks--;
        if (this.momentumLockCooldownTicks > 0) this.momentumLockCooldownTicks--;
        if (this.crushingGravityCooldownTicks > 0) this.crushingGravityCooldownTicks--;
        if (this.accelerationCooldownTicks > 0) this.accelerationCooldownTicks--;
        if (this.blackHoleCooldownTicks > 0) this.blackHoleCooldownTicks--;

        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Phase phase = this.phase();
        boolean majorBusy = this.royalEffects.active("momentum_lock") || this.royalEffects.active("crushing_gravity");
        boolean allowOverlap = phase != Phase.ONE;
        String[] candidates = phase == Phase.ONE && !this.isMomentumLockAvailableEarly()
                ? new String[] {"gravity_stomp", "black_hole"}
                : new String[] {"gravity_stomp", "black_hole", "momentum_lock", "crushing_gravity", "acceleration"};
        String selected = this.attackScheduler.chooseWeighted(candidates, id -> {
            if (!this.queenAttackReady(id, phase, majorBusy, allowOverlap)) return 0;
            return switch (id) {
                case "black_hole", "crushing_gravity" -> phase == Phase.THREE ? 5 : 3;
                case "momentum_lock", "acceleration" -> phase == Phase.THREE ? 4 : 2;
                default -> 4;
            };
        }, this.random);
        if (selected == null) return;
        int cooldown = switch (selected) {
            case "gravity_stomp" -> GRAVITY_STOMP_COOLDOWN;
            case "black_hole" -> BLACK_HOLE_COOLDOWN;
            case "momentum_lock" -> MOMENTUM_LOCK_COOLDOWN;
            case "crushing_gravity" -> CRUSHING_GRAVITY_COOLDOWN;
            default -> ACCELERATION_COOLDOWN;
        };
        RoyalAttackLane lane = "black_hole".equals(selected) ? RoyalAttackLane.CENTER_HEAD : RoyalAttackLane.HAZARD;
        if (!this.beginRoyalAttack(selected, lane, this.cooldown(cooldown),
                10, 1, 14, new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                    @Override public void onStart() {
                        if ("gravity_stomp".equals(selected)) {
                            QueenEntity.this.triggerAnim("body_action", "stomp");
                        } else if ("momentum_lock".equals(selected)) {
                            QueenEntity.this.triggerAnim("wing_action", "wing_gust");
                        } else if ("black_hole".equals(selected)) {
                            QueenEntity.this.royalHead(RoyalHead.Slot.CENTER).startShoot();
                            QueenEntity.this.triggerAnim(RoyalHead.Slot.CENTER.controllerName(), "shoot");
                        }
                    }
                    @Override public void onActive(int elapsedTicks) {
                        QueenEntity.this.performSelectedQueenAttack(selected, cooldown, level, target);
                    }
                    @Override public void onComplete() {
                        if ("black_hole".equals(selected)) {
                            QueenEntity.this.royalHead(RoyalHead.Slot.CENTER).stopShoot();
                        }
                    }
                })) return;
    }

    private void performSelectedQueenAttack(String selected, int cooldown,
                                            ServerLevel level, LivingEntity target) {
        switch (selected) {
            case "gravity_stomp" -> { this.gravityStompCooldownTicks = cooldown; this.performGravityStomp(level, target); }
            case "black_hole" -> { this.blackHoleCooldownTicks = cooldown; this.castBlackHole(level, target); }
            case "momentum_lock" -> { this.momentumLockCooldownTicks = cooldown; this.startMomentumLock(level); }
            case "crushing_gravity" -> {
                this.crushingGravityCooldownTicks = cooldown;
                this.royalEffects.start("crushing_gravity", CRUSHING_GRAVITY_DURATION,
                        () -> {}, this::tickCrushingGravity, () -> {});
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.6F);
            }
            case "acceleration" -> { this.accelerationCooldownTicks = cooldown; this.startRoyalAcceleration(); }
        }
    }

    private boolean queenAttackReady(String id, Phase phase, boolean majorBusy, boolean allowOverlap) {
        if (!allowOverlap && majorBusy) return false;
        return switch (id) {
            case "gravity_stomp" -> this.gravityStompCooldownTicks <= 0;
            case "black_hole" -> this.blackHoleCooldownTicks <= 0
                    && this.royalHead(RoyalHead.Slot.CENTER).readyToAttack()
                    && !this.attackScheduler.laneBusy(RoyalAttackLane.CENTER_HEAD);
            case "momentum_lock" -> !this.royalEffects.active("momentum_lock") && this.momentumLockCooldownTicks <= 0;
            case "crushing_gravity" -> phase != Phase.ONE && !this.royalEffects.active("crushing_gravity") && this.crushingGravityCooldownTicks <= 0;
            case "acceleration" -> phase == Phase.THREE && !this.royalEffects.active("acceleration") && this.accelerationCooldownTicks <= 0;
            default -> false;
        };
    }

    private boolean isMomentumLockAvailableEarly() {
        return this.getMaxHealth() > 0.0F
                && this.getHealth() / this.getMaxHealth() <= MOMENTUM_LOCK_EARLY_HEALTH_THRESHOLD;
    }

    private void tickFinalAcceleration() {
        if (this.phase() != Phase.THREE) {
            return;
        }
        if (!this.royalEffects.active("acceleration") && this.accelerationCooldownTicks <= 0) {
            this.startRoyalAcceleration();
        }
    }

    private void performGravityStomp(ServerLevel level, LivingEntity target) {
        this.triggerAnim("body_action", "stomp");
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.7F);
        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(18.0D), this::canDamageWithRoyalAttack)) {
            double distance = Math.max(1.0D, living.distanceTo(this));
            living.hurt(source, (float) (12.0D * Math.max(0.25D, 1.0D - distance / 24.0D)));
            Vec3 push = living.position().subtract(this.position()).normalize().scale(1.3D);
            living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.65D, push.z));
            living.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(),
                18, 5.0D, 0.6D, 5.0D, 0.08D);
    }

    private void castBlackHole(ServerLevel level, LivingEntity target) {
        double vertical = 2.0D + this.random.nextDouble() * 2.0D;
        if (AntarchyGravityApi.isGravityInverted(this)) {
            vertical = -vertical;
        }
        Vec3 anchor = target.position().add(
                (this.random.nextDouble() - 0.5D) * 4.0D,
                vertical,
                (this.random.nextDouble() - 0.5D) * 4.0D);
        Vec3 mouth = this.beamAnchor(this.royalHead(RoyalHead.Slot.CENTER));
        RoyalBlackHoleEntity hole = RoyalBlackHoleEntity.createProjectile(level, mouth, anchor, this.getUUID());
        level.addFreshEntity(hole);
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 1.0F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, mouth.x, mouth.y, mouth.z,
                30, 0.6D, 0.6D, 0.6D, 0.25D);
    }

    private void startMomentumLock(ServerLevel level) {
        this.momentumLockCooldownTicks = MOMENTUM_LOCK_COOLDOWN;
        this.royalEffects.start("momentum_lock", MOMENTUM_LOCK_DURATION, () -> {
            this.frozenVelocities.clear();
            for (Entity entity : level.getEntitiesOfClass(Entity.class,
                    this.getBoundingBox().inflate(MOMENTUM_LOCK_RADIUS))) {
                if (entity == this || isFieldImmune(entity) || entity instanceof Player player && player.isCreative()) {
                    continue;
                }
                this.frozenVelocities.put(entity.getUUID(), entity.getDeltaMovement());
            }
        }, this::tickMomentumLockEffect, () -> {
            for (Map.Entry<UUID, Vec3> entry : this.frozenVelocities.entrySet()) {
                Entity entity = level.getEntity(entry.getKey());
                if (entity != null && entity.isAlive()) {
                    entity.setDeltaMovement(entry.getValue());
                    entity.hasImpulse = true;
                }
            }
            this.frozenVelocities.clear();
        });
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.5F);
    }

    private void tickMomentumLockEffect() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        for (Iterator<Map.Entry<UUID, Vec3>> iterator = this.frozenVelocities.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<UUID, Vec3> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
                continue;
            }
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0.0F;
            entity.hasImpulse = true;
            if (this.tickCount % 6 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        }
    }

    private void tickCrushingGravity() {
        if (!this.royalEffects.active("crushing_gravity")) {
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        for (Entity entity : level.getEntitiesOfClass(Entity.class,
                this.getBoundingBox().inflate(CRUSHING_GRAVITY_RADIUS))) {
            if (entity == this || isFieldImmune(entity) || entity instanceof Player player && player.isCreative()) {
                continue;
            }
            double downSign = AntarchyGravityApi.isGravityInverted(entity) ? 1.0D : -1.0D;
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, downSign * CRUSHING_GRAVITY_STRENGTH, 0.0D));
            entity.hasImpulse = true;
        }
        if (this.tickCount % 8 == 0) {
            level.sendParticles(ACCEL_DUST, this.getX(), this.getY() + 1.0D, this.getZ(), 20,
                    CRUSHING_GRAVITY_RADIUS * 0.5D, 1.0D, CRUSHING_GRAVITY_RADIUS * 0.5D, 0.0D);
        }
    }

    private void startRoyalAcceleration() {
        this.accelerationCooldownTicks = ACCELERATION_COOLDOWN;
        this.royalEffects.start("acceleration", ACCELERATION_DURATION, () -> {
            this.setRoyalAccelerated(true);
            this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 1.4F);
            applyAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
            applyAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
        }, this::tickRoyalAccelerationEffect, () -> {
            this.setRoyalAccelerated(false);
            removeAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
            removeAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
        });
    }

    private static void applyAccelModifier(AttributeInstance attribute, ResourceLocation id) {
        if (attribute == null || attribute.getModifier(id) != null) {
            return;
        }
        attribute.addTransientModifier(new AttributeModifier(id, 0.9D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removeAccelModifier(AttributeInstance attribute, ResourceLocation id) {
        if (attribute != null) {
            attribute.removeModifier(id);
        }
    }

    private void tickRoyalAccelerationEffect() {
        if (!this.isRoyalAccelerated()) {
            return;
        }
        if (this.level() instanceof ServerLevel level && this.tickCount % 2 == 0) {
            level.sendParticles(ACCEL_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * this.getBbHeight(),
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    6, 0.4D, 0.6D, 0.4D, 0.02D);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            DimensionalTearEntity.discardQueenOwnedTears(level, this.getUUID());
        }
        this.royalEffects.clear();
        this.frozenVelocities.clear();
        removeAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
        removeAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
        super.die(damageSource);
    }

    private boolean spawnManticore(ServerLevel serverLevel, LivingEntity target) {
        double minimumDistance = Math.max(4.0D, this.getBbWidth() * 0.55D);
        double maximumDistance = Math.max(minimumDistance + 1.0D, AntarchySettings.queenManticoreSummonRange());
        for (int attempt = 0; attempt < POSITION_ATTEMPTS_PER_MANTICORE; attempt++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double distance = Mth.lerp(this.random.nextDouble(), minimumDistance, maximumDistance);
            double x = this.getX() + Math.cos(angle) * distance;
            double y = this.getY() + (this.random.nextDouble() - 0.5D) * 2.0D;
            double z = this.getZ() + Math.sin(angle) * distance;
            ManticoreEntity manticore = AntarchyObjects.MANTICORE.get().create(serverLevel);
            if (manticore == null) {
                return false;
            }
            manticore.moveTo(x, y, z, this.random.nextFloat() * 360.0F, 0.0F);
            BlockPos spawnPos = manticore.blockPosition();
            if (!serverLevel.noCollision(manticore)
                    || !serverLevel.isEmptyBlock(spawnPos)
                    || !ManticoreEntity.canSpawn(AntarchyObjects.MANTICORE.get(), serverLevel, MobSpawnType.MOB_SUMMONED, spawnPos, this.random)) {
                manticore.discard();
                continue;
            }
            manticore.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null);
            manticore.markQueenSummoned(this.getUUID());
            manticore.setTarget(target);
            serverLevel.addFreshEntity(manticore);
            return true;
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(SUMMON_COOLDOWN_KEY, this.manticoreSummonCooldownTicks);
        tag.putInt("GravityStompCooldownTicks", this.gravityStompCooldownTicks);
        tag.putInt("AccelerationCooldownTicks", this.accelerationCooldownTicks);
        tag.putInt("BlackHoleCooldownTicks", this.blackHoleCooldownTicks);
        if (this.naturalTrailHome != null) {
            tag.putLong("NaturalTrailSiteId", this.naturalTrailSiteId);
            tag.putLong("NaturalTrailHome", this.naturalTrailHome.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.manticoreSummonCooldownTicks = Math.max(0, tag.getInt(SUMMON_COOLDOWN_KEY));
        this.gravityStompCooldownTicks = Math.max(0, tag.getInt("GravityStompCooldownTicks"));
        this.momentumLockCooldownTicks = Math.max(0, tag.getInt("MomentumLockCooldownTicks"));
        this.crushingGravityCooldownTicks = Math.max(0, tag.getInt("CrushingGravityCooldownTicks"));
        this.accelerationCooldownTicks = Math.max(0, tag.getInt("AccelerationCooldownTicks"));
        this.blackHoleCooldownTicks = Math.max(0, tag.getInt("BlackHoleCooldownTicks"));
        if (tag.contains("NaturalTrailHome")) {
            this.naturalTrailSiteId = tag.getLong("NaturalTrailSiteId");
            this.naturalTrailHome = BlockPos.of(tag.getLong("NaturalTrailHome"));
        }
    }
}
