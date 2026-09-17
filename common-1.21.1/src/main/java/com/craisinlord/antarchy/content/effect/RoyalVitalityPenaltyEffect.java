package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class RoyalVitalityPenaltyEffect extends MobEffect {
    public static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "royal_vitality_penalty");

    public RoyalVitalityPenaltyEffect() {
        super(MobEffectCategory.HARMFUL, 0xD8A52B);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        AttributeInstance maxHealth = attributeMap.getInstance(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.addTransientModifier(new AttributeModifier(
                MODIFIER_ID, -0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        AttributeInstance maxHealth = attributeMap.getInstance(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.removeModifier(MODIFIER_ID);
    }
}
