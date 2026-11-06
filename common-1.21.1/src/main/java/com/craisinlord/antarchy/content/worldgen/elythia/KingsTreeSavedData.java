package com.craisinlord.antarchy.content.worldgen.elythia;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class KingsTreeSavedData extends SavedData {
    private static final String ID = "antarchy_kings_trees";
    private static final String TREES_KEY = "Trees";
    private static final String BASE_HEIGHTS_KEY = "BaseHeights";
    private static final String PENDING_KEY = "Pending";
    private static final SavedData.Factory<KingsTreeSavedData> FACTORY =
            new SavedData.Factory<>(KingsTreeSavedData::new, KingsTreeSavedData::load, null);

    private final Map<Long, Integer> treeBaseHeights = new HashMap<>();
    private final Set<Long> pendingTrees = new LinkedHashSet<>();
    private boolean restored;

    static KingsTreeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    static long treeKey(int centerX, int centerZ) {
        return ((long) centerX << 32) ^ (centerZ & 0xffffffffL);
    }

    static int centerX(long treeKey) {
        return (int) (treeKey >> 32);
    }

    static int centerZ(long treeKey) {
        return (int) treeKey;
    }

    private static KingsTreeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        KingsTreeSavedData data = new KingsTreeSavedData();
        long[] trees = tag.getLongArray(TREES_KEY);
        int[] baseHeights = tag.getIntArray(BASE_HEIGHTS_KEY);
        for (int index = 0; index < Math.min(trees.length, baseHeights.length); index++) {
            data.treeBaseHeights.put(trees[index], baseHeights[index]);
        }
        for (long tree : tag.getLongArray(PENDING_KEY)) {
            if (data.treeBaseHeights.containsKey(tree)) {
                data.pendingTrees.add(tree);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] trees = new long[this.treeBaseHeights.size()];
        int[] baseHeights = new int[trees.length];
        int index = 0;
        for (Map.Entry<Long, Integer> tree : this.treeBaseHeights.entrySet()) {
            trees[index] = tree.getKey();
            baseHeights[index++] = tree.getValue();
        }
        tag.putLongArray(TREES_KEY, trees);
        tag.putIntArray(BASE_HEIGHTS_KEY, baseHeights);
        tag.putLongArray(PENDING_KEY, this.pendingTrees.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }

    boolean markRestored() {
        boolean firstRestore = !this.restored;
        this.restored = true;
        return firstRestore;
    }

    Map<Long, Integer> treeBaseHeights() {
        return this.treeBaseHeights;
    }

    Set<Long> pendingTrees() {
        return this.pendingTrees;
    }

    boolean claim(long treeKey, int baseY) {
        if (this.treeBaseHeights.putIfAbsent(treeKey, baseY) != null) {
            return this.pendingTrees.contains(treeKey);
        }
        this.pendingTrees.add(treeKey);
        this.setDirty();
        return true;
    }

    void complete(long treeKey) {
        if (this.pendingTrees.remove(treeKey)) {
            this.setDirty();
        }
    }
}
