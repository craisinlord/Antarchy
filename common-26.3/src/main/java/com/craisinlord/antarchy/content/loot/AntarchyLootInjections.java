package com.craisinlord.antarchy.content.loot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import java.util.Set;

public final class AntarchyLootInjections {
    private static final Identifier ANTROID_PHONE = Identifier.fromNamespaceAndPath("antos", "antroid_phone");
    private static final Set<Identifier> PHONE_TARGETS = Set.of(
            vanilla("village/village_armorer"),
            vanilla("village/village_butcher"),
            vanilla("village/village_cartographer"),
            vanilla("village/village_desert_house"),
            vanilla("village/village_fisher"),
            vanilla("village/village_fletcher"),
            vanilla("village/village_mason"),
            vanilla("village/village_plains_house"),
            vanilla("village/village_savanna_house"),
            vanilla("village/village_shepherd"),
            vanilla("village/village_snowy_house"),
            vanilla("village/village_taiga_house"),
            vanilla("village/village_tannery"),
            vanilla("village/village_temple"),
            vanilla("village/village_toolsmith"),
            vanilla("village/village_weaponsmith"),
            vanilla("shipwreck_supply"),
            vanilla("buried_treasure"),
            vanilla("simple_dungeon"),
            vanilla("abandoned_mineshaft"),
            vanilla("ancient_city")
    );

    private AntarchyLootInjections() {
    }

    public static boolean addsPhone(Identifier table) {
        return PHONE_TARGETS.contains(table);
    }

    public static LootPool.Builder phonePool() {
        return LootPool.lootPool()
                .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.getValue(ANTROID_PHONE)).setWeight(1))
                .add(EmptyLootItem.emptyItem().setWeight(9));
    }

    private static Identifier vanilla(String path) {
        return Identifier.withDefaultNamespace("chests/" + path);
    }
}
