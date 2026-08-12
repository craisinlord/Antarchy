package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerTrades.class)
public abstract class VillagerTradesMixin {
    @Shadow @Mutable @Final private static Map TRADES;
    @Shadow @Mutable @Final private static Int2ObjectMap WANDERING_TRADER_TRADES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void antarchy$appendTrades(CallbackInfo ci) {
        appendVillagerTrade(VillagerProfession.FARMER, 1,
                emeraldForItems(AntarchyObjects.CORN, 20, 16, 2));
        appendVillagerTrade(VillagerProfession.BUTCHER, 1,
                emeraldForItems(AntarchyObjects.COOKED_CORNDOG, 5, 16, 2));
        appendWanderingTrade(0,
                itemsForEmerald(AntarchyObjects.CORN_SEEDS, 1, 3, 12, 2));
    }

    private static VillagerTrades.ItemListing emeraldForItems(Supplier<? extends Item> itemSupplier, int count, int maxUses, int villagerXp) {
        return (trader, random) -> new MerchantOffer(new ItemStack(itemSupplier.get(), count), new ItemStack(Items.EMERALD), maxUses, villagerXp, 0.05F);
    }

    private static VillagerTrades.ItemListing itemsForEmerald(Supplier<? extends Item> itemSupplier, int emeraldCost, int count, int maxUses, int villagerXp) {
        return (trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, emeraldCost), new ItemStack(itemSupplier.get(), count), maxUses, villagerXp, 0.05F);
    }

    @SuppressWarnings("unchecked")
    private static void appendVillagerTrade(VillagerProfession profession, int level, VillagerTrades.ItemListing listing) {
        Object tradeValue = TRADES.get(profession);
        if (!(tradeValue instanceof Map<?, ?> tradeMap)) {
            return;
        }

        Int2ObjectOpenHashMap<VillagerTrades.ItemListing[]> copy = new Int2ObjectOpenHashMap<>();
        for (Map.Entry<?, ?> entry : tradeMap.entrySet()) {
            if (entry.getKey() instanceof Integer key && entry.getValue() instanceof VillagerTrades.ItemListing[] listings) {
                copy.put(key, listings);
            }
        }

        VillagerTrades.ItemListing[] existing = copy.get(level);
        VillagerTrades.ItemListing[] merged = Arrays.copyOf(existing == null ? new VillagerTrades.ItemListing[0] : existing,
                (existing == null ? 0 : existing.length) + 1);
        merged[merged.length - 1] = listing;
        copy.put(level, merged);
        Map<Object, Object> rebuilt = new HashMap<>();
        for (Object entryObject : TRADES.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
            if (entry.getKey() != profession) {
                rebuilt.put(entry.getKey(), entry.getValue());
            }
        }
        rebuilt.put(profession, copy);
        TRADES = rebuilt;
    }

    @SuppressWarnings("unchecked")
    private static void appendWanderingTrade(int group, VillagerTrades.ItemListing listing) {
        Int2ObjectOpenHashMap<VillagerTrades.ItemListing[]> copy = new Int2ObjectOpenHashMap<>();
        for (Object entryObject : ((Map<?, ?>) WANDERING_TRADER_TRADES).entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
            if (entry.getKey() instanceof Integer key && entry.getValue() instanceof VillagerTrades.ItemListing[] listings) {
                copy.put((int) key, listings);
            }
        }

        VillagerTrades.ItemListing[] existing = copy.get(group);
        VillagerTrades.ItemListing[] merged = Arrays.copyOf(existing == null ? new VillagerTrades.ItemListing[0] : existing,
                (existing == null ? 0 : existing.length) + 1);
        merged[merged.length - 1] = listing;
        copy.put(group, merged);
        WANDERING_TRADER_TRADES = copy;
    }
}
