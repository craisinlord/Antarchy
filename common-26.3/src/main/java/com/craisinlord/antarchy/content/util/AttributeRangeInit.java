package com.craisinlord.antarchy.content.util;

import com.craisinlord.antarchy.mixins.AccessorRangedAttribute;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.Set;

public class AttributeRangeInit {

    private static final double MAX = 11_000_000D;

    private static final Set<Identifier> TARGETS = ImmutableSet.of(
            Identifier.fromNamespaceAndPath("minecraft", "generic.max_health"),
            Identifier.fromNamespaceAndPath("minecraft", "generic.armor"),
            Identifier.fromNamespaceAndPath("minecraft", "generic.armor_toughness"),
            Identifier.fromNamespaceAndPath("minecraft", "generic.attack_damage"),
            Identifier.fromNamespaceAndPath("minecraft", "generic.attack_knockback")
    );

    public static void apply() {
        for (Identifier id : TARGETS) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.getValue(id);
            if (attribute instanceof RangedAttribute ranged && ranged instanceof AccessorRangedAttribute accessor) {
                if (ranged.getMaxValue() < MAX) {
                    accessor.antarchyAttributefix$setMaxValue(MAX);
                }
            }
        }
    }
}
