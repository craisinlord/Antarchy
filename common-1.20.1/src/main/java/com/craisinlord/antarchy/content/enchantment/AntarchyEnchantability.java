package com.craisinlord.antarchy.content.enchantment;

import com.craisinlord.antarchy.Antarchy;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

public final class AntarchyEnchantability {
    private static final TagKey<Item> SWORDS = vanilla("swords");
    private static final TagKey<Item> AXES = vanilla("axes");
    private static final TagKey<Item> PICKAXES = vanilla("pickaxes");
    private static final TagKey<Item> SHOVELS = vanilla("shovels");
    private static final TagKey<Item> HOES = vanilla("hoes");
    private static final TagKey<Item> HEAD_ARMOR = vanilla("head_armor");
    private static final TagKey<Item> CHEST_ARMOR = vanilla("chest_armor");
    private static final TagKey<Item> LEG_ARMOR = vanilla("leg_armor");
    private static final TagKey<Item> FOOT_ARMOR = vanilla("foot_armor");
    private static final TagKey<Item> ENCHANTABLE_BOW = vanilla("enchantable/bow");
    private static final TagKey<Item> ENCHANTABLE_CROSSBOW = vanilla("enchantable/crossbow");
    private static final TagKey<Item> ENCHANTABLE_DURABILITY = vanilla("enchantable/durability");
    private static final TagKey<Item> ENCHANTABLE_EQUIPPABLE = vanilla("enchantable/equippable");
    private static final TagKey<Item> ENCHANTABLE_FIRE_ASPECT = vanilla("enchantable/fire_aspect");
    private static final TagKey<Item> ENCHANTABLE_MACE = vanilla("enchantable/mace");
    private static final TagKey<Item> ENCHANTABLE_TRIDENT = vanilla("enchantable/trident");
    private static final TagKey<Item> ENCHANTABLE_VANISHING = vanilla("enchantable/vanishing");
    private static final TagKey<Item> ENCHANTABLE_WEAPON = vanilla("enchantable/weapon");

    private static final Predicate<Holder<Item>> HEAD = item -> item.is(HEAD_ARMOR);
    private static final Predicate<Holder<Item>> CHEST = item -> item.is(CHEST_ARMOR);
    private static final Predicate<Holder<Item>> LEGS = item -> item.is(LEG_ARMOR);
    private static final Predicate<Holder<Item>> FEET = item -> item.is(FOOT_ARMOR);
    private static final Predicate<Holder<Item>> ARMOR = HEAD.or(CHEST).or(LEGS).or(FEET);
    private static final Predicate<Holder<Item>> SWORD = item -> item.is(SWORDS);
    private static final Predicate<Holder<Item>> SHARP_WEAPON = SWORD.or(item -> item.is(AXES));
    private static final Predicate<Holder<Item>> MACE = item -> item.is(ENCHANTABLE_MACE);
    private static final Predicate<Holder<Item>> WEAPON = SHARP_WEAPON.or(MACE).or(item -> item.is(ENCHANTABLE_WEAPON));
    private static final Predicate<Holder<Item>> FIRE_ASPECT = SWORD.or(MACE).or(item -> item.is(ENCHANTABLE_FIRE_ASPECT));
    private static final Predicate<Holder<Item>> MINING_LOOT = item -> item.is(AXES) || item.is(PICKAXES) || item.is(SHOVELS) || item.is(HOES);
    private static final Predicate<Holder<Item>> BOW = item -> item.is(ENCHANTABLE_BOW);
    private static final Predicate<Holder<Item>> CROSSBOW = item -> item.is(ENCHANTABLE_CROSSBOW);
    private static final Predicate<Holder<Item>> TRIDENT = item -> item.is(ENCHANTABLE_TRIDENT);
    private static final Predicate<Holder<Item>> MULTISHOT = CROSSBOW.or(item -> item.is(com.craisinlord.antarchy.content.AntarchyTags.Items.MULTISHOT_ENCHANTABLE));
    private static final Predicate<Holder<Item>> DURABILITY = ARMOR.or(SWORD).or(MINING_LOOT).or(BOW).or(CROSSBOW).or(TRIDENT).or(MACE)
            .or(item -> item.is(ENCHANTABLE_DURABILITY));
    private static final Predicate<Holder<Item>> EQUIPPABLE = ARMOR.or(item -> item.is(ENCHANTABLE_EQUIPPABLE));
    private static final Predicate<Holder<Item>> VANISHING = DURABILITY.or(item -> item.is(ENCHANTABLE_VANISHING));

    private static volatile Map<Enchantment, Rule> rules;

    private AntarchyEnchantability() {
    }

