package com.craisinlord.antarchy.content.computer.blockle;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class BlockleAnswers {
    public static final int VERSION = 1;
    private static final int SALT = 0x4A17;
    private static final List<Answer> ANSWERS = List.of(
            new Answer("anvil", id("anvil")),
            new Answer("apple", id("apple")),
            new Answer("arrow", id("arrow")),
            new Answer("bread", id("bread")),
            new Answer("brick", id("brick")),
            new Answer("chain", id("chain")),
            new Answer("chest", id("chest")),
            new Answer("clock", id("clock")),
            new Answer("cocoa", id("cocoa")),
            new Answer("flint", id("flint")),
            new Answer("glass", id("glass")),
            new Answer("melon", id("melon")),
            new Answer("paper", id("paper")),
            new Answer("poppy", id("poppy")),
            new Answer("sculk", id("sculk")),
            new Answer("stone", id("stone")),
            new Answer("stick", id("stick")),
            new Answer("sugar", id("sugar")),
            new Answer("torch", id("torch")),
            new Answer("vault", id("vault")),
            new Answer("wheat", id("wheat")),
            new Answer("allay", id("allay_spawn_egg")),
            new Answer("blaze", id("blaze_spawn_egg")),
            new Answer("camel", id("camel_spawn_egg")),
            new Answer("drown", id("drowned_spawn_egg")),
            new Answer("ghast", id("ghast_spawn_egg")),
            new Answer("horse", id("horse_spawn_egg")),
            new Answer("llama", id("llama_spawn_egg")),
            new Answer("panda", id("panda_spawn_egg")),
            new Answer("sheep", id("sheep_spawn_egg")),
            new Answer("slime", id("slime_spawn_egg")),
            new Answer("squid", id("squid_spawn_egg")),
            new Answer("stray", id("stray_spawn_egg")),
            new Answer("witch", id("witch_spawn_egg")),
            new Answer("lever", id("lever")),
            new Answer("sword", id("iron_sword")),
            new Answer("cheep", antarchy("cheep_spawn_egg")),
            new Answer("jerry", antarchy("jerry_spawn_egg")),
            new Answer("lucid", antarchy("lucid_spawn_egg")),
            new Answer("queen", antarchy("queen_spawn_egg")),
            new Answer("ichor", antarchy("ichor")),
            new Answer("lotus", antarchy("lotus")),
            new Answer("mucus", antarchy("mucus")),
            new Answer("peach", antarchy("peach"))
    );

    private BlockleAnswers() {
    }

    public static Answer answerForDay(long day) {
        int index = (int) Math.floorMod(day + SALT, ANSWERS.size());
        return ANSWERS.get(index);
    }

    public static int size() {
        return ANSWERS.size();
    }

    public record Answer(String word, ResourceLocation itemId) {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static ResourceLocation antarchy(String path) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
    }
}
