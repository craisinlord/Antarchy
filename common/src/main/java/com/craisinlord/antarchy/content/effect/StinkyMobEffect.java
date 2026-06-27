package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.content.StinkyBehavior;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class StinkyMobEffect extends MobEffect {
    public StinkyMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x66773A);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        StinkyBehavior.tickStinkyTrail(entity);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
