package com.craisinlord.antarchy.content.entity.royal;

import net.minecraft.world.entity.item.ItemEntity;

public final class RoyalLootDropGlow {
    private static final String TAG_PREFIX = "antarchy_royal_loot_glow_until_";
    public static final int DURATION_TICKS = 20 * 120;

    private RoyalLootDropGlow() {
    }

    public static void start(ItemEntity itemEntity) {
        long expiryTick = itemEntity.level().getGameTime() + DURATION_TICKS;
        itemEntity.addTag(TAG_PREFIX + expiryTick);
        itemEntity.setGlowingTag(true);
    }

    public static void tick(ItemEntity itemEntity) {
        if (itemEntity.level().isClientSide) return;
        for (String tag : itemEntity.getTags()) {
            if (!tag.startsWith(TAG_PREFIX)) continue;
            long expiryTick;
            try {
                expiryTick = Long.parseLong(tag.substring(TAG_PREFIX.length()));
            } catch (NumberFormatException ignored) {
                itemEntity.removeTag(tag);
                continue;
            }
            if (itemEntity.level().getGameTime() < expiryTick) {
                itemEntity.setGlowingTag(true);
                return;
            }
            itemEntity.removeTag(tag);
            itemEntity.setGlowingTag(false);
        }
    }
}
