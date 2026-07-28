package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class GoopedMobEffect extends MobEffect {
    private static final double SLOWDOWN_PER_LEVEL = -0.3D;

    public GoopedMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x32B84E);
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gooped_attack_speed"),
                SLOWDOWN_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gooped_movement_speed"),
                SLOWDOWN_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
