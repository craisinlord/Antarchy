package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class QueenEntity extends RoyalBossEntity {
    private static final String SUMMON_COOLDOWN_KEY = "ManticoreSummonCooldownTicks";
    private static final int FAILED_SUMMON_RETRY_TICKS = 20;
    private static final int POSITION_ATTEMPTS_PER_MANTICORE = 8;
    private int manticoreSummonCooldownTicks;
    private int gravityStompCooldownTicks;
    private int timeFieldCooldownTicks;

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

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.isDeadOrDying()) {
            return;
        }
        this.tickManticoreSummon();
        this.tickGravityAttacks();
    }

    private void tickManticoreSummon() {
        if (this.manticoreSummonCooldownTicks > 0) {
            this.manticoreSummonCooldownTicks--;
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
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
                ? Math.max(20, AntarchySettings.queenManticoreSummonCooldownTicks())
                : FAILED_SUMMON_RETRY_TICKS;
        if (spawned > 0) {
            this.triggerAnim("body_action", "minion_spawn");
            this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.0F, 0.8F + this.random.nextFloat() * 0.12F);
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 2.0D, this.getZ(),
                    36, 3.0D, 2.0D, 3.0D, 0.15D);
        }
    }

    private int spawnManticoreTears(ServerLevel level, net.minecraft.world.entity.LivingEntity target, int count) {
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

    private void tickGravityAttacks() {
        if (this.gravityStompCooldownTicks > 0) {
            this.gravityStompCooldownTicks--;
        }
        if (this.timeFieldCooldownTicks > 0) {
            this.timeFieldCooldownTicks--;
        }
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (this.gravityStompCooldownTicks <= 0) {
            this.gravityStompCooldownTicks = 180;
            this.triggerAnim("body_action", "stomp");
            this.playSound(AntarchySoundEvents.QUEEN_ROAR.get(), 3.0F, 0.7F);
            DamageSource source = this.damageSources().mobAttack(this);
            for (net.minecraft.world.entity.LivingEntity living : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                    this.getBoundingBox().inflate(18.0D), entity -> entity instanceof Player && entity.isAlive())) {
                double distance = Math.max(1.0D, living.distanceTo(this));
                living.hurt(source, (float) (12.0D * Math.max(0.25D, 1.0D - distance / 24.0D)));
                Vec3 push = living.position().subtract(this.position()).normalize().scale(1.3D);
                living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.65D, push.z));
                living.hasImpulse = true;
            }
        }
        if (this.timeFieldCooldownTicks <= 0) {
            this.timeFieldCooldownTicks = 260;
            TimeDilationApi.createField(level, target.position(), 12.0D, 0.45D, 140);
            this.triggerAnim("body_action", "wing_gust");
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            DimensionalTearEntity.discardQueenOwnedTears(level, this.getUUID());
        }
        super.die(damageSource);
    }

    private boolean spawnManticore(ServerLevel serverLevel, net.minecraft.world.entity.LivingEntity target) {
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.manticoreSummonCooldownTicks = Math.max(0, tag.getInt(SUMMON_COOLDOWN_KEY));
        this.gravityStompCooldownTicks = Math.max(0, tag.getInt("GravityStompCooldownTicks"));
        this.timeFieldCooldownTicks = Math.max(0, tag.getInt("TimeFieldCooldownTicks"));
    }
}
