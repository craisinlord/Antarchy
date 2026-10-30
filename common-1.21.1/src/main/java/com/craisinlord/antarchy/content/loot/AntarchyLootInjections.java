package com.craisinlord.antarchy.content.loot;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

public final class AntarchyLootInjections {
    private static final ResourceKey<LootTable> ANTROID_PHONE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "inject/antroid_phone"));
    private static final Set<ResourceLocation> PHONE_TARGETS = Set.of(
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

    public static boolean addsPhone(ResourceLocation table) {
        return PHONE_TARGETS.contains(table);
    }

    public static LootPool.Builder phonePool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(NestedLootTable.lootTableReference(ANTROID_PHONE));
    }

    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.withDefaultNamespace("chests/" + path);
    }
}
