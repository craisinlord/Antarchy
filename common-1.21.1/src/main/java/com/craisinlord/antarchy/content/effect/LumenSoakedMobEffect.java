package com.craisinlord.antarchy.content.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class LumenSoakedMobEffect extends MobEffect {
    public LumenSoakedMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0x00BEE3);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return entity.level().isClientSide() || !entity.isInWaterRainOrBubble();
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance effect) {
        if (LumenSoakedHooks.dripParticleType() instanceof ParticleOptions drip) {
            return drip;
        }
        return ParticleTypes.DRIPPING_WATER;
    }
}
