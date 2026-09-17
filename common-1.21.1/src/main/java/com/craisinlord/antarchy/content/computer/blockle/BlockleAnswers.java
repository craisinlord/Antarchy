package com.craisinlord.antarchy.content.computer.blockle;

import com.craisinlord.antarchy.Antarchy;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class BlockleAnswers extends SimplePreparableReloadListener<List<String>> {
    public static final int VERSION = 1;
    private static final int SALT = 0x4A17;
    private static final ResourceLocation ANSWERS_FILE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blockle/answers.json");
    private static final BlockleAnswers INSTANCE = new BlockleAnswers();
    private static final Map<String, ResourceLocation> ANSWER_ITEMS = Map.ofEntries(
            Map.entry("anvil", vanilla("anvil")), Map.entry("apple", vanilla("apple")),
            Map.entry("arrow", vanilla("arrow")), Map.entry("bread", vanilla("bread")),
            Map.entry("brick", vanilla("brick")), Map.entry("chain", vanilla("chain")),
            Map.entry("chest", vanilla("chest")), Map.entry("clock", vanilla("clock")),
            Map.entry("cocoa", vanilla("cocoa")), Map.entry("flint", vanilla("flint")),
            Map.entry("glass", vanilla("glass")), Map.entry("melon", vanilla("melon")),
            Map.entry("paper", vanilla("paper")), Map.entry("poppy", vanilla("poppy")),
            Map.entry("sculk", vanilla("sculk")), Map.entry("stone", vanilla("stone")),
            Map.entry("stick", vanilla("stick")), Map.entry("sugar", vanilla("sugar")),
            Map.entry("torch", vanilla("torch")), Map.entry("vault", vanilla("vault")),
            Map.entry("wheat", vanilla("wheat")), Map.entry("allay", vanilla("allay_spawn_egg")),
            Map.entry("blaze", vanilla("blaze_spawn_egg")), Map.entry("camel", vanilla("camel_spawn_egg")),
            Map.entry("drown", vanilla("drowned_spawn_egg")), Map.entry("ghast", vanilla("ghast_spawn_egg")),
            Map.entry("horse", vanilla("horse_spawn_egg")), Map.entry("llama", vanilla("llama_spawn_egg")),
            Map.entry("panda", vanilla("panda_spawn_egg")), Map.entry("sheep", vanilla("sheep_spawn_egg")),
            Map.entry("slime", vanilla("slime_spawn_egg")), Map.entry("squid", vanilla("squid_spawn_egg")),
            Map.entry("stray", vanilla("stray_spawn_egg")), Map.entry("witch", vanilla("witch_spawn_egg")),
            Map.entry("lever", vanilla("lever")), Map.entry("sword", vanilla("iron_sword")),
            Map.entry("cheep", antarchy("cheep_spawn_egg")), Map.entry("jerry", antarchy("jerry_spawn_egg")),
            Map.entry("lucid", antarchy("lucid_spawn_egg")), Map.entry("queen", antarchy("queen_spawn_egg")),
            Map.entry("ichor", antarchy("ichor")), Map.entry("lotus", antarchy("lotus")),
            Map.entry("mucus", antarchy("mucus")), Map.entry("peach", antarchy("peach"))
    );
    private static volatile List<String> answers = List.of();

    private BlockleAnswers() {
    }

    public static BlockleAnswers instance() {
        return INSTANCE;
    }

    public static Answer answerForDay(long day) {
        List<String> currentAnswers = answers;
        if (currentAnswers.isEmpty()) return null;
        int index = (int) Math.floorMod(day + SALT, currentAnswers.size());
        String word = currentAnswers.get(index);
        return new Answer(word, ANSWER_ITEMS.getOrDefault(word, vanilla("book")));
    }

    public static int size() {
        return answers.size();
    }

    @Override
    protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        var resource = resourceManager.getResource(ANSWERS_FILE);
        if (resource.isEmpty()) return List.of();

        try (var reader = resource.get().openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            if (!json.isJsonArray()) return List.of();
            LinkedHashSet<String> validAnswers = new LinkedHashSet<>();
            json.getAsJsonArray().forEach(element -> {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    String word = element.getAsString();
                    if (word.matches("(?i)[a-z]{5}")) validAnswers.add(word.toLowerCase(java.util.Locale.ROOT));
                }
            });
            return List.copyOf(validAnswers);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    @Override
    protected void apply(List<String> loadedAnswers, ResourceManager resourceManager, ProfilerFiller profiler) {
        answers = loadedAnswers;
        Antarchy.LOGGER.info("Loaded {} valid Blockle answers", answers.size());
    }

    public record Answer(String word, ResourceLocation itemId) {
    }

    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static ResourceLocation antarchy(String path) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
    }
}
