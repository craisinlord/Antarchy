package com.craisinlord.antarchy.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class ContractedMobEffect extends MobEffect {
    public static final double RATE = 2.0D;
    public static final double STRONG_RATE = 3.0D;

    public ContractedMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD5A63A);
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
