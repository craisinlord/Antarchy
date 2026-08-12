package com.craisinlord.antarchy.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class StinkyMobEffect extends MobEffect {
    public StinkyMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x66773A);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        StinkyBehavior.tickStinkyTrail(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
