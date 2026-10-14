package com.craisinlord.antarchy.compat.item;

import com.craisinlord.antarchy.compat.entity.EntityReach;
import com.google.common.collect.ImmutableMultimap;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public final class WeaponAttributes {
    private WeaponAttributes() {
    }

    public static ImmutableMultimap.Builder<Attribute, AttributeModifier> base(Tier tier, float attackDamage, float attackSpeed) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(Ids.ATTACK_DAMAGE, "Weapon modifier",
                attackDamage + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(Ids.ATTACK_SPEED, "Weapon modifier",
                attackSpeed, AttributeModifier.Operation.ADDITION));
        return builder;
    }

    public static void add(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, Attribute attribute, ResourceLocation id, double amount) {
        builder.put(attribute, EntityReach.modifier(id, amount, AttributeModifier.Operation.ADDITION));
    }

    public static UUID baseAttackDamageId() {
        return Ids.ATTACK_DAMAGE;
    }

    public static UUID baseAttackSpeedId() {
        return Ids.ATTACK_SPEED;
    }

    private static final class Ids extends Item {
        static final UUID ATTACK_DAMAGE = BASE_ATTACK_DAMAGE_UUID;
        static final UUID ATTACK_SPEED = BASE_ATTACK_SPEED_UUID;

        private Ids() {
            super(new Item.Properties());
        }
    }
}
