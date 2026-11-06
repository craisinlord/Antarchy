package com.craisinlord.antarchy.content.item;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class AntarchyItemProperties {
    private AntarchyItemProperties() {
    }

    public static Item.Properties configEnchantable(Item.Properties properties, IntSupplier value) {
        return properties.delayedComponent(DataComponents.ENCHANTABLE, context -> {
            int resolved = value.getAsInt();
            return resolved > 0 ? new Enchantable(resolved) : null;
        });
    }

    public static Item.Properties configAttributes(Item.Properties properties, Supplier<ItemAttributeModifiers> attributes) {
        return properties.delayedComponent(DataComponents.ATTRIBUTE_MODIFIERS, context -> attributes.get());
    }

    public static Item.Properties repairableBy(Item.Properties properties, Identifier itemId) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, itemId);
        return properties.delayedComponent(DataComponents.REPAIRABLE, context -> context.lookupOrThrow(Registries.ITEM)
                .get(key)
                .map(holder -> new Repairable(HolderSet.direct(holder)))
                .orElse(null));
    }

    public static Item.Properties repairableBy(Item.Properties properties, Supplier<Ingredient> ingredient) {
        return properties.delayedComponent(DataComponents.REPAIRABLE, context -> {
            List<Holder<Item>> items = ingredient.get().items().toList();
            return items.isEmpty() ? null : new Repairable(HolderSet.direct(items));
        });
    }

    public static ItemAttributeModifiers toolAttributes(ToolMaterial material, double attackDamage, double attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage + material.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    public static Tool swordTool() {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return new Tool(
                List.of(
                        Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
                ),
                1.0F,
                2,
                false
        );
    }

    public static Tool diggerTool(ToolMaterial material, TagKey<Block> minesEfficiently) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return new Tool(
                List.of(
                        Tool.Rule.deniesDrops(blocks.getOrThrow(material.incorrectBlocksForDrops())),
                        Tool.Rule.minesAndDrops(blocks.getOrThrow(minesEfficiently), material.speed())
                ),
                1.0F,
                1,
                true
        );
    }

    public static Weapon swordWeapon() {
        return new Weapon(1);
    }
}
