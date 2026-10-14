package com.craisinlord.antarchy.content.block.entity.undertrial;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;

import java.util.Optional;

public record UndertrialSpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules,
                                  Optional<UndertrialEquipmentTable> equipment) {
    public static final String ENTITY_TAG = "entity";

    public UndertrialSpawnData() {
        this(new CompoundTag(), Optional.empty(), Optional.empty());
    }

    public CompoundTag getEntityToSpawn() {
        return this.entityToSpawn;
    }

    public Optional<SpawnData.CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public Optional<UndertrialEquipmentTable> getEquipment() {
        return this.equipment;
    }

    public static boolean isValidPosition(SpawnData.CustomSpawnRules rules, BlockPos pos, ServerLevel level) {
        return rules.blockLightLimit().isValueInRange(level.getBrightness(LightLayer.BLOCK, pos))
                && rules.skyLightLimit().isValueInRange(level.getBrightness(LightLayer.SKY, pos));
    }

    public static Optional<UndertrialSpawnData> load(Tag tag) {
        if (!(tag instanceof CompoundTag compound) || !compound.contains(ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        Optional<SpawnData.CustomSpawnRules> rules = Optional.empty();
        if (compound.contains("custom_spawn_rules")) {
            Optional<SpawnData.CustomSpawnRules> parsed = SpawnData.CustomSpawnRules.CODEC
                    .parse(NbtOps.INSTANCE, compound.get("custom_spawn_rules")).result();
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            rules = parsed;
        }
        Optional<UndertrialEquipmentTable> equipment = Optional.empty();
        if (compound.contains("equipment")) {
            equipment = UndertrialEquipmentTable.load(compound.get("equipment"));
            if (equipment.isEmpty()) {
                return Optional.empty();
            }
        }
        return Optional.of(new UndertrialSpawnData(compound.getCompound(ENTITY_TAG), rules, equipment));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(ENTITY_TAG, this.entityToSpawn);
        this.customSpawnRules.flatMap(rules -> SpawnData.CustomSpawnRules.CODEC.encodeStart(NbtOps.INSTANCE, rules).result())
                .ifPresent(encoded -> tag.put("custom_spawn_rules", encoded));
        this.equipment.ifPresent(table -> tag.put("equipment", table.save()));
        return tag;
    }

    public static Optional<SimpleWeightedRandomList<UndertrialSpawnData>> loadList(Tag tag) {
        if (!(tag instanceof ListTag list)) {
            return Optional.empty();
        }
        SimpleWeightedRandomList.Builder<UndertrialSpawnData> builder = SimpleWeightedRandomList.builder();
        for (Tag element : list) {
            if (!(element instanceof CompoundTag entry) || !entry.contains("weight", Tag.TAG_ANY_NUMERIC)) {
                return Optional.empty();
            }
            Optional<UndertrialSpawnData> data = load(entry.get("data"));
            int weight = entry.getInt("weight");
            if (data.isEmpty() || weight < 0) {
                return Optional.empty();
            }
            builder.add(data.get(), weight);
        }
        return Optional.of(builder.build());
    }

    public static ListTag saveList(SimpleWeightedRandomList<UndertrialSpawnData> list) {
        ListTag tag = new ListTag();
        for (WeightedEntry.Wrapper<UndertrialSpawnData> wrapper : list.unwrap()) {
            CompoundTag entry = new CompoundTag();
            entry.put("data", wrapper.getData().save());
            entry.putInt("weight", wrapper.getWeight().asInt());
            tag.add(entry);
        }
        return tag;
    }
}
