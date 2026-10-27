package com.craisinlord.antarchy.content.entity.trades;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AntarchyTradeListing {
    int OFFERS_PER_LEVEL = 2;

    @Nullable
    MerchantOffer getOffer(Entity trader, RandomSource random);

    static void addOffers(Entity trader, RandomSource random, MerchantOffers offers, List<AntarchyTradeListing> listings, int maxOffers) {
        List<AntarchyTradeListing> remaining = new ArrayList<>(listings);
        int added = 0;
        while (added < maxOffers && !remaining.isEmpty()) {
            MerchantOffer offer = remaining.remove(random.nextInt(remaining.size())).getOffer(trader, random);
            if (offer != null) {
                offers.add(offer);
                added++;
            }
        }
    }
}
