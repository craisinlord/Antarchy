package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class PrincessEntity extends RoyalMountEntity {
    public PrincessEntity(EntityType<? extends PrincessEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RoyalMountEntity.createBaseAttributes()
                .add(Attributes.MAX_HEALTH, 110.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.95D);
    }

    @Override
    protected String geoName() {
        return "princess";
    }

    @Override
    protected EntityType<? extends RoyalBoltEntity> boltType() {
        return AntarchyObjects.ROYAL_BOLT.get();
    }
}
