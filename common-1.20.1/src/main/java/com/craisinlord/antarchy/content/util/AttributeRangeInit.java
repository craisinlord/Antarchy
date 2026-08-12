package com.craisinlord.antarchy.content.util;

import com.craisinlord.antarchy.mixins.AccessorRangedAttribute;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.Set;

public class AttributeRangeInit {

    private static final double MAX = 11_000_000D;

    private static final Set<ResourceLocation> TARGETS = ImmutableSet.of(
            new ResourceLocation("minecraft", "generic.max_health"),
            new ResourceLocation("minecraft", "generic.armor"),
            new ResourceLocation("minecraft", "generic.armor_toughness"),
            new ResourceLocation("minecraft", "generic.attack_damage"),
            new ResourceLocation("minecraft", "generic.attack_knockback")
    );

    public static void apply() {
        for (ResourceLocation id : TARGETS) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);
            if (attribute instanceof RangedAttribute ranged && ranged instanceof AccessorRangedAttribute accessor) {
                if (ranged.getMaxValue() < MAX) {
                    accessor.antarchyAttributefix$setMaxValue(MAX);
                }
            }
        }
    }
}
