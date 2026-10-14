package com.craisinlord.antarchy.content.block.entity.undervault;

import com.craisinlord.antarchy.content.block.entity.undertrial.UndertrialPlayerDetector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record UnderVaultConfig(
        ResourceLocation lootTable,
        double activationRange,
        double deactivationRange,
        ItemStack keyItem,
        Optional<ResourceLocation> overrideLootTableToDisplay,
        UndertrialPlayerDetector playerDetector,
        UndertrialPlayerDetector.EntitySelector entitySelector
) {
    public static final String TAG_NAME = "config";
    public static final ResourceLocation TRIAL_CHAMBERS_REWARD = new ResourceLocation("minecraft", "chests/trial_chambers/reward");
    public static final UnderVaultConfig DEFAULT = new UnderVaultConfig(TRIAL_CHAMBERS_REWARD, 4.0D, 4.5D, ItemStack.EMPTY,
            Optional.empty(), UndertrialPlayerDetector.INCLUDING_CREATIVE_PLAYERS, UndertrialPlayerDetector.EntitySelector.SELECT_FROM_LEVEL);

    public UnderVaultConfig(ResourceLocation lootTable, double activationRange, double deactivationRange, ItemStack keyItem,
                            Optional<ResourceLocation> overrideLootTableToDisplay) {
        this(lootTable, activationRange, deactivationRange, keyItem, overrideLootTableToDisplay,
                DEFAULT.playerDetector(), DEFAULT.entitySelector());
    }

    public static Optional<UnderVaultConfig> load(CompoundTag tag) {
        ResourceLocation lootTable = DEFAULT.lootTable;
        if (tag.contains("loot_table", Tag.TAG_STRING)) {
            ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("loot_table"));
            if (parsed != null) {
                lootTable = parsed;
            }
        }
        double activation = tag.contains("activation_range", Tag.TAG_ANY_NUMERIC) ? tag.getDouble("activation_range") : DEFAULT.activationRange;
        double deactivation = tag.contains("deactivation_range", Tag.TAG_ANY_NUMERIC) ? tag.getDouble("deactivation_range") : DEFAULT.deactivationRange;
        ItemStack key = tag.contains("key_item", Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound("key_item")) : ItemStack.EMPTY;
        Optional<ResourceLocation> override = tag.contains("override_loot_table_to_display", Tag.TAG_STRING)
                ? Optional.ofNullable(ResourceLocation.tryParse(tag.getString("override_loot_table_to_display"))) : Optional.empty();
        if (activation > deactivation) {
            return Optional.empty();
        }
        return Optional.of(new UnderVaultConfig(lootTable, activation, deactivation, key, override));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!this.lootTable.equals(DEFAULT.lootTable)) {
            tag.putString("loot_table", this.lootTable.toString());
        }
        if (this.activationRange != DEFAULT.activationRange) {
            tag.putDouble("activation_range", this.activationRange);
        }
        if (this.deactivationRange != DEFAULT.deactivationRange) {
            tag.putDouble("deactivation_range", this.deactivationRange);
        }
        if (!this.keyItem.isEmpty()) {
            tag.put("key_item", this.keyItem.save(new CompoundTag()));
        }
        this.overrideLootTableToDisplay.ifPresent(id -> tag.putString("override_loot_table_to_display", id.toString()));
        return tag;
    }
}
