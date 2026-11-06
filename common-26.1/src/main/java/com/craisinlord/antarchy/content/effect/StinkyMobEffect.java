package com.craisinlord.antarchy.content.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class StinkyMobEffect extends MobEffect {
    public StinkyMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x66773A);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        StinkyBehavior.tickStinkyTrail(entity);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
