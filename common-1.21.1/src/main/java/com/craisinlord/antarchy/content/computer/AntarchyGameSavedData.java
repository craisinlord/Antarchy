package com.craisinlord.antarchy.content.computer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent Antarchy game progress keyed by AntOS computer location. */
public final class AntarchyGameSavedData extends SavedData {
    private static final String ID = "antarchy_computer_games";
    private final Map<String, GameState> games = new HashMap<>();

    public static AntarchyGameSavedData create() { return new AntarchyGameSavedData(); }

    public static AntarchyGameSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        AntarchyGameSavedData data = new AntarchyGameSavedData();
        for (net.minecraft.nbt.Tag raw : tag.getList("Games", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag item = (CompoundTag) raw;
            GameState state = new GameState();
            state.basiliskScore = item.getInt("BasiliskScore");
            state.basiliskHighScore = item.getInt("BasiliskHighScore");
            state.antmanScore = item.getInt("AntmanScore");
            state.antmanHighScore = item.getInt("AntmanHighScore");
            state.blockleDay = item.getLong("BlockleDay");
            for (net.minecraft.nbt.Tag guess : item.getList("BlockleGuesses", net.minecraft.nbt.Tag.TAG_STRING)) state.blockleGuesses.add(guess.getAsString());
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
            item.putLong("BlockleDay", state.blockleDay);
            ListTag guesses = new ListTag();
            state.blockleGuesses.forEach(guess -> guesses.add(net.minecraft.nbt.StringTag.valueOf(guess)));
            item.put("BlockleGuesses", guesses);
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

    public static String key(net.minecraft.resources.ResourceLocation dimension, net.minecraft.core.BlockPos pos) {
        return dimension + "|" + pos.asLong();
    }

    public static final class GameState {
        public int basiliskScore, basiliskHighScore, antmanScore, antmanHighScore;
        public long blockleDay = Long.MIN_VALUE;
        public final List<String> blockleGuesses = new ArrayList<>();
    }
}
