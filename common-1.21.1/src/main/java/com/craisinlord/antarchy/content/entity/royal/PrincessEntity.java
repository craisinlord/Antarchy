package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class PrincessEntity extends RoyalMountEntity {
    public PrincessEntity(EntityType<? extends PrincessEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RoyalMountEntity.createBaseAttributes(
                AntarchySettings.princessHealth(), AntarchySettings.princessAttackDamage(),
                AntarchySettings.princessMovementSpeed(), AntarchySettings.princessFlyingSpeed(),
                AntarchySettings.princessArmor(), AntarchySettings.princessKnockbackResistance(),
                AntarchySettings.princessFollowRange());
    }

    @Override
    protected String geoName() {
        return "princess";
    }

    @Override
    protected EntityType<? extends RoyalBoltEntity> boltType() {
        return AntarchyObjects.ROYAL_BOLT.get();
    }

    @Override
    protected SoundEvent idleSound() {
        return AntarchySoundEvents.PRINCESS_IDLE.get();
    }

    @Override
    protected SoundEvent biteSound() {
        return AntarchySoundEvents.PRINCESS_BITE.get();
    }

    @Override
    protected SoundEvent shootSound() {
        return AntarchySoundEvents.PRINCESS_SHOOT.get();
    }

    @Override
    protected SoundEvent flySound() {
        return AntarchySoundEvents.PRINCESS_FLY.get();
    }

    @Override
    protected SoundEvent stepSound() {
        return AntarchySoundEvents.PRINCESS_STEP.get();
    }
}
