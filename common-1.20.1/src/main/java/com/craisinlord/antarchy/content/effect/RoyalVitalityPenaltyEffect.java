package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;

public final class RoyalVitalityPenaltyEffect extends MobEffect {
    public static final ResourceLocation MODIFIER_ID =
            new ResourceLocation(Antarchy.MODID, "royal_vitality_penalty");
    private static final UUID MODIFIER_UUID = UUID.nameUUIDFromBytes(MODIFIER_ID.toString().getBytes(StandardCharsets.UTF_8));

    public RoyalVitalityPenaltyEffect() {
        super(MobEffectCategory.HARMFUL, 0xD8A52B);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance maxHealth = attributeMap.getInstance(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.removeModifier(MODIFIER_UUID);
        maxHealth.addTransientModifier(new AttributeModifier(
                MODIFIER_UUID, MODIFIER_ID.toString(), -0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance maxHealth = attributeMap.getInstance(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.removeModifier(MODIFIER_UUID);
    }
}
