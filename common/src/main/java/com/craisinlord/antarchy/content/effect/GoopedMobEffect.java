package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class GoopedMobEffect extends MobEffect {
    public GoopedMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x32B84E);
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gooped_attack_speed"),
                -0.3D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gooped_movement_speed"),
                -0.3D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
