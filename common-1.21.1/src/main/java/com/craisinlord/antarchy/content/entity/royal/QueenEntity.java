package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import java.util.HashMap;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public class QueenEntity extends RoyalBossEntity {
    private static final String SUMMON_COOLDOWN_KEY = "ManticoreSummonCooldownTicks";
    private static final int FAILED_SUMMON_RETRY_TICKS = 20;
    private static final int POSITION_ATTEMPTS_PER_MANTICORE = 8;

    private static final ResourceLocation ACCEL_SPEED_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "queen_royal_acceleration_speed");
    private static final ResourceLocation ACCEL_FLY_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "queen_royal_acceleration_fly");
    private static final DustParticleOptions ACCEL_DUST = new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.15F, 0.15F), 2.0F);

    private static final int GRAVITY_STOMP_COOLDOWN = 180;
    private static final int TIME_FIELD_COOLDOWN = 260;
    private static final int MOMENTUM_LOCK_COOLDOWN = 420;
    private static final int MOMENTUM_LOCK_DURATION = 90;
    private static final double MOMENTUM_LOCK_RADIUS = 26.0D;
    private static final int CRUSHING_GRAVITY_COOLDOWN = 360;
    private static final int CRUSHING_GRAVITY_DURATION = 120;
    private static final double CRUSHING_GRAVITY_RADIUS = 18.0D;
    private static final double CRUSHING_GRAVITY_STRENGTH = 0.12D;
    private static final int ACCELERATION_COOLDOWN = 500;
    private static final int ACCELERATION_DURATION = 140;
    private static final int BLACK_HOLE_COOLDOWN = 340;

    private int manticoreSummonCooldownTicks;
    private int gravityStompCooldownTicks;
    private int timeFieldCooldownTicks;
    private int momentumLockCooldownTicks;
    private int momentumLockTicks;
    private int crushingGravityCooldownTicks;
    private int crushingGravityTicks;
    private int accelerationCooldownTicks;
    private int accelerationTicks;
    private int blackHoleCooldownTicks;
    private int idleWanderCooldownTicks;
    private final Map<UUID, Vec3> frozenVelocities = new HashMap<>();

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
        return false;
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
                AntarchySettings.queenBeamDurationTicks(), AntarchySettings.queenBeamCooldownTicks(), 6.0F, 6.0F,
                (float) AntarchySettings.queenBeamDamage(), 1.0F, 3, 100.0D,
                (float) AntarchySettings.queenBeamTerrainRadius(), 4.0F, AntarchySettings.queenBeamTerrainCap(),
                1.0F, 0.08F, 15.0F, true, true);
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode() {
        return RoyalBeamTerrainMode.DESTROY;
    }

    public static boolean isFieldImmune(Entity entity) {
        return entity instanceof RoyalBossEntity
                || entity.getType().is(AntarchyTags.Entities.TIME_DILATION_IMMUNE);
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
        this.tickRoyalAcceleration();
        this.tickMomentumLock();
        this.tickCrushingGravity();
    }

    private void tickIdleWander() {
        if (this.idleWanderCooldownTicks-- > 0) {
            return;
        }
        this.idleWanderCooldownTicks = 80 + this.random.nextInt(80);
        double angle = this.random.nextDouble() * Mth.TWO_PI;
        double radius = 6.0D + this.random.nextDouble() * 14.0D;
        double wx = this.getX() + Math.cos(angle) * radius;
        double wz = this.getZ() + Math.sin(angle) * radius;
        this.getMoveControl().setWantedPosition(wx, this.groundYBelow(wx, wz), wz, 0.7D);
    }

    private int cooldown(int base) {
        return this.accelerationTicks > 0 ? Math.max(20, base / 2) : base;
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
            this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.0F, 0.8F + this.random.nextFloat() * 0.12F);
            serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 2.0D, this.getZ(),
                    36, 3.0D, 2.0D, 3.0D, 0.15D);
        }
    }

    private int spawnManticoreTears(ServerLevel level, LivingEntity target, int count) {
        int pairs = Math.max(1, Math.min(3, (count + 1) / 2));
        int created = 0;
        for (int i = 0; i < pairs; i++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double distance = Math.max(8.0D, AntarchySettings.queenManticoreSummonRange() * 0.65D);
            Vec3 firstPos = this.position().add(Math.cos(angle) * distance, 1.0D, Math.sin(angle) * distance);
            Vec3 secondPos = this.position().add(Math.cos(angle + Math.PI) * distance, 1.0D, Math.sin(angle + Math.PI) * distance);
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
        if (this.timeFieldCooldownTicks > 0) this.timeFieldCooldownTicks--;
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
        boolean majorBusy = this.momentumLockTicks > 0 || this.crushingGravityTicks > 0;
        boolean allowOverlap = phase != Phase.ONE;

        if (this.gravityStompCooldownTicks <= 0 && (allowOverlap || !majorBusy)) {
            this.gravityStompCooldownTicks = this.cooldown(GRAVITY_STOMP_COOLDOWN);
            this.performGravityStomp(level, target);
        }
        if (this.timeFieldCooldownTicks <= 0 && (allowOverlap || !majorBusy)) {
            this.timeFieldCooldownTicks = this.cooldown(TIME_FIELD_COOLDOWN);
            double fieldRate = phase == Phase.THREE ? 0.1D : 0.4D;
            TimeDilationApi.createField(level, target.position(), 12.0D, fieldRate, 140 + phase.ordinal() * 40);
            this.triggerAnim("body_action", "wing_gust");
        }
        if (this.blackHoleCooldownTicks <= 0 && (allowOverlap || !majorBusy)) {
            this.blackHoleCooldownTicks = this.cooldown(BLACK_HOLE_COOLDOWN);
            this.castBlackHole(level, target);
        }
        if (this.momentumLockTicks <= 0 && this.momentumLockCooldownTicks <= 0 && (allowOverlap || !majorBusy)) {
            this.startMomentumLock(level);
        }
        if (this.crushingGravityTicks <= 0 && this.crushingGravityCooldownTicks <= 0 && phase != Phase.ONE) {
            this.crushingGravityCooldownTicks = CRUSHING_GRAVITY_COOLDOWN;
            this.crushingGravityTicks = CRUSHING_GRAVITY_DURATION;
            this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.0F, 0.6F);
        }
        if (this.accelerationTicks <= 0 && this.accelerationCooldownTicks <= 0
                && (phase == Phase.THREE || (phase == Phase.TWO && this.random.nextInt(3) == 0))) {
            this.startRoyalAcceleration();
        }
    }

    private void performGravityStomp(ServerLevel level, LivingEntity target) {
        this.triggerAnim("body_action", "stomp");
        this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.0F, 0.7F);
        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(18.0D), entity -> entity instanceof Player && entity.isAlive())) {
            double distance = Math.max(1.0D, living.distanceTo(this));
            living.hurt(source, (float) (12.0D * Math.max(0.25D, 1.0D - distance / 24.0D)));
            Vec3 push = living.position().subtract(this.position()).normalize().scale(1.3D);
            living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.65D, push.z));
            living.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 2.0D, 0.5D, 2.0D, 0.0D);
    }

    private void castBlackHole(ServerLevel level, LivingEntity target) {
        Vec3 anchor = target.position().add(
                (this.random.nextDouble() - 0.5D) * 4.0D,
                2.0D + this.random.nextDouble() * 2.0D,
                (this.random.nextDouble() - 0.5D) * 4.0D);
        RoyalBlackHoleEntity hole = RoyalBlackHoleEntity.create(level, anchor, this.getUUID());
        level.addFreshEntity(hole);
        this.triggerAnim("body_action", "wing_gust");
        this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.0F, 1.15F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, anchor.x, anchor.y, anchor.z, 60, 1.0D, 1.0D, 1.0D, 0.4D);
    }

    private void startMomentumLock(ServerLevel level) {
        this.momentumLockCooldownTicks = MOMENTUM_LOCK_COOLDOWN;
        this.momentumLockTicks = MOMENTUM_LOCK_DURATION;
        this.frozenVelocities.clear();
        this.triggerAnim("body_action", "wing_gust");
        this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.0F, 0.5F);
        for (Entity entity : level.getEntitiesOfClass(Entity.class,
                this.getBoundingBox().inflate(MOMENTUM_LOCK_RADIUS))) {
            if (entity == this || isFieldImmune(entity) || entity instanceof Player player && player.isCreative()) {
                continue;
            }
            this.frozenVelocities.put(entity.getUUID(), entity.getDeltaMovement());
        }
    }

    private void tickMomentumLock() {
        if (this.momentumLockTicks <= 0) {
            return;
        }
        this.momentumLockTicks--;
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        for (Map.Entry<UUID, Vec3> entry : this.frozenVelocities.entrySet()) {
            Entity entity = level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive()) {
                continue;
            }
            if (this.momentumLockTicks <= 0) {
                entity.setDeltaMovement(entry.getValue());
                entity.hasImpulse = true;
            } else {
                entity.setDeltaMovement(Vec3.ZERO);
                entity.fallDistance = 0.0F;
                entity.hasImpulse = true;
                if (this.tickCount % 6 == 0 && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                            2, 0.2D, 0.2D, 0.2D, 0.0D);
                }
            }
        }
        if (this.momentumLockTicks <= 0) {
            this.frozenVelocities.clear();
        }
    }

    private void tickCrushingGravity() {
        if (this.crushingGravityTicks <= 0) {
            return;
        }
        this.crushingGravityTicks--;
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
        this.accelerationTicks = ACCELERATION_DURATION;
        this.setRoyalAccelerated(true);
        this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.5F, 1.4F);
        applyAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
        applyAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
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

    private void tickRoyalAcceleration() {
        if (this.accelerationTicks <= 0) {
            if (this.isRoyalAccelerated()) {
                this.setRoyalAccelerated(false);
                removeAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
                removeAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
            }
            return;
        }
        this.accelerationTicks--;
        if (this.level() instanceof ServerLevel level && this.tickCount % 2 == 0) {
            level.sendParticles(ACCEL_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * this.getBbHeight(),
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    6, 0.4D, 0.6D, 0.4D, 0.02D);
        }
        if (this.accelerationTicks <= 0) {
            this.setRoyalAccelerated(false);
            removeAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
            removeAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            DimensionalTearEntity.discardQueenOwnedTears(level, this.getUUID());
        }
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
        tag.putInt("TimeFieldCooldownTicks", this.timeFieldCooldownTicks);
        tag.putInt("MomentumLockCooldownTicks", this.momentumLockCooldownTicks);
        tag.putInt("CrushingGravityCooldownTicks", this.crushingGravityCooldownTicks);
        tag.putInt("AccelerationCooldownTicks", this.accelerationCooldownTicks);
        tag.putInt("BlackHoleCooldownTicks", this.blackHoleCooldownTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.manticoreSummonCooldownTicks = Math.max(0, tag.getInt(SUMMON_COOLDOWN_KEY));
        this.gravityStompCooldownTicks = Math.max(0, tag.getInt("GravityStompCooldownTicks"));
        this.timeFieldCooldownTicks = Math.max(0, tag.getInt("TimeFieldCooldownTicks"));
        this.momentumLockCooldownTicks = Math.max(0, tag.getInt("MomentumLockCooldownTicks"));
        this.crushingGravityCooldownTicks = Math.max(0, tag.getInt("CrushingGravityCooldownTicks"));
        this.accelerationCooldownTicks = Math.max(0, tag.getInt("AccelerationCooldownTicks"));
        this.blackHoleCooldownTicks = Math.max(0, tag.getInt("BlackHoleCooldownTicks"));
    }
}
