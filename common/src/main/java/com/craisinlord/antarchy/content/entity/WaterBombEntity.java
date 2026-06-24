package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraft.network.syncher.SynchedEntityData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaterBombEntity extends ThrowableProjectile {

    private int lifetimeTicks;
    private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

    public WaterBombEntity(EntityType<? extends WaterBombEntity> entityType, Level level) {
        super(entityType, level);
        this.lifetimeTicks = AntarchySettings.waterBombLifetimeTicks();
    }

    public WaterBombEntity(Level level, LivingEntity owner) {
        super(AntarchyObjects.WATER_BOMB.get(), owner, level);
        this.lifetimeTicks = AntarchySettings.waterBombLifetimeTicks();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return AntarchySettings.waterBombGravity();
    }

    @Override
    public void tick() {
        super.tick();

        this.hitCooldowns.replaceAll((uuid, cd) -> cd - 1);
        this.hitCooldowns.entrySet().removeIf(e -> e.getValue() <= 0);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRIPPING_WATER, this.getX(), this.getY(), this.getZ(), 3, 0.15D, 0.15D, 0.15D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.01D);
        }

        this.lifetimeTicks--;
        if (this.lifetimeTicks <= 0) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity == this.getOwner()) return;
        if (!(entity instanceof LivingEntity living)) return;

        UUID id = entity.getUUID();
        if (this.hitCooldowns.getOrDefault(id, 0) > 0) return;

        float damage = (float) AntarchySettings.waterBombDamage();
        if (living.hurt(this.damageSources().thrown(this, this.getOwner()), damage)) {
            this.hitCooldowns.put(id, 15);
            Vec3 knockDir = living.position().subtract(this.position()).normalize();
            double knockback = AntarchySettings.waterBombKnockback();
            living.push(knockDir.x * knockback, 0.3D, knockDir.z * knockback);
            living.hurtMarked = true;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockPos hitPos = result.getBlockPos().relative(result.getDirection());
        Level level = this.level();
        if (level.isEmptyBlock(hitPos) && level.getBlockState(hitPos.below()).isSolid()) {
            level.setBlock(hitPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2), 3);
            level.scheduleTick(hitPos, Fluids.WATER, 2);
        }
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LifetimeTicks", this.lifetimeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lifetimeTicks = tag.contains("LifetimeTicks") ? tag.getInt("LifetimeTicks") : AntarchySettings.waterBombLifetimeTicks();
    }
}
