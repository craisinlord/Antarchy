package com.craisinlord.antarchy.content.block.entity.undervault;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UnderVaultServerData {
    public static final String TAG_NAME = "server_data";
    private static final int MAX_REWARD_PLAYERS = 128;
    private final Set<UUID> rewardedPlayers = new ObjectLinkedOpenHashSet<>();
    private long stateUpdatingResumesAt;
    private final List<ItemStack> itemsToEject = new ObjectArrayList<>();
    private long lastInsertFailTimestamp;
    private int totalEjectionsNeeded;
    boolean isDirty;

    public static UnderVaultServerData load(CompoundTag tag) {
        UnderVaultServerData data = new UnderVaultServerData();
        data.rewardedPlayers.addAll(UnderVaultSharedData.loadUuidSet(tag.get("rewarded_players")));
        data.stateUpdatingResumesAt = tag.contains("state_updating_resumes_at", Tag.TAG_ANY_NUMERIC) ? tag.getLong("state_updating_resumes_at") : 0L;
        if (tag.contains("items_to_eject", Tag.TAG_LIST)) {
            ListTag list = tag.getList("items_to_eject", Tag.TAG_COMPOUND);
            List<ItemStack> items = new ObjectArrayList<>();
            boolean valid = true;
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.of(list.getCompound(i));
                if (stack.isEmpty()) {
                    valid = false;
                    break;
                }
                items.add(stack);
            }
            if (valid) {
                data.itemsToEject.addAll(items);
            }
        }
        data.totalEjectionsNeeded = tag.contains("total_ejections_needed", Tag.TAG_ANY_NUMERIC) ? tag.getInt("total_ejections_needed") : 0;
        return data;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!this.rewardedPlayers.isEmpty()) {
            tag.put("rewarded_players", UnderVaultSharedData.saveUuidSet(this.rewardedPlayers));
        }
        if (this.stateUpdatingResumesAt != 0L) {
            tag.putLong("state_updating_resumes_at", this.stateUpdatingResumesAt);
        }
        if (!this.itemsToEject.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack stack : this.itemsToEject) {
                list.add(stack.save(new CompoundTag()));
            }
            tag.put("items_to_eject", list);
        }
        if (this.totalEjectionsNeeded != 0) {
            tag.putInt("total_ejections_needed", this.totalEjectionsNeeded);
        }
        return tag;
    }

    void setLastInsertFailTimestamp(long timestamp) {
        this.lastInsertFailTimestamp = timestamp;
    }

    long getLastInsertFailTimestamp() {
        return this.lastInsertFailTimestamp;
    }

    Set<UUID> getRewardedPlayers() {
        return this.rewardedPlayers;
    }

    boolean hasRewardedPlayer(Player player) {
        return this.rewardedPlayers.contains(player.getUUID());
    }

    public void addToRewardedPlayers(Player player) {
        this.rewardedPlayers.add(player.getUUID());
        if (this.rewardedPlayers.size() > MAX_REWARD_PLAYERS) {
            Iterator<UUID> iterator = this.rewardedPlayers.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        this.markChanged();
    }

    long stateUpdatingResumesAt() {
        return this.stateUpdatingResumesAt;
    }

    void pauseStateUpdatingUntil(long time) {
        this.stateUpdatingResumesAt = time;
        this.markChanged();
    }

    List<ItemStack> getItemsToEject() {
        return this.itemsToEject;
    }

    void markEjectionFinished() {
        this.totalEjectionsNeeded = 0;
        this.markChanged();
    }

    void setItemsToEject(List<ItemStack> items) {
        this.itemsToEject.clear();
        this.itemsToEject.addAll(items);
        this.totalEjectionsNeeded = this.itemsToEject.size();
        this.markChanged();
    }

    ItemStack getNextItemToEject() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.itemsToEject.get(this.itemsToEject.size() - 1);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    ItemStack popNextItemToEject() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.markChanged();
        ItemStack stack = this.itemsToEject.remove(this.itemsToEject.size() - 1);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    void set(UnderVaultServerData other) {
        this.stateUpdatingResumesAt = other.stateUpdatingResumesAt();
        this.itemsToEject.clear();
        this.itemsToEject.addAll(other.itemsToEject);
        this.rewardedPlayers.clear();
        this.rewardedPlayers.addAll(other.rewardedPlayers);
    }

    private void markChanged() {
        this.isDirty = true;
    }

    public float ejectionProgress() {
        return this.totalEjectionsNeeded == 1 ? 1.0F
                : 1.0F - Mth.inverseLerp((float) this.getItemsToEject().size(), 1.0F, (float) this.totalEjectionsNeeded);
    }
}
