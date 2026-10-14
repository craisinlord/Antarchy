package com.craisinlord.antarchy.content.block.entity.undervault;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UnderVaultSharedData {
    public static final String TAG_NAME = "shared_data";
    private ItemStack displayItem = ItemStack.EMPTY;
    private Set<UUID> connectedPlayers = new ObjectLinkedOpenHashSet<>();
    private double connectedParticlesRange = UnderVaultConfig.DEFAULT.deactivationRange();
    boolean isDirty;

    public static UnderVaultSharedData load(CompoundTag tag) {
        UnderVaultSharedData data = new UnderVaultSharedData();
        data.displayItem = tag.contains("display_item", Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound("display_item")) : ItemStack.EMPTY;
        data.connectedPlayers.addAll(loadUuidSet(tag.get("connected_players")));
        data.connectedParticlesRange = tag.contains("connected_particles_range", Tag.TAG_ANY_NUMERIC)
                ? tag.getDouble("connected_particles_range") : UnderVaultConfig.DEFAULT.deactivationRange();
        return data;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!this.displayItem.isEmpty()) {
            tag.put("display_item", this.displayItem.save(new CompoundTag()));
        }
        if (!this.connectedPlayers.isEmpty()) {
            tag.put("connected_players", saveUuidSet(this.connectedPlayers));
        }
        if (this.connectedParticlesRange != UnderVaultConfig.DEFAULT.deactivationRange()) {
            tag.putDouble("connected_particles_range", this.connectedParticlesRange);
        }
        return tag;
    }

    static Set<UUID> loadUuidSet(Tag tag) {
        Set<UUID> result = new ObjectLinkedOpenHashSet<>();
        if (!(tag instanceof ListTag list)) {
            return result;
        }
        for (Tag element : list) {
            if (!(element instanceof IntArrayTag array) || array.size() != 4) {
                return new ObjectLinkedOpenHashSet<>();
            }
            result.add(NbtUtils.loadUUID(array));
        }
        return result;
    }

    static ListTag saveUuidSet(Set<UUID> uuids) {
        ListTag list = new ListTag();
        for (UUID uuid : uuids) {
            list.add(NbtUtils.createUUID(uuid));
        }
        return list;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public boolean hasDisplayItem() {
        return !this.displayItem.isEmpty();
    }

    public void setDisplayItem(ItemStack stack) {
        if (!ItemStack.matches(this.displayItem, stack)) {
            this.displayItem = stack.copy();
            this.markDirty();
        }
    }

    boolean hasConnectedPlayers() {
        return !this.connectedPlayers.isEmpty();
    }

    Set<UUID> getConnectedPlayers() {
        return this.connectedPlayers;
    }

    double connectedParticlesRange() {
        return this.connectedParticlesRange;
    }

    void updateConnectedPlayersWithinRange(ServerLevel level, BlockPos pos, UnderVaultServerData serverData, UnderVaultConfig config, double range) {
        Set<UUID> players = new HashSet<>();
        for (UUID uuid : config.playerDetector().detect(level, config.entitySelector(), pos, range, false)) {
            if (!serverData.getRewardedPlayers().contains(uuid)) {
                players.add(uuid);
            }
        }
        if (!this.connectedPlayers.equals(players)) {
            this.connectedPlayers = players;
            this.markDirty();
        }
    }

    private void markDirty() {
        this.isDirty = true;
    }

    void set(UnderVaultSharedData other) {
        this.displayItem = other.displayItem;
        this.connectedPlayers = other.connectedPlayers;
        this.connectedParticlesRange = other.connectedParticlesRange;
    }
}
