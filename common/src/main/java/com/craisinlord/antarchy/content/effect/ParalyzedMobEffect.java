package com.craisinlord.antarchy.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class ParalyzedMobEffect extends MobEffect {
    public ParalyzedMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x808080);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hasImpulse = true;
            entity.stopUsingItem();
            entity.setSprinting(false);
            entity.setYRot(entity.yRotO);
            entity.setXRot(entity.xRotO);
            entity.yHeadRot = entity.yHeadRotO;
            entity.yBodyRot = entity.yBodyRotO;

            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
