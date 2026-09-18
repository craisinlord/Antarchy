package com.craisinlord.antarchy.content.entity.trades;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public final class PestControlTradeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static volatile List<Entry> trades = List.of();

    public PestControlTradeManager() {
        super(GSON, "pest_control_trades");
    }

    public static int[] slotsPerLevel() {
        return new int[] {3, 4, 3, 2, 2};
    }

    public static VillagerTrades.ItemListing listing(int level, int index) {
        return (villager, random) -> trades.stream().filter(entry -> entry.level == level && entry.index == index)
                .findFirst().map(entry -> entry.offer(random)).orElse(null);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        List<Entry> parsed = new ArrayList<>();
        for (JsonElement resource : resources.values()) {
            if (!resource.isJsonObject()) continue;
            JsonObject root = resource.getAsJsonObject();
            if (root.has("trades")) {
                for (JsonElement element : root.getAsJsonArray("trades")) if (element.isJsonObject()) parsed.add(Entry.parse(element.getAsJsonObject()));
            } else parsed.add(Entry.parse(root));
        }
        trades = List.copyOf(parsed);
    }

    private record Entry(int level, int index, String buy, int buyCount, String sell, int sellCount,
                         String secondBuy, int secondBuyCount, int maxUses, int xp) {
        private MerchantOffer offer(RandomSource random) {
            var buyItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(buy));
            var sellItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(sell));
            var secondCost = secondBuy == null ? java.util.Optional.<ItemCost>empty()
                    : java.util.Optional.of(new ItemCost(BuiltInRegistries.ITEM.get(ResourceLocation.parse(secondBuy)), secondBuyCount));
            return new MerchantOffer(new ItemCost(buyItem, buyCount), secondCost, new ItemStack(sellItem, sellCount), maxUses, xp, 0.05F);
        }

        private static Entry parse(JsonObject json) {
            JsonObject buy = json.getAsJsonObject("buy");
            JsonObject sell = json.getAsJsonObject("sell");
            JsonObject second = json.has("second_buy") ? json.getAsJsonObject("second_buy") : null;
            return new Entry(json.get("level").getAsInt(), json.get("index").getAsInt(), buy.get("item").getAsString(),
                    count(buy), sell.get("item").getAsString(), count(sell), second == null ? null : second.get("item").getAsString(),
                    second == null ? 0 : count(second), json.has("max_uses") ? json.get("max_uses").getAsInt() : 12,
                    json.has("villager_xp") ? json.get("villager_xp").getAsInt() : 5);
        }

        private static int count(JsonObject stack) {
            if (stack.has("min") && stack.has("max")) return stack.get("min").getAsInt();
            return stack.has("count") ? stack.get("count").getAsInt() : stack.get("min").getAsInt();
        }
    }
}
