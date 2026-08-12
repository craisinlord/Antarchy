package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.content.client.particle.GlimmerParticles;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class GlimmeringMobEffect extends MobEffect {
    public GlimmeringMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x66CCFF);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            boolean running = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            GlimmerParticles.tickAmbient(entity, running);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
