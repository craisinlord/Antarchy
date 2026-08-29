package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class PrinceEntity extends RoyalMountEntity {
    public PrinceEntity(EntityType<? extends PrinceEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RoyalMountEntity.createBaseAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D);
    }

    @Override
    protected String geoName() {
        return "prince";
    }

    @Override
    protected EntityType<? extends RoyalBoltEntity> boltType() {
        return AntarchyObjects.ROYAL_BOLT.get();
    }
}
