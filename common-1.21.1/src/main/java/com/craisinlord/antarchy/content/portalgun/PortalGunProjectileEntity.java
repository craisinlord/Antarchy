package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PortalGunProjectileEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> SIDE = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOOTER_ID = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISTANCE = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPAWN_X = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPAWN_Y = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPAWN_Z = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> VELOCITY_X = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VELOCITY_Y = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VELOCITY_Z = SynchedEntityData.defineId(PortalGunProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final int MAX_FLIGHT_TICKS = 80;
    private UUID gunId;
    private boolean spawnSynced;
    public int portalWidth = 1;
    public int portalHeight = 2;

    public PortalGunProjectileEntity(EntityType<? extends PortalGunProjectileEntity> entityType, Level level) {
        super(entityType, level);
        this.configureProjectile();
    }

    public PortalGunProjectileEntity(EntityType<? extends PortalGunProjectileEntity> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
        this.configureProjectile();
    }

    private void configureProjectile() {
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIDE, PortalGunPortalEntity.PortalSide.BLUE.ordinal());
        builder.define(SHOOTER_ID, -1);
        builder.define(DISTANCE, 0);
        builder.define(SPAWN_X, 0);
        builder.define(SPAWN_Y, 0);
        builder.define(SPAWN_Z, 0);
        builder.define(VELOCITY_X, 0.0F);
        builder.define(VELOCITY_Y, 0.0F);
        builder.define(VELOCITY_Z, 0.0F);
    }

    public void configure(PortalGunPortalEntity.PortalSide side, UUID gunId, ItemStack gunStack) {
        this.entityData.set(SIDE, side.ordinal());
        this.gunId = gunId;
        this.setItem(gunStack.copyWithCount(1));
        Entity owner = this.getOwner();
        this.entityData.set(SHOOTER_ID, owner == null ? -1 : owner.getId());
        this.syncSpawnPosition(this.blockPosition());
        this.syncVelocity();
    }

    public PortalGunPortalEntity.PortalSide getPortalSide() {
        return this.entityData.get(SIDE) == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
    }

    public int getShooterId() {
        return this.entityData.get(SHOOTER_ID);
    }

    public int getSyncedDistance() {
        return this.entityData.get(DISTANCE);
    }

    public BlockPos getSyncedSpawnPos() {
        return new BlockPos(this.entityData.get(SPAWN_X), this.entityData.get(SPAWN_Y), this.entityData.get(SPAWN_Z));
    }

    public Vec3 getSyncedVelocity() {
        return new Vec3(this.entityData.get(VELOCITY_X), this.entityData.get(VELOCITY_Y), this.entityData.get(VELOCITY_Z));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.spawnSynced) {
            this.syncSpawnPosition(this.blockPosition());
        }
        this.syncVelocity();
        this.entityData.set(DISTANCE, this.tickCount);
        if (this.tickCount > MAX_FLIGHT_TICKS) {
            this.discard();
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6D) {
            double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            this.setYRot((float) (net.minecraft.util.Mth.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F);
            this.setXRot((float) (-(net.minecraft.util.Mth.atan2(motion.y, horizontal) * (180.0D / Math.PI))));
        }
        if (this.level().isClientSide) {
            Vec3 point = this.position();
            if (this.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE) {
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
            } else {
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.CRIT, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            this.spawnImpactParticles(serverLevel, result.getLocation());
            ItemStack sourceStack = this.resolveSourceStack();
            if (this.getOwner() instanceof Player player && sourceStack.getItem() instanceof PortalGunItem portalGunItem) {
                portalGunItem.handlePortalImpact(serverLevel, player, sourceStack, this.getPortalSide(), result, this.position());
            }
        }
        this.discard();
    }

    private void spawnImpactParticles(ServerLevel level, Vec3 impactPos) {
        if (this.getPortalSide() == PortalGunPortalEntity.PortalSide.BLUE) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, impactPos.x, impactPos.y, impactPos.z, 10, 0.08D, 0.08D, 0.08D, 0.01D);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, impactPos.x, impactPos.y, impactPos.z, 6, 0.04D, 0.04D, 0.04D, 0.01D);
            return;
        }
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, impactPos.x, impactPos.y, impactPos.z, 10, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, impactPos.x, impactPos.y, impactPos.z, 6, 0.04D, 0.04D, 0.04D, 0.01D);
    }

    private void syncSpawnPosition(BlockPos pos) {
        this.entityData.set(SPAWN_X, pos.getX());
        this.entityData.set(SPAWN_Y, pos.getY());
        this.entityData.set(SPAWN_Z, pos.getZ());
        this.spawnSynced = true;
    }

    private void syncVelocity() {
        Vec3 velocity = this.getDeltaMovement();
        this.entityData.set(VELOCITY_X, (float) velocity.x);
        this.entityData.set(VELOCITY_Y, (float) velocity.y);
        this.entityData.set(VELOCITY_Z, (float) velocity.z);
    }

    private ItemStack resolveSourceStack() {
        ItemStack projectileStack = this.getItem();
        if (!(this.getOwner() instanceof Player player) || !(projectileStack.getItem() instanceof PortalGunItem portalGunItem) || this.gunId == null) {
            return projectileStack;
        }
        ItemStack matching = portalGunItem.findMatchingGunStack(player, this.gunId);
        return matching.isEmpty() ? projectileStack : matching;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Side", this.entityData.get(SIDE));
        tag.putInt("ShooterId", this.entityData.get(SHOOTER_ID));
        tag.putInt("Distance", this.entityData.get(DISTANCE));
        tag.putInt("SpawnX", this.entityData.get(SPAWN_X));
        tag.putInt("SpawnY", this.entityData.get(SPAWN_Y));
        tag.putInt("SpawnZ", this.entityData.get(SPAWN_Z));
        tag.putFloat("VelocityX", this.entityData.get(VELOCITY_X));
        tag.putFloat("VelocityY", this.entityData.get(VELOCITY_Y));
        tag.putFloat("VelocityZ", this.entityData.get(VELOCITY_Z));
        tag.putInt("PortalWidth", this.portalWidth);
        tag.putInt("PortalHeight", this.portalHeight);
        if (this.gunId != null) {
            tag.putUUID("GunId", this.gunId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SIDE, tag.getInt("Side"));
        this.entityData.set(SHOOTER_ID, tag.getInt("ShooterId"));
        this.entityData.set(DISTANCE, tag.getInt("Distance"));
        this.entityData.set(SPAWN_X, tag.getInt("SpawnX"));
        this.entityData.set(SPAWN_Y, tag.getInt("SpawnY"));
        this.entityData.set(SPAWN_Z, tag.getInt("SpawnZ"));
        this.entityData.set(VELOCITY_X, tag.getFloat("VelocityX"));
        this.entityData.set(VELOCITY_Y, tag.getFloat("VelocityY"));
        this.entityData.set(VELOCITY_Z, tag.getFloat("VelocityZ"));
        this.portalWidth = Math.max(1, tag.getInt("PortalWidth"));
        this.portalHeight = Math.max(2, tag.getInt("PortalHeight"));
        this.spawnSynced = true;
        if (tag.hasUUID("GunId")) {
            this.gunId = tag.getUUID("GunId");
        }
        ItemStack itemStack = this.getItem();
        if (this.gunId != null && !itemStack.isEmpty()) {
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, itemStack, customData -> customData.putUUID(PortalGunItem.GUN_ID_TAG, this.gunId));
        }
    }
}
