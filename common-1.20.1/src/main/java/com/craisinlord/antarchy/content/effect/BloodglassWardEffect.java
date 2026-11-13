package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.UUID;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class BloodglassWardEffect extends MobEffect {
    public static final UUID MODIFIER_ID =
            UUID.fromString("8e1a2c40-3f6b-4d1e-9a5c-2f7b6e0d1a4f");

    public BloodglassWardEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xAC3E5C);
    }

    @Override
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance instance = attributeMap.getInstance(AntarchyObjects.BLOODGLASS_MAX_HEARTS.get());
        if (instance == null) {
            return;
        }
        instance.removeModifier(MODIFIER_ID);
        instance.addTransientModifier(
                new AttributeModifier(MODIFIER_ID, "bloodglass_ward_hearts", amplifier + 1, AttributeModifier.Operation.ADDITION)
        );
    }

    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance instance = attributeMap.getInstance(AntarchyObjects.BLOODGLASS_MAX_HEARTS.get());
        if (instance == null) {
            return;
        }
        instance.removeModifier(MODIFIER_ID);
    }
}
