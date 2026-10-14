package com.craisinlord.antarchy.compat.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class EntityReach {
    public static final double DEFAULT_ENTITY_INTERACTION_RANGE = 3.0D;
    public static final double DEFAULT_BLOCK_INTERACTION_RANGE = 4.5D;
    private static final double CREATIVE_ENTITY_BONUS = 2.0D;
    private static final double CREATIVE_BLOCK_BONUS = 0.5D;

    private EntityReach() {
    }

    @Nullable
    public static Attribute attribute() {
        java.util.function.Supplier<Attribute> supplier = AntarchyObjects.ENTITY_REACH;
        return supplier == null ? null : supplier.get();
    }

    @Nullable
    public static AttributeInstance instance(LivingEntity entity) {
        Attribute attribute = attribute();
        return attribute == null ? null : entity.getAttribute(attribute);
    }

    public static double entityInteractionRange(LivingEntity entity) {
        AttributeInstance instance = instance(entity);
        double range = instance != null ? instance.getValue() : DEFAULT_ENTITY_INTERACTION_RANGE;
        if (entity instanceof Player player && player.isCreative()) {
            range += CREATIVE_ENTITY_BONUS;
        }
        return range;
    }

    public static double blockInteractionRange(Player player) {
        return DEFAULT_BLOCK_INTERACTION_RANGE + (player.isCreative() ? CREATIVE_BLOCK_BONUS : 0.0D);
    }

    public static UUID modifierId(ResourceLocation id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static AttributeModifier modifier(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(modifierId(id), id.toString(), amount, operation);
    }

    public static void addModifier(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, ResourceLocation id, double amount) {
        Attribute attribute = attribute();
        if (attribute != null) {
            builder.put(attribute, modifier(id, amount, AttributeModifier.Operation.ADDITION));
        }
    }

    public static Multimap<Attribute, AttributeModifier> withMainhandModifier(Multimap<Attribute, AttributeModifier> base, EquipmentSlot slot,
            ResourceLocation id, double amount) {
        Attribute attribute = attribute();
        if (slot != EquipmentSlot.MAINHAND || attribute == null) {
            return base;
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        builder.put(attribute, modifier(id, amount, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }
}
