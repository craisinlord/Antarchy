package com.craisinlord.antarchy.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class DilatedMobEffect extends MobEffect {
    public static final double RATE = 0.35D;
    public static final double STRONG_RATE = 0.2D;

    public DilatedMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x6C4AB6);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public static double rateForAmplifier(int amplifier) {
        return amplifier > 0 ? STRONG_RATE : RATE;
    }
}
