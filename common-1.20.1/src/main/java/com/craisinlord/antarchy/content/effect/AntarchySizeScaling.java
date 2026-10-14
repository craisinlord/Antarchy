package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.function.Supplier;

public final class AntarchySizeScaling {
    private AntarchySizeScaling() {
    }

    public static void register(Supplier<? extends MobEffect> shrinkingEffect, Supplier<? extends MobEffect> growthEffect) {
        ScaleModifier shrinking = ScaleRegistries.register(ScaleRegistries.SCALE_MODIFIERS, new ResourceLocation("antarchy", "shrinking"), potionModifier(shrinkingEffect, -1, AntarchySettings::shrinkingPotionDelta));
        ScaleModifier growth = ScaleRegistries.register(ScaleRegistries.SCALE_MODIFIERS, new ResourceLocation("antarchy", "growth"), potionModifier(growthEffect, 1, AntarchySettings::growthPotionDelta));
        ScaleTypes.BASE.getDefaultBaseValueModifiers().add(shrinking);
        ScaleTypes.BASE.getDefaultBaseValueModifiers().add(growth);
    }

    private static ScaleModifier potionModifier(Supplier<? extends MobEffect> effectSupplier, int sign, java.util.function.DoubleSupplier deltaPerLevel) {
        return new ScaleModifier() {
            @Override
            public float modifyScale(ScaleData data, float currentValue, float baseValue) {
                Entity entity = data.getEntity();
                if (!(entity instanceof LivingEntity living)) {
                    return currentValue;
                }
                MobEffectInstance instance;
                try {
                    instance = living.getEffect(effectSupplier.get());
                } catch (NullPointerException notYetConstructed) {
                    return currentValue;
                }
                if (instance == null) {
                    return currentValue;
                }
                double offset = sign * deltaPerLevel.getAsDouble() * (instance.getAmplifier() + 1);
                return (float) (currentValue + baseValue * offset);
            }
        };
    }
}
