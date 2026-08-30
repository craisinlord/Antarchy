package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class KingEntity extends RoyalBossEntity {
    public KingEntity(EntityType<? extends KingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.kingHealth(), AntarchySettings.kingAttackDamage());
    }

    @Override
    protected String geoName() {
        return "king";
    }

    @Override
    protected boolean isFlyingBoss() {
        return true;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    @Override
    protected SoundEvent royalIdleSound() {
        return AntarchySoundEvents.KING_IDLE.get();
    }

    @Override
    protected SoundEvent royalHurtSound() {
        return AntarchySoundEvents.KING_HURT.get();
    }

    @Override
    protected SoundEvent royalDeathSound() {
        return AntarchySoundEvents.KING_DEATH.get();
    }

    @Override
    protected SoundEvent royalBiteSound() {
        return AntarchySoundEvents.KING_BITE.get();
    }
}
