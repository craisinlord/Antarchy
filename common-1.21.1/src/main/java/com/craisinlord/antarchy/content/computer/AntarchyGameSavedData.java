package com.craisinlord.antarchy.content.computer;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class AntarchyGameSavedData extends SavedData {
    private static final String ID = "antarchy_computer_games";
    private final Map<String, GameState> games = new HashMap<>();

    public static AntarchyGameSavedData create() {
        return new AntarchyGameSavedData();
    }

    public static AntarchyGameSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        AntarchyGameSavedData data = new AntarchyGameSavedData();
        for (Tag raw : tag.getList("Games", Tag.TAG_COMPOUND)) {
            CompoundTag item = (CompoundTag) raw;
            GameState state = new GameState();
            state.basiliskScore = item.getInt("BasiliskScore");
            state.basiliskHighScore = item.getInt("BasiliskHighScore");
            state.antmanScore = item.getInt("AntmanScore");
            state.antmanHighScore = item.getInt("AntmanHighScore");
            data.games.put(item.getString("Computer"), state);
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        games.forEach((key, state) -> {
            CompoundTag item = new CompoundTag();
            item.putString("Computer", key);
            item.putInt("BasiliskScore", state.basiliskScore);
            item.putInt("BasiliskHighScore", state.basiliskHighScore);
            item.putInt("AntmanScore", state.antmanScore);
            item.putInt("AntmanHighScore", state.antmanHighScore);
            list.add(item);
        });
        tag.put("Games", list);
        return tag;
    }

    public static GameState get(MinecraftServer server, String key) {
        AntarchyGameSavedData data = server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AntarchyGameSavedData::create, AntarchyGameSavedData::load, null), ID);
        return data.games.computeIfAbsent(key, ignored -> new GameState());
    }

    public static void changed(MinecraftServer server) {
        server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AntarchyGameSavedData::create, AntarchyGameSavedData::load, null), ID).setDirty();
    }

    public static String key(ResourceLocation dimension, BlockPos pos) {
        return dimension + "|" + pos.asLong();
    }

    public static final class GameState {
        public int basiliskScore, basiliskHighScore, antmanScore, antmanHighScore;
    }
}
