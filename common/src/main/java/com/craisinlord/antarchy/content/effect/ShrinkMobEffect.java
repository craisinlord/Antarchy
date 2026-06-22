package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ShrinkMobEffect extends MobEffect {
    public static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "shrinking_effect_scale");

    public ShrinkMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFFFF00);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        AttributeInstance instance = attributeMap.getInstance(Attributes.SCALE);
        if (instance == null) return;
        double amount = -(AntarchySettings.shrinkingPotionDelta() * (amplifier + 1));
        instance.addTransientModifier(
                new AttributeModifier(MODIFIER_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        );
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        AttributeInstance instance = attributeMap.getInstance(Attributes.SCALE);
        if (instance == null) return;
        instance.removeModifier(MODIFIER_ID);
    }
}
