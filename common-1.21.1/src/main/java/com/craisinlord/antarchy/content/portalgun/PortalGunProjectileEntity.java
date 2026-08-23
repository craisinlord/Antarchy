package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import java.util.UUID;
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
    private static final int MAX_FLIGHT_TICKS = 80;
    private UUID gunId;

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
    }

    public void configure(PortalGunPortalEntity.PortalSide side, UUID gunId, ItemStack gunStack) {
        this.entityData.set(SIDE, side.ordinal());
        this.gunId = gunId;
        this.setItem(gunStack.copyWithCount(1));
    }

    public PortalGunPortalEntity.PortalSide getPortalSide() {
        return this.entityData.get(SIDE) == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
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
        if (this.gunId != null) {
            tag.putUUID("GunId", this.gunId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SIDE, tag.getInt("Side"));
        if (tag.hasUUID("GunId")) {
            this.gunId = tag.getUUID("GunId");
        }
        ItemStack itemStack = this.getItem();
        if (this.gunId != null && !itemStack.isEmpty()) {
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA, itemStack, customData -> customData.putUUID(PortalGunItem.GUN_ID_TAG, this.gunId));
        }
    }
}
