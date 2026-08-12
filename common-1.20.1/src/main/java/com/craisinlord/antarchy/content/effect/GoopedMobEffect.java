package com.craisinlord.antarchy.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class GoopedMobEffect extends MobEffect {
    private static final double SLOWDOWN_PER_LEVEL = -0.3D;

    public static final java.util.UUID ATTACK_SPEED_MODIFIER_ID =
            java.util.UUID.fromString("6f3a2b9e-7f0d-4f7e-9b7a-3b3e2f6c8a51");
    public static final java.util.UUID MOVEMENT_SPEED_MODIFIER_ID =
            java.util.UUID.fromString("5a1d4c8f-9e2b-4d6a-8c7f-2e1b9a4d3f60");

    public GoopedMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x32B84E);
    }

    @Override
    public void addAttributeModifiers(net.minecraft.world.entity.LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance attackSpeed = attributeMap.getInstance(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.addTransientModifier(
                    new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, "gooped_attack_speed", SLOWDOWN_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
        AttributeInstance movementSpeed = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.addTransientModifier(
                    new AttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, "gooped_movement_speed", SLOWDOWN_PER_LEVEL, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
    }

    @Override
    public void removeAttributeModifiers(net.minecraft.world.entity.LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance attackSpeed = attributeMap.getInstance(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
        AttributeInstance movementSpeed = attributeMap.getInstance(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
        }
    }
}
