package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class PrinceEntity extends RoyalMountEntity {
    public PrinceEntity(EntityType<? extends PrinceEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RoyalMountEntity.createBaseAttributes(
                AntarchySettings.princeHealth(), AntarchySettings.princeAttackDamage(),
                AntarchySettings.princeMovementSpeed(), AntarchySettings.princeFlyingSpeed(),
                AntarchySettings.princeArmor(), AntarchySettings.princeKnockbackResistance(),
                AntarchySettings.princeFollowRange());
    }

    @Override
    protected RoyalBeamElement boltElement(int lateralIndex) {
        return switch (lateralIndex) {
            case -1 -> RoyalBeamElement.FIRE;
            case 1 -> RoyalBeamElement.ICE;
            default -> RoyalBeamElement.LIGHTNING;
        };
    }

    @Override
    protected String geoName() {
        return "prince";
    }

    @Override
    protected EntityType<? extends RoyalBoltEntity> boltType() {
        return AntarchyObjects.ROYAL_BOLT.get();
    }

    @Override
    protected SoundEvent idleSound() {
        return AntarchySoundEvents.PRINCE_IDLE.get();
    }

    @Override
    protected SoundEvent biteSound() {
        return AntarchySoundEvents.PRINCE_BITE.get();
    }

    @Override
    protected SoundEvent shootSound() {
        return AntarchySoundEvents.PRINCE_SHOOT.get();
    }

    @Override
    protected SoundEvent flySound() {
        return AntarchySoundEvents.PRINCE_FLY.get();
    }

    @Override
    protected SoundEvent stepSound() {
        return AntarchySoundEvents.PRINCE_STEP.get();
    }
}
