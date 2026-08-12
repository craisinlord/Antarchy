package com.craisinlord.antarchy.content.entity.kraken;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.KrakensGraspItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class KrakensGraspThrownTrident extends AbstractArrow implements ItemSupplier {
    private boolean dealtDamage;
    private boolean returning;

    public KrakensGraspThrownTrident(EntityType<? extends KrakensGraspThrownTrident> entityType, Level level) {
        super(entityType, level);
    }

    public KrakensGraspThrownTrident(EntityType<? extends KrakensGraspThrownTrident> entityType, LivingEntity owner, Level level, ItemStack stack) {
        super(entityType, owner, level);
        this.setBaseDamage(AntarchySettings.krakensGraspThrownDamage());
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(AntarchyObjects.KRAKENS_GRASP.get());
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(AntarchyObjects.KRAKENS_GRASP.get());
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity owner = this.getOwner();
        int loyalty = this.effectiveLoyaltyLevel();
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && owner instanceof LivingEntity livingOwner && livingOwner.isAlive()) {
            this.setNoPhysics(true);
            Vec3 toOwner = owner.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015D * loyalty, this.getZ());
            if (this.level().isClientSide) {
                this.yOld = this.getY();
            }

            double speed = 0.05D * loyalty;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(toOwner.normalize().scale(speed)));
            if (!this.returning) {
                this.returning = true;
                this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
            }
        }

        super.tick();
    }

    private int effectiveLoyaltyLevel() {
        return AntarchySettings.krakensGraspInnateLoyalty() ? Mth.clamp(AntarchySettings.krakensGraspInnateLoyaltyLevel(), 1, 3) : 0;
    }

    @Override
    protected net.minecraft.world.phys.EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = (float) this.getBaseDamage();
        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner != null ? owner : this);

        this.dealtDamage = true;

        if (target.hurt(damageSource, damage) && target.getType() != EntityType.ENDERMAN) {
            if (target instanceof LivingEntity livingTarget) {
                this.doPostHurtEffects(livingTarget);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);

        if (target instanceof LivingEntity livingTarget && this.level() instanceof ServerLevel serverLevel) {
            KrakensGraspItem.strikeLightning(livingTarget, serverLevel);
            TentacleEntity.spawnAt(serverLevel, new Vec3(target.getX(), target.getY(), target.getZ()),
                    owner instanceof LivingEntity livingOwner ? livingOwner : null);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = result.getLocation();
            KrakensGraspItem.strikeLightningAt(serverLevel, pos.x, pos.y, pos.z);
            TentacleEntity.spawnAt(serverLevel, pos, this.getOwner() instanceof LivingEntity livingOwner ? livingOwner : null);
        }
    }
}
