package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class InvertedMobEffect extends MobEffect {
    private static final AntarchyGravityTransition EFFECT_TRANSITION = new AntarchyGravityTransition(12);

    public InvertedMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            if (AntarchyGravityApi.getGravityDirection(entity) != AntarchyGravityDirection.UP || !AntarchyGravityApi.isGravityForced(entity)) {
                AntarchyGravityApi.setForcedGravityDirection(entity, AntarchyGravityDirection.UP, EFFECT_TRANSITION);
            }
        } else {
            // Mob effects are synced by vanilla, so this fires on the client too.
            // If the gravity state hasn't been applied yet , apply it directly without position adjustment.
            if (!AntarchyGravityApi.isGravityInverted(entity)) {
                AntarchyGravityApi.applySyncedState(entity, AntarchyGravityDirection.UP, AntarchyGravityDirection.UP, true, 0, 0);
            }
        }
        return true;
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

}
