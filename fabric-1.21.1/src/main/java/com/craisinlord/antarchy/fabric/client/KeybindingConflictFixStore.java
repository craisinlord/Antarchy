package com.craisinlord.antarchy.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;

public final class KeybindingConflictFixStore {
    private static final String ANTARCHY_KEY_PREFIX = "key.antarchy.";
    private static final Map<InputConstants.Key, List<KeyMapping>> MAPPINGS = new HashMap<>();

    private KeybindingConflictFixStore() {
    }

    public static void add(InputConstants.Key key, KeyMapping mapping) {
        if (!mapping.getName().startsWith(ANTARCHY_KEY_PREFIX)) {
            return;
        }
        List<KeyMapping> mappings = MAPPINGS.computeIfAbsent(key, ignored -> new ArrayList<>());
        if (!mappings.contains(mapping)) {
            mappings.add(mapping);
        }
    }

    public static void clear() {
        MAPPINGS.clear();
    }

    public static List<KeyMapping> others(InputConstants.Key key, KeyMapping active) {
        List<KeyMapping> mappings = MAPPINGS.get(key);
        if (mappings == null || mappings.size() < 2) {
            return List.of();
        }
        List<KeyMapping> others = new ArrayList<>(mappings.size() - 1);
        for (KeyMapping mapping : mappings) {
            if (mapping != active) {
                others.add(mapping);
            }
        }
        return others;
    }
}
