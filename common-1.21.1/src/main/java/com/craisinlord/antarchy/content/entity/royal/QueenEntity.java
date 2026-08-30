package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class QueenEntity extends RoyalBossEntity {
    public QueenEntity(EntityType<? extends QueenEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.queenHealth(), AntarchySettings.queenAttackDamage());
    }

    @Override
    protected String geoName() {
        return "queen";
    }

    @Override
    protected boolean isFlyingBoss() {
        return false;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.PURPLE;
    }

    @Override
    protected SoundEvent royalIdleSound() {
        return AntarchySoundEvents.QUEEN_IDLE.get();
    }

    @Override
    protected SoundEvent royalHurtSound() {
        return AntarchySoundEvents.QUEEN_HURT.get();
    }

    @Override
    protected SoundEvent royalDeathSound() {
        return AntarchySoundEvents.QUEEN_DEATH.get();
    }

    @Override
    protected SoundEvent royalBiteSound() {
        return AntarchySoundEvents.QUEEN_BITE.get();
    }
}
