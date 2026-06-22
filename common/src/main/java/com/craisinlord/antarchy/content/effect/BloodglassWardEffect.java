package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class BloodglassWardEffect extends MobEffect {
    public static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bloodglass_ward_hearts");

    public BloodglassWardEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xAC3E5C);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        AttributeInstance instance = attributeMap.getInstance(AntarchyObjects.BLOODGLASS_MAX_HEARTS.get());
        if (instance == null) return;
        instance.addTransientModifier(
                new AttributeModifier(MODIFIER_ID, amplifier + 1, AttributeModifier.Operation.ADD_VALUE)
        );
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        AttributeInstance instance = attributeMap.getInstance(AntarchyObjects.BLOODGLASS_MAX_HEARTS.get());
        if (instance == null) return;
        instance.removeModifier(MODIFIER_ID);
    }
}
