package com.craisinlord.antarchy.content.computer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.util.LegacySavedDataFiles;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class AntarchyGameSavedData extends SavedData {
    private static final String ID = "antarchy_computer_games";
    private static final Codec<AntarchyGameSavedData> CODEC = CompoundTag.CODEC.xmap(AntarchyGameSavedData::load, AntarchyGameSavedData::save);
    private static final SavedDataType<AntarchyGameSavedData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(Antarchy.MODID, ID), AntarchyGameSavedData::create, CODEC, null);
    private final Map<String, GameState> games = new HashMap<>();

    public static AntarchyGameSavedData create() { return new AntarchyGameSavedData(); }

    public static AntarchyGameSavedData load(CompoundTag tag) {
        AntarchyGameSavedData data = new AntarchyGameSavedData();
        for (net.minecraft.nbt.Tag raw : tag.getListOrEmpty("Games")) {
            if (!(raw instanceof CompoundTag item)) {
                continue;
            }
            GameState state = new GameState();
            state.basiliskScore = item.getIntOr("BasiliskScore", 0);
            state.basiliskHighScore = item.getIntOr("BasiliskHighScore", 0);
            state.antmanScore = item.getIntOr("AntmanScore", 0);
            state.antmanHighScore = item.getIntOr("AntmanHighScore", 0);
            data.games.put(item.getStringOr("Computer", ""), state);
        }
        return data;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
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

    private static AntarchyGameSavedData get(MinecraftServer server) {
        return LegacySavedDataFiles.computeIfAbsent(server, TYPE, ID);
    }

    public static GameState get(MinecraftServer server, String key) {
        return get(server).games.computeIfAbsent(key, ignored -> new GameState());
    }

    public static void changed(MinecraftServer server) {
        get(server).setDirty();
    }

    public static String key(net.minecraft.resources.Identifier dimension, net.minecraft.core.BlockPos pos) {
        return dimension + "|" + pos.asLong();
    }

    public static final class GameState {
        public int basiliskScore, basiliskHighScore, antmanScore, antmanHighScore;
    }
}
