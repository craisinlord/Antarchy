package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.advancement.AntarchyAdvancements;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SizeRayProjectileEntity extends AbstractArrow {

    private static final String CHARGE_LEVEL_KEY = "ChargeLevel";
    private static final int MAX_LIFETIME_TICKS = 20;
    private static final ResourceLocation VERY_HUNGRY_CATERPILLAR_ADVANCEMENT =
            new ResourceLocation(Antarchy.MODID, "very_hungry_caterpillar");
    private static final EntityDataAccessor<Integer> CHARGE_LEVEL =
            SynchedEntityData.defineId(SizeRayProjectileEntity.class, EntityDataSerializers.INT);

    private final SizeRayType sizeRayType;

    public SizeRayProjectileEntity(
            EntityType<? extends SizeRayProjectileEntity> entityType,
            Level level,
            SizeRayType sizeRayType
    ) {
        super(entityType, level);
        this.sizeRayType = sizeRayType;
        this.configureAsRay();
        this.setChargeLevel(1);
    }

    public SizeRayProjectileEntity(
            EntityType<? extends SizeRayProjectileEntity> entityType,
            LivingEntity shooter,
            Level level,
            ItemStack firedFromWeapon,
            SizeRayType sizeRayType
    ) {
        super(entityType, shooter, level);
        this.sizeRayType = sizeRayType;
        this.configureAsRay();
        this.setChargeLevel(1);
    }

    public static SizeRayProjectileEntity createShrink(
            EntityType<SizeRayProjectileEntity> entityType, Level level
    ) {
        return new SizeRayProjectileEntity(entityType, level, SizeRayType.SHRINK);
    }

    public static SizeRayProjectileEntity createGrowth(
            EntityType<SizeRayProjectileEntity> entityType, Level level
    ) {
        return new SizeRayProjectileEntity(entityType, level, SizeRayType.GROWTH);
    }

    public SizeRayType getSizeRayType() {
        return this.sizeRayType;
    }

    public int getChargeLevel() {
        return this.entityData.get(CHARGE_LEVEL);
    }

    public int getEffectiveChargeLevel() {
        return Math.max(1, Math.min(this.getChargeLevel(), this.getMaximumUsefulChargeLevel()));
    }

    public void setChargeLevel(int chargeLevel) {
        this.entityData.set(CHARGE_LEVEL, Math.max(1, chargeLevel));
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide()
                && AntarchySettings.sizeChangingRaysEnabled()
                && result.getEntity() instanceof LivingEntity livingEntity
                && !livingEntity.getType().is(AntarchyTags.Entities.SIZE_CHANGING_IMMUNE)) {
            applyScaleChange(livingEntity);
            if (this.sizeRayType == SizeRayType.GROWTH
                    && livingEntity instanceof CaterpillarEntity
                    && this.getOwner() instanceof ServerPlayer serverPlayer) {
                AntarchyAdvancements.award(serverPlayer, VERY_HUNGRY_CATERPILLAR_ADVANCEMENT);
            }
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }

        super.tick();

        if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    private void configureAsRay() {
        this.pickup = Pickup.DISALLOWED;
        this.setNoGravity(true);
        this.setBaseDamage(0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGE_LEVEL, 1);
    }

    
    private void applyScaleChange(LivingEntity livingEntity) {
        this.applyScaleChange(livingEntity, this.getEffectiveChargeLevel());
    }

    private void applyScaleChange(LivingEntity livingEntity, int chargeLevel) {
        virtuoel.pehkui.api.ScaleData scaleData = virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(livingEntity);
        double current = scaleData.getTargetScale();
        double delta = AntarchySettings.sizeRayDeltaPerHit() * chargeLevel;
        double next = this.sizeRayType == SizeRayType.SHRINK ? current - delta : current + delta;
        next = Mth.clamp(next, AntarchySettings.sizeRayMinScale(), AntarchySettings.sizeRayMaxScale());
        scaleData.setTargetScale((float) next);
    }

    public int getMaximumUsefulChargeLevel() {
        double delta = AntarchySettings.sizeRayDeltaPerHit();
        double availableOffset = this.sizeRayType == SizeRayType.SHRINK
                ? 1.0D - AntarchySettings.sizeRayMinScale()
                : AntarchySettings.sizeRayMaxScale() - 1.0D;
        return Math.max(1, (int) Math.ceil(availableOffset / delta));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(CHARGE_LEVEL_KEY, this.getChargeLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setChargeLevel(Math.max(1, tag.getInt(CHARGE_LEVEL_KEY)));
    }

    public static void resetScale(LivingEntity livingEntity) {
        virtuoel.pehkui.api.ScaleTypes.BASE.getScaleData(livingEntity).setTargetScale(1.0F);
    }

    public enum SizeRayType {
        SHRINK,
        GROWTH
    }
}
