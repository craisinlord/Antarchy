package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

final class KingsTreeSavedData extends SavedData {
    private static final String ID = "antarchy_kings_trees";
    private static final String TREES_KEY = "Trees";
    private static final String BASE_HEIGHTS_KEY = "BaseHeights";
    private static final String PENDING_KEY = "Pending";
    private static final Codec<KingsTreeSavedData> CODEC =
            CompoundTag.CODEC.xmap(KingsTreeSavedData::load, data -> data.save(new CompoundTag()));
    private static final SavedDataType<KingsTreeSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Antarchy.MODID, ID), KingsTreeSavedData::new, CODEC, null);

    private final Map<Long, Integer> treeBaseHeights = new HashMap<>();
    private final Set<Long> pendingTrees = new LinkedHashSet<>();
    private boolean restored;

    static KingsTreeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
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

    private static KingsTreeSavedData load(CompoundTag tag) {
        KingsTreeSavedData data = new KingsTreeSavedData();
        long[] trees = tag.getLongArray(TREES_KEY).orElse(new long[0]);
        int[] baseHeights = tag.getIntArray(BASE_HEIGHTS_KEY).orElse(new int[0]);
        for (int index = 0; index < Math.min(trees.length, baseHeights.length); index++) {
            data.treeBaseHeights.put(trees[index], baseHeights[index]);
        }
        for (long tree : tag.getLongArray(PENDING_KEY).orElse(new long[0])) {
            if (data.treeBaseHeights.containsKey(tree)) {
                data.pendingTrees.add(tree);
            }
        }
        return data;
    }

    private CompoundTag save(CompoundTag tag) {
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
