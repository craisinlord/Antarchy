package com.craisinlord.antarchy.content.block.entity.undertrial;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record UndertrialEquipmentTable(ResourceLocation lootTable, Map<EquipmentSlot, Float> slotDropChances) {
    private static final LootContextParamSet EQUIPMENT = LootContextParamSet.builder()
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.THIS_ENTITY)
            .build();

    public static Optional<UndertrialEquipmentTable> load(Tag tag) {
        if (!(tag instanceof CompoundTag compound) || !compound.contains("loot_table", Tag.TAG_STRING)) {
            return Optional.empty();
        }
        ResourceLocation lootTable = ResourceLocation.tryParse(compound.getString("loot_table"));
        if (lootTable == null) {
            return Optional.empty();
        }
        Map<EquipmentSlot, Float> chances = new EnumMap<>(EquipmentSlot.class);
        Tag chancesTag = compound.get("slot_drop_chances");
        if (chancesTag instanceof NumericTag numeric) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                chances.put(slot, numeric.getAsFloat());
            }
        } else if (chancesTag instanceof CompoundTag chanceMap) {
            for (String key : chanceMap.getAllKeys()) {
                EquipmentSlot slot = slotByName(key);
                if (slot == null || !(chanceMap.get(key) instanceof NumericTag value)) {
                    return Optional.empty();
                }
                chances.put(slot, value.getAsFloat());
            }
        } else if (chancesTag != null) {
            return Optional.empty();
        }
        return Optional.of(new UndertrialEquipmentTable(lootTable, chances));
    }

    private static EquipmentSlot slotByName(String name) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getName().equals(name)) {
                return slot;
            }
        }
        return null;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("loot_table", this.lootTable.toString());
        if (!this.slotDropChances.isEmpty()) {
            Set<Float> distinct = new HashSet<>(this.slotDropChances.values());
            if (distinct.size() == 1 && this.slotDropChances.keySet().containsAll(List.of(EquipmentSlot.values()))) {
                tag.put("slot_drop_chances", FloatTag.valueOf(distinct.iterator().next()));
            } else {
                CompoundTag map = new CompoundTag();
                for (Map.Entry<EquipmentSlot, Float> entry : this.slotDropChances.entrySet()) {
                    map.putFloat(entry.getKey().getName(), entry.getValue());
                }
                tag.put("slot_drop_chances", map);
            }
        }
        return tag;
    }

    public void equip(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || this.lootTable.equals(BuiltInLootTables.EMPTY)) {
            return;
        }
        LootTable table = level.getServer().getLootData().getLootTable(this.lootTable);
        if (table == LootTable.EMPTY) {
            return;
        }
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .create(EQUIPMENT);
        List<EquipmentSlot> used = new ArrayList<>();
        for (ItemStack stack : table.getRandomItems(params, 0L)) {
            EquipmentSlot slot = resolveSlot(stack, used);
            if (slot != null) {
                ItemStack equipped = slot.isArmor() ? stack.split(1) : stack;
                mob.setItemSlot(slot, equipped);
                Float chance = this.slotDropChances.get(slot);
                if (chance != null) {
                    mob.setDropChance(slot, chance);
                }
                used.add(slot);
            }
        }
    }

    private static EquipmentSlot resolveSlot(ItemStack stack, List<EquipmentSlot> used) {
        if (stack.isEmpty()) {
            return null;
        }
        Equipable equipable = Equipable.get(stack);
        if (equipable != null) {
            EquipmentSlot slot = equipable.getEquipmentSlot();
            if (!used.contains(slot)) {
                return slot;
            }
        } else if (!used.contains(EquipmentSlot.MAINHAND)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}
