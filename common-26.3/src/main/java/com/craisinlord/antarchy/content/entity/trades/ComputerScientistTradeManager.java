package com.craisinlord.antarchy.content.entity.trades;

import com.craisinlord.antos.content.AntOSObjects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ComputerScientistTradeManager extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final String DIRECTORY = "computer_scientist_trades";
    private static final List<Identifier> GENERAL_DISKS = List.of(
            id("high_fructose_corn_syrup"),
            id("elythia"), id("flying_squirrel"), id("rainbow_ant"), id("kraken"), id("cavaryn"), id("thoraxis"),
            id("temporal_fields"), id("bloodglass"), id("vortex"));
    private static volatile List<TradeEntry> loadedTrades = List.of();

    public ComputerScientistTradeManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    public static int[] slotsPerLevel() {
        return new int[] {4, 2, 2, 3, 5};
    }

    public static void addOffers(Entity trader, int villagerLevel, RandomSource random, MerchantOffers offers) {
        int[] slots = slotsPerLevel();
        if (villagerLevel < 1 || villagerLevel > slots.length) {
            return;
        }
        List<AntarchyTradeListing> listings = new ArrayList<>();
        for (int index = 0; index < slots[villagerLevel - 1]; index++) {
            listings.add(listing(villagerLevel, index));
        }
        AntarchyTradeListing.addOffers(trader, random, offers, listings, AntarchyTradeListing.OFFERS_PER_LEVEL);
    }

    public static AntarchyTradeListing listing(int level, int index) {
        return (trader, random) -> {
            TradeEntry trade = loadedTrades.stream()
                    .filter(entry -> entry.level == level && entry.index == index)
                    .findFirst()
                    .orElse(null);
            return trade == null ? null : trade.createOffer(random);
        };
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<TradeEntry> parsed = new ArrayList<>();
        for (JsonElement file : object.values()) {
            if (!file.isJsonObject()) continue;
            JsonObject root = file.getAsJsonObject();
            if (root.has("trades") && root.get("trades").isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray("trades")) {
                    if (element.isJsonObject()) parsed.add(parse(element.getAsJsonObject()));
                }
            } else {
                parsed.add(parse(root));
            }
        }
        loadedTrades = List.copyOf(parsed);
    }

    private static TradeEntry parse(JsonObject json) {
        return new TradeEntry(
                    integer(json, "level", 1), integer(json, "index", 0),
                    stack(json.getAsJsonObject("buy")),
                    json.has("second_buy") ? stack(json.getAsJsonObject("second_buy")) : null,
                    stack(json.getAsJsonObject("sell")),
                    json.has("sell_disk") ? json.get("sell_disk").getAsString() : null,
                    integer(json, "max_uses", 12), integer(json, "villager_xp", 5),
                    json.has("price_multiplier") ? json.get("price_multiplier").getAsFloat() : 0.05F,
                    json.has("chance") ? json.get("chance").getAsFloat() : 1.0F);
    }

    private record TradeEntry(int level, int index, StackRange buy, StackRange secondBuy,
                               StackRange sell, String sellDisk, int maxUses, int villagerXp,
                               float priceMultiplier, float chance) {
        private MerchantOffer createOffer(net.minecraft.util.RandomSource random) {
            if (random.nextFloat() > chance) return null;
            ItemCost buyCost = buy.toCost(random);
            java.util.Optional<ItemCost> secondCost = secondBuy == null
                    ? java.util.Optional.empty() : java.util.Optional.of(secondBuy.toCost(random));
            ItemStack result = sellDisk == null ? sell.toStack(random) : diskStack(sellDisk, random, sell.toStack(random).getCount());
            return new MerchantOffer(buyCost, secondCost, result, maxUses, villagerXp, priceMultiplier);
        }
    }

    private record StackRange(Identifier item, int min, int max) {
        private Item itemValue() {
            return BuiltInRegistries.ITEM.getValue(item);
        }

        private int count(net.minecraft.util.RandomSource random) {
            return min == max ? min : min + random.nextInt(max - min + 1);
        }

        private ItemCost toCost(net.minecraft.util.RandomSource random) {
            return new ItemCost(itemValue(), count(random));
        }

        private ItemStack toStack(net.minecraft.util.RandomSource random) {
            return new ItemStack(itemValue(), count(random));
        }
    }

    private static StackRange stack(JsonObject json) {
        int min = integer(json, "min", integer(json, "count", 1));
        int max = integer(json, "max", min);
        return new StackRange(Identifier.parse(json.get("item").getAsString()), min, max);
    }

    private static ItemStack diskStack(String diskType, net.minecraft.util.RandomSource random, int count) {
        Identifier diskId = diskType.equals("random_general")
                ? GENERAL_DISKS.get(random.nextInt(GENERAL_DISKS.size()))
                : Identifier.parse(diskType);
        ItemStack disk = new ItemStack(AntOSObjects.FLOPPY_DISK.get(), count);
        disk.set(AntOSObjects.FLOPPY_DISK_COMPONENT.get(), diskId);
        return disk;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("antarchy", path);
    }
}
