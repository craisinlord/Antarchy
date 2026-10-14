package com.craisinlord.antarchy.content.block.entity.undertrial;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;

import java.util.Optional;

public record UndertrialSpawnerConfig(
        int spawnRange,
        float totalMobs,
        float simultaneousMobs,
        float totalMobsAddedPerPlayer,
        float simultaneousMobsAddedPerPlayer,
        int ticksBetweenSpawn,
        SimpleWeightedRandomList<UndertrialSpawnData> spawnPotentialsDefinition,
        SimpleWeightedRandomList<ResourceLocation> lootTablesToEject,
        ResourceLocation itemsToDropWhenOminous
) {
    public static final ResourceLocation SPAWNER_TRIAL_CHAMBER_CONSUMABLES = new ResourceLocation("minecraft", "spawners/trial_chamber/consumables");
    public static final ResourceLocation SPAWNER_TRIAL_CHAMBER_KEY = new ResourceLocation("minecraft", "spawners/trial_chamber/key");
    public static final ResourceLocation SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS = new ResourceLocation("minecraft", "spawners/trial_chamber/items_to_drop_when_ominous");
    public static final UndertrialSpawnerConfig DEFAULT = new UndertrialSpawnerConfig(
            4,
            6.0F,
            2.0F,
            2.0F,
            1.0F,
            40,
            SimpleWeightedRandomList.empty(),
            SimpleWeightedRandomList.<ResourceLocation>builder().add(SPAWNER_TRIAL_CHAMBER_CONSUMABLES, 1).add(SPAWNER_TRIAL_CHAMBER_KEY, 1).build(),
            SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
    );

    public int calculateTargetTotalMobs(int additionalPlayers) {
        return (int) Math.floor(this.totalMobs + this.totalMobsAddedPerPlayer * additionalPlayers);
    }

    public int calculateTargetSimultaneousMobs(int additionalPlayers) {
        return (int) Math.floor(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * additionalPlayers);
    }

    public long ticksBetweenItemSpawners() {
        return 160L;
    }

    public static UndertrialSpawnerConfig load(CompoundTag tag) {
        int spawnRange = DEFAULT.spawnRange;
        if (tag.contains("spawn_range", Tag.TAG_ANY_NUMERIC)) {
            int value = tag.getInt("spawn_range");
            if (value >= 1 && value <= 128) {
                spawnRange = value;
            }
        }
        float totalMobs = nonNegativeFloat(tag, "total_mobs", DEFAULT.totalMobs);
        float simultaneousMobs = nonNegativeFloat(tag, "simultaneous_mobs", DEFAULT.simultaneousMobs);
        float totalMobsAddedPerPlayer = nonNegativeFloat(tag, "total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer);
        float simultaneousMobsAddedPerPlayer = nonNegativeFloat(tag, "simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer);
        int ticksBetweenSpawn = DEFAULT.ticksBetweenSpawn;
        if (tag.contains("ticks_between_spawn", Tag.TAG_ANY_NUMERIC) && tag.getInt("ticks_between_spawn") >= 0) {
            ticksBetweenSpawn = tag.getInt("ticks_between_spawn");
        }
        SimpleWeightedRandomList<UndertrialSpawnData> potentials = DEFAULT.spawnPotentialsDefinition;
        if (tag.contains("spawn_potentials")) {
            potentials = UndertrialSpawnData.loadList(tag.get("spawn_potentials")).orElse(DEFAULT.spawnPotentialsDefinition);
        }
        SimpleWeightedRandomList<ResourceLocation> lootTables = DEFAULT.lootTablesToEject;
        if (tag.contains("loot_tables_to_eject")) {
            lootTables = loadLootTables(tag.get("loot_tables_to_eject")).orElse(DEFAULT.lootTablesToEject);
        }
        ResourceLocation itemsToDrop = DEFAULT.itemsToDropWhenOminous;
        if (tag.contains("items_to_drop_when_ominous", Tag.TAG_STRING)) {
            ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("items_to_drop_when_ominous"));
            if (parsed != null) {
                itemsToDrop = parsed;
            }
        }
        return new UndertrialSpawnerConfig(spawnRange, totalMobs, simultaneousMobs, totalMobsAddedPerPlayer,
                simultaneousMobsAddedPerPlayer, ticksBetweenSpawn, potentials, lootTables, itemsToDrop);
    }

    private static float nonNegativeFloat(CompoundTag tag, String key, float fallback) {
        if (tag.contains(key, Tag.TAG_ANY_NUMERIC)) {
            float value = tag.getFloat(key);
            if (value >= 0.0F) {
                return value;
            }
        }
        return fallback;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (this.spawnRange != DEFAULT.spawnRange) {
            tag.putInt("spawn_range", this.spawnRange);
        }
        if (this.totalMobs != DEFAULT.totalMobs) {
            tag.putFloat("total_mobs", this.totalMobs);
        }
        if (this.simultaneousMobs != DEFAULT.simultaneousMobs) {
            tag.putFloat("simultaneous_mobs", this.simultaneousMobs);
        }
        if (this.totalMobsAddedPerPlayer != DEFAULT.totalMobsAddedPerPlayer) {
            tag.putFloat("total_mobs_added_per_player", this.totalMobsAddedPerPlayer);
        }
        if (this.simultaneousMobsAddedPerPlayer != DEFAULT.simultaneousMobsAddedPerPlayer) {
            tag.putFloat("simultaneous_mobs_added_per_player", this.simultaneousMobsAddedPerPlayer);
        }
        if (this.ticksBetweenSpawn != DEFAULT.ticksBetweenSpawn) {
            tag.putInt("ticks_between_spawn", this.ticksBetweenSpawn);
        }
        if (this.spawnPotentialsDefinition != DEFAULT.spawnPotentialsDefinition) {
            tag.put("spawn_potentials", UndertrialSpawnData.saveList(this.spawnPotentialsDefinition));
        }
        if (this.lootTablesToEject != DEFAULT.lootTablesToEject) {
            tag.put("loot_tables_to_eject", saveLootTables(this.lootTablesToEject));
        }
        if (!this.itemsToDropWhenOminous.equals(DEFAULT.itemsToDropWhenOminous)) {
            tag.putString("items_to_drop_when_ominous", this.itemsToDropWhenOminous.toString());
        }
        return tag;
    }

    public static Optional<SimpleWeightedRandomList<ResourceLocation>> loadLootTables(Tag tag) {
        if (!(tag instanceof ListTag list)) {
            return Optional.empty();
        }
        SimpleWeightedRandomList.Builder<ResourceLocation> builder = SimpleWeightedRandomList.builder();
        for (Tag element : list) {
            if (!(element instanceof CompoundTag entry) || !entry.contains("data", Tag.TAG_STRING)
                    || !entry.contains("weight", Tag.TAG_ANY_NUMERIC)) {
                return Optional.empty();
            }
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("data"));
            int weight = entry.getInt("weight");
            if (id == null || weight < 0) {
                return Optional.empty();
            }
            builder.add(id, weight);
        }
        return Optional.of(builder.build());
    }

    public static ListTag saveLootTables(SimpleWeightedRandomList<ResourceLocation> list) {
        ListTag tag = new ListTag();
        for (WeightedEntry.Wrapper<ResourceLocation> wrapper : list.unwrap()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("data", wrapper.getData().toString());
            entry.putInt("weight", wrapper.getWeight().asInt());
            tag.add(entry);
        }
        return tag;
    }
}
