package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyAdvancements;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SizeRayProjectileEntity extends AbstractArrow {
    
    public static final ResourceLocation SIZE_RAY_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "size_ray_scale");
    private static final ResourceLocation LEGACY_SHRINK_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "shrink_ray_scale");
    private static final ResourceLocation LEGACY_GROW_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "grow_ray_scale");
    private static final String STAGE_TAG_PREFIX = Antarchy.MODID + ":size_ray_stage_";
    private static final String CHARGE_LEVEL_KEY = "ChargeLevel";
    private static final int LEGACY_STAGE_RANGE = 3;
    private static final int MAX_LIFETIME_TICKS = 20;
    private static final ResourceLocation VERY_HUNGRY_CATERPILLAR_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "very_hungry_caterpillar");
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
        super(entityType, shooter, level, firedFromWeapon.copyWithCount(1), firedFromWeapon);
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
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGE_LEVEL, 1);
    }

    
    private void applyScaleChange(LivingEntity livingEntity) {
        this.applyScaleChange(livingEntity, this.getEffectiveChargeLevel());
    }

    private void applyScaleChange(LivingEntity livingEntity, int chargeLevel) {
        AttributeInstance scaleAttribute = livingEntity.getAttribute(Attributes.SCALE);
        if (scaleAttribute == null) return;

        migrateLegacy(livingEntity, scaleAttribute);
        AttributeModifier existing = scaleAttribute.getModifier(SIZE_RAY_MODIFIER_ID);
        double current = existing != null ? existing.amount() : 0.0;

        double delta = AntarchySettings.sizeRayDeltaPerHit();
        double snappedCurrent = Math.round(current / delta) * delta;
        double next = this.sizeRayType == SizeRayType.SHRINK
                ? snappedCurrent - delta * chargeLevel
                : snappedCurrent + delta * chargeLevel;
        double minOffset = AntarchySettings.sizeRayMinScale() - 1.0;
        double maxOffset = AntarchySettings.sizeRayMaxScale() - 1.0;
        next = Mth.clamp(next, minOffset, maxOffset);

        writeModifier(scaleAttribute, next);
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

    
    public static void writeModifier(AttributeInstance scaleAttribute, double offset) {
        scaleAttribute.removeModifier(SIZE_RAY_MODIFIER_ID);
        if (offset != 0.0) {
            scaleAttribute.addPermanentModifier(
                    new AttributeModifier(
                            SIZE_RAY_MODIFIER_ID,
                            offset,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            );
        }
    }

    
    private static void migrateLegacy(LivingEntity livingEntity, AttributeInstance scaleAttribute) {
        scaleAttribute.removeModifier(LEGACY_SHRINK_MODIFIER_ID);
        scaleAttribute.removeModifier(LEGACY_GROW_MODIFIER_ID);
        for (int i = -LEGACY_STAGE_RANGE; i <= LEGACY_STAGE_RANGE; i++) {
            if (i != 0) livingEntity.removeTag(STAGE_TAG_PREFIX + i);
        }
    }

    public enum SizeRayType {
        SHRINK,
        GROWTH
    }
}
