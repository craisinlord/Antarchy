package com.craisinlord.antarchy.content.entity.trades;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DrTrayaurusTradeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final String DIRECTORY = "trayaurus_trades";
    private static final Codec<TradeFile> TRADE_FILE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TradeEntry.CODEC.listOf().fieldOf("trades").forGetter(TradeFile::trades)
    ).apply(instance, TradeFile::new));
    private static volatile List<TradeEntry> loadedTrades = defaultTrades();

    public DrTrayaurusTradeManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(java.util.Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<TradeEntry> parsedTrades = new ArrayList<>();
        for (java.util.Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            TRADE_FILE_CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(message -> {
                        throw new JsonParseException("Failed to parse " + entry.getKey() + ": " + message);
                    })
                    .ifPresent(file -> parsedTrades.addAll(file.trades()));
        }

        loadedTrades = parsedTrades.isEmpty() ? defaultTrades() : List.copyOf(parsedTrades);
    }

    public static MerchantOffers createOffers() {
        MerchantOffers offers = new MerchantOffers();
        for (TradeEntry trade : loadedTrades) {
            trade.toOffer().ifPresent(offers::add);
        }
        return offers;
    }

    private static List<TradeEntry> defaultTrades() {
        return List.of(
                new TradeEntry(new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "uranium_ingot"), 4), Optional.empty(), new StackEntry(ResourceLocation.withDefaultNamespace("spyglass"), 1), 16, 1, 0.05F),
                new TradeEntry(new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "titanium_ingot"), 6), Optional.empty(), new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "duct_tape"), 2), 12, 5, 0.05F),
                new TradeEntry(new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "uranium_ingot"), 8), Optional.of(new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "kraken_tooth"), 1)), new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "duplicator_sapling"), 1), 6, 10, 0.1F),
                new TradeEntry(new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "titanium_ingot"), 10), Optional.empty(), new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "easter_bunny_spawn_egg"), 1), 3, 5, 0.05F),
                new TradeEntry(new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "uranium_ingot"), 16), Optional.of(new StackEntry(ResourceLocation.fromNamespaceAndPath("antarchy", "titanium_ingot"), 8)), new StackEntry(ResourceLocation.withDefaultNamespace("enchanted_golden_apple"), 1), 2, 20, 0.2F)
        );
    }

    private record TradeFile(List<TradeEntry> trades) {
    }

    private record TradeEntry(StackEntry buy, Optional<StackEntry> secondBuy, StackEntry sell, int maxUses, int villagerXp, float priceMultiplier) {
        private static final Codec<TradeEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                StackEntry.CODEC.fieldOf("buy").forGetter(TradeEntry::buy),
                StackEntry.CODEC.optionalFieldOf("second_buy").forGetter(TradeEntry::secondBuy),
                StackEntry.CODEC.fieldOf("sell").forGetter(TradeEntry::sell),
                Codec.INT.optionalFieldOf("max_uses", 12).forGetter(TradeEntry::maxUses),
                Codec.INT.optionalFieldOf("villager_xp", 1).forGetter(TradeEntry::villagerXp),
                Codec.FLOAT.optionalFieldOf("price_multiplier", 0.05F).forGetter(TradeEntry::priceMultiplier)
        ).apply(instance, TradeEntry::new));

        private Optional<MerchantOffer> toOffer() {
            if (this.maxUses < 1 || this.villagerXp < 1 || this.villagerXp > 99) {
                return Optional.empty();
            }

            Optional<ItemCost> buyCost = this.buy.toCost();
            if (buyCost.isEmpty()) {
                return Optional.empty();
            }

            Optional<ItemCost> secondBuyCost = this.secondBuy.flatMap(StackEntry::toCost);
            ItemStack sellStack = this.sell.toStack();
            if (sellStack.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new MerchantOffer(
                    buyCost.get(),
                    secondBuyCost,
                    sellStack,
                    this.maxUses,
                    this.villagerXp,
                    this.priceMultiplier
            ));
        }
    }

    private record StackEntry(ResourceLocation itemId, int count) {
        private static final Codec<StackEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(StackEntry::itemId),
                Codec.INT.optionalFieldOf("count", 1).forGetter(StackEntry::count)
        ).apply(instance, StackEntry::new));

        private Optional<Item> item() {
            return BuiltInRegistries.ITEM.getOptional(this.itemId)
                    .filter(item -> item != net.minecraft.world.item.Items.AIR);
        }

        private ItemStack toStack() {
            return this.item()
                    .filter(item -> this.count > 0)
                    .map(item -> new ItemStack(item, this.count))
                    .orElse(ItemStack.EMPTY);
        }

        private Optional<ItemCost> toCost() {
            return this.item()
                    .filter(item -> this.count > 0)
                    .map(item -> new ItemCost(item, this.count));
        }
    }
}
