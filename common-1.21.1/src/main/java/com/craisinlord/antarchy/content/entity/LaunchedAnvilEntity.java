package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class LaunchedAnvilEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<BlockState> ANVIL_STATE = SynchedEntityData.defineId(LaunchedAnvilEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> FALLING = SynchedEntityData.defineId(LaunchedAnvilEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String ANVIL_STATE_TAG = "AnvilState";
    private static final String FALLING_TAG = "Falling";
    private static final String PEAK_Y_TAG = "PeakY";
    private static final String DESTROY_ON_LANDING_TAG = "DestroyOnLanding";
    private static final int MAX_LIFETIME_TICKS = 600;
    private static final double GRAVITY = 0.07D;
    private static final float CRUSH_DAMAGE_PER_BLOCK = 2.0F;
    private static final float CRUSH_DAMAGE_MAX = 40.0F;

    private final Set<UUID> crushed = new HashSet<>();
    private double peakY = Double.NaN;
    private boolean destroyOnLanding;
    private boolean landed;

    public LaunchedAnvilEntity(EntityType<? extends LaunchedAnvilEntity> entityType, Level level) {
        super(entityType, level);
    }

    public void setAnvilState(BlockState state) {
        this.entityData.set(ANVIL_STATE, state);
    }

    public BlockState getAnvilState() {
        return this.entityData.get(ANVIL_STATE);
    }

    public boolean isFalling() {
        return this.entityData.get(FALLING);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ANVIL_STATE, Blocks.ANVIL.defaultBlockState());
        builder.define(FALLING, false);
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.tickCount > MAX_LIFETIME_TICKS) {
            this.land(this.blockPosition());
            return;
        }
        if (Double.isNaN(this.peakY) || this.getY() > this.peakY) {
            this.peakY = this.getY();
        }
        if (this.isFalling()) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(0.0D, Math.min(0.0D, motion.y), 0.0D);
        }
        if (!this.level().isClientSide && !this.landed) {
            this.sweepEntities();
        }
        super.tick();
    }

    private void sweepEntities() {
        AABB swept = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(0.1D);
        List<Entity> targets = this.level().getEntities(this, swept, this::canHitEntity);
        if (targets.isEmpty()) {
            return;
        }
        if (this.isFalling()) {
            for (Entity target : targets) {
                this.onHitEntity(new EntityHitResult(target));
            }
            return;
        }
        Entity nearest = targets.stream()
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(this)))
                .orElseThrow();
        this.onHitEntity(new EntityHitResult(nearest));
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!(this.level() instanceof ServerLevel serverLevel) || this.landed) {
            return;
        }

        Entity target = hitResult.getEntity();
        if (this.isFalling()) {
            this.crush(serverLevel, target);
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        double launchVelocity = Math.max(0.1D, AntarchySettings.anvilCannonLaunchVelocity());
        float speedScale = (float) Mth.clamp(motion.length() / launchVelocity, 0.35D, 1.25D);
        float damage = (float) AntarchySettings.anvilCannonImpactDamage() * speedScale;
        if (target.hurt(AntarchyDamageSources.anvilCannon(serverLevel, this, this.getOwner()), damage) && target instanceof LivingEntity living) {
            Vec3 push = new Vec3(motion.x, 0.0D, motion.z);
            double knockback = AntarchySettings.anvilCannonImpactKnockback() * speedScale;
            if (push.lengthSqr() > 1.0E-4D && knockback > 0.0D) {
                push = push.normalize().scale(knockback);
                double resistance = Math.max(0.0D, 1.0D - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                living.push(push.x * resistance, 0.35D * resistance, push.z * resistance);
                living.hurtMarked = true;
            }
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2F, 0.7F + this.random.nextFloat() * 0.2F);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.getAnvilState()), this.getX(), this.getY(0.5D), this.getZ(), 24, 0.3D, 0.3D, 0.3D, 0.15D);
        this.startFalling();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (this.level().isClientSide || this.landed) {
            return;
        }

        if (hitResult.getDirection() == Direction.UP) {
            this.land(hitResult.getBlockPos().above());
            return;
        }
        if (!this.isFalling()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.8F, 0.8F + this.random.nextFloat() * 0.2F);
            this.startFalling();
        }
    }

    private void startFalling() {
        if (this.isFalling()) {
            return;
        }
        if (AntarchySettings.anvilCannonDegradeOnImpact()) {
            BlockState degraded = AnvilBlock.damage(this.getAnvilState());
            if (degraded == null) {
                this.destroyOnLanding = true;
            } else {
                this.setAnvilState(degraded);
            }
        }
        this.entityData.set(FALLING, true);
        this.peakY = this.getY();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, Math.min(0.0D, motion.y), 0.0D);
    }

    private float crushDamage() {
        int blocks = Mth.ceil(Math.max(0.0D, this.peakY - this.getY()) - 1.0D);
        if (blocks <= 0) {
            return 0.0F;
        }
        return Math.min(Mth.floor(blocks * CRUSH_DAMAGE_PER_BLOCK), CRUSH_DAMAGE_MAX);
    }

    private void crush(ServerLevel level, Entity target) {
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target) || !EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(target) || !this.crushed.add(target.getUUID())) {
            return;
        }
        float damage = this.crushDamage();
        if (damage > 0.0F) {
            target.hurt(AntarchyDamageSources.anvilCannon(level, this, this.getOwner()), damage);
        }
    }

    private void land(BlockPos pos) {
        this.landed = true;
        Level level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            AABB cell = new AABB(pos);
            for (Entity victim : level.getEntities(this, cell.minmax(this.getBoundingBox()))) {
                this.crush(serverLevel, victim);
            }
        }

        if (!this.isFalling() && AntarchySettings.anvilCannonDegradeOnImpact()) {
            BlockState degraded = AnvilBlock.damage(this.getAnvilState());
            if (degraded == null) {
                this.destroyOnLanding = true;
            } else {
                this.setAnvilState(degraded);
            }
        }

        if (this.destroyOnLanding) {
            level.levelEvent(1029, pos, 0);
            this.discard();
            return;
        }

        BlockState state = this.getAnvilState();
        BlockState existing = level.getBlockState(pos);
        if (level.isInWorldBounds(pos) && (existing.isAir() || existing.canBeReplaced()) && state.canSurvive(level, pos)) {
            level.setBlock(pos, state, 3);
            level.levelEvent(1031, pos, 0);
        } else {
            this.spawnAtLocation(new ItemStack(state.getBlock()));
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put(ANVIL_STATE_TAG, NbtUtils.writeBlockState(this.getAnvilState()));
        tag.putBoolean(FALLING_TAG, this.isFalling());
        tag.putBoolean(DESTROY_ON_LANDING_TAG, this.destroyOnLanding);
        if (!Double.isNaN(this.peakY)) {
            tag.putDouble(PEAK_Y_TAG, this.peakY);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(ANVIL_STATE_TAG)) {
            this.setAnvilState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompound(ANVIL_STATE_TAG)));
        }
        this.entityData.set(FALLING, tag.getBoolean(FALLING_TAG));
        this.destroyOnLanding = tag.getBoolean(DESTROY_ON_LANDING_TAG);
        if (tag.contains(PEAK_Y_TAG)) {
            this.peakY = tag.getDouble(PEAK_Y_TAG);
        }
    }
}
