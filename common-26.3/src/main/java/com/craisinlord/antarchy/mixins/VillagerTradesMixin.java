package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractVillager.class)
public abstract class VillagerTradesMixin {
    @WrapOperation(
            method = "addOffersFromTradeSet",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/TradeSet;trades()Lnet/minecraft/core/HolderSet;")
    )
    private HolderSet<VillagerTrade> antarchy$appendTrades(TradeSet tradeSet, Operation<HolderSet<VillagerTrade>> original,
                                                           @Local(argsOnly = true) ResourceKey<TradeSet> key) {
        HolderSet<VillagerTrade> trades = original.call(tradeSet);
        if (key.equals(TradeSets.FARMER_LEVEL_1)) {
            return antarchy$with(trades, antarchy$emeraldForItems(AntarchyObjects.CORN, 20, 16, 2));
        }
        if (key.equals(TradeSets.BUTCHER_LEVEL_1)) {
            return antarchy$with(trades, antarchy$emeraldForItems(AntarchyObjects.COOKED_CORNDOG, 5, 16, 2));
        }
        if (key.equals(TradeSets.WANDERING_TRADER_COMMON)) {
            return antarchy$with(trades, antarchy$itemsForEmerald(AntarchyObjects.CORN_SEEDS, 1, 3, 12, 2));
        }
        return trades;
    }

    @Unique
    private static VillagerTrade antarchy$emeraldForItems(Supplier<? extends Item> itemSupplier, int count, int maxUses, int villagerXp) {
        return VillagerTrade.builder(new TradeCost(itemSupplier.get(), count), new ItemStackTemplate(Items.EMERALD), maxUses, villagerXp, 0.05F).build();
    }

    @Unique
    private static VillagerTrade antarchy$itemsForEmerald(Supplier<? extends Item> itemSupplier, int emeraldCost, int count, int maxUses, int villagerXp) {
        return VillagerTrade.builder(new TradeCost(Items.EMERALD, emeraldCost), new ItemStackTemplate(itemSupplier.get(), count), maxUses, villagerXp, 0.05F).build();
    }

    @Unique
    private static HolderSet<VillagerTrade> antarchy$with(HolderSet<VillagerTrade> trades, VillagerTrade extra) {
        List<Holder<VillagerTrade>> merged = new ArrayList<>(trades.size() + 1);
        for (Holder<VillagerTrade> holder : trades) {
            merged.add(holder);
        }
        merged.add(Holder.direct(extra));
        return HolderSet.direct(merged);
    }
}
