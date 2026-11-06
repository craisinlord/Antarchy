package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class AntarchyRepairTags {
    public static final TagKey<Item> ULTIMATE = create("repairs_ultimate_gear");
    public static final TagKey<Item> BLOOD_CRYSTAL = create("repairs_blood_crystal_gear");
    public static final TagKey<Item> ATTITUDE_ADJUSTER = create("repairs_attitude_adjuster");
    public static final TagKey<Item> ROYAL_GUARDIAN = create("repairs_royal_guardian_gear");
    public static final TagKey<Item> ROYAL_ASSAILANT = create("repairs_royal_assailant_gear");
    public static final TagKey<Item> NIGHTMARE = create("repairs_nightmare_gear");
    public static final TagKey<Item> PRIMORDIAL = create("repairs_primordial_armor");
    public static final TagKey<Item> TIGERS_EYE = create("repairs_tigers_eye_armor");
    public static final TagKey<Item> SPRINGY_BOOTS = create("repairs_springy_boots");
    public static final TagKey<Item> GOLD = create("repairs_gold_gear");
    public static final TagKey<Item> ROYAL_GUARDIAN_SHIELD = create("repairs_royal_guardian_shield");

    private AntarchyRepairTags() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