    @Nullable
    public static Boolean supports(@Nullable Enchantment enchantment, @Nullable Item item) {
        Rule rule = rule(enchantment, item);
        return rule == null ? null : rule.supported().test(item.builtInRegistryHolder());
    }

    @Nullable
    public static Boolean primary(@Nullable Enchantment enchantment, @Nullable Item item) {
        Rule rule = rule(enchantment, item);
        return rule == null ? null : rule.primary().test(item.builtInRegistryHolder());
    }

    @Nullable
    private static Rule rule(@Nullable Enchantment enchantment, @Nullable Item item) {
        if (enchantment == null || item == null) {
            return null;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (!Antarchy.MODID.equals(itemId.getNamespace())) {
            return null;
        }
        return rules().get(enchantment);
    }

    private static Map<Enchantment, Rule> rules() {
        Map<Enchantment, Rule> current = rules;
        if (current != null) {
            return current;
        }
        Map<Enchantment, Rule> built = new IdentityHashMap<>();
        put(built, Enchantments.AQUA_AFFINITY, HEAD);
        put(built, Enchantments.RESPIRATION, HEAD);
        put(built, Enchantments.BINDING_CURSE, EQUIPPABLE);
        put(built, Enchantments.ALL_DAMAGE_PROTECTION, ARMOR);
        put(built, Enchantments.FIRE_PROTECTION, ARMOR);
        put(built, Enchantments.BLAST_PROTECTION, ARMOR);
        put(built, Enchantments.PROJECTILE_PROTECTION, ARMOR);
        put(built, Enchantments.THORNS, ARMOR, CHEST);
        put(built, Enchantments.FALL_PROTECTION, FEET);
        put(built, Enchantments.DEPTH_STRIDER, FEET);
        put(built, Enchantments.FROST_WALKER, FEET);
        put(built, Enchantments.SOUL_SPEED, FEET);
        put(built, Enchantments.SWIFT_SNEAK, LEGS);
        put(built, Enchantments.SHARPNESS, SHARP_WEAPON, SWORD);
        put(built, Enchantments.SMITE, WEAPON, SWORD);
        put(built, Enchantments.BANE_OF_ARTHROPODS, WEAPON, SWORD);
        put(built, Enchantments.FIRE_ASPECT, FIRE_ASPECT, SWORD);
        put(built, Enchantments.KNOCKBACK, SWORD);
        put(built, Enchantments.MOB_LOOTING, SWORD);
        put(built, Enchantments.SWEEPING_EDGE, SWORD);
        put(built, Enchantments.BLOCK_EFFICIENCY, MINING_LOOT);
        put(built, Enchantments.SILK_TOUCH, MINING_LOOT);
        put(built, Enchantments.BLOCK_FORTUNE, MINING_LOOT);
        put(built, Enchantments.UNBREAKING, DURABILITY);
        put(built, Enchantments.MENDING, DURABILITY);
        put(built, Enchantments.VANISHING_CURSE, VANISHING);
        put(built, Enchantments.POWER_ARROWS, BOW);
        put(built, Enchantments.PUNCH_ARROWS, BOW);
        put(built, Enchantments.FLAMING_ARROWS, BOW);
        put(built, Enchantments.INFINITY_ARROWS, BOW);
        put(built, Enchantments.MULTISHOT, MULTISHOT);
        put(built, Enchantments.PIERCING, CROSSBOW);
        put(built, Enchantments.QUICK_CHARGE, CROSSBOW);
        put(built, Enchantments.LOYALTY, TRIDENT);
        put(built, Enchantments.IMPALING, TRIDENT);
        put(built, Enchantments.RIPTIDE, TRIDENT);
        put(built, Enchantments.CHANNELING, TRIDENT);
        Enchantment featherRising = AntarchyEnchantments.featherRising();
        Enchantment chronosphere = AntarchyEnchantments.chronosphere();
        put(built, featherRising, FEET);
        put(built, chronosphere, CHEST);
        if (featherRising != null && chronosphere != null) {
            rules = built;
        }
        return built;
    }

    private static void put(Map<Enchantment, Rule> map, @Nullable Enchantment enchantment, Predicate<Holder<Item>> supported) {
        put(map, enchantment, supported, supported);
    }

    private static void put(Map<Enchantment, Rule> map, @Nullable Enchantment enchantment, Predicate<Holder<Item>> supported,
            Predicate<Holder<Item>> primary) {
        if (enchantment != null) {
            map.put(enchantment, new Rule(supported, primary));
        }
    }

    private static TagKey<Item> vanilla(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", path));
    }

    private record Rule(Predicate<Holder<Item>> supported, Predicate<Holder<Item>> primary) {
    }
}
