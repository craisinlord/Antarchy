package com.craisinlord.antarchy.content.block.entity.undertrial;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class UndertrialOmen {
    public static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    private static final String TAG_PREFIX = "antarchy.trial_omen.";

    private UndertrialOmen() {
    }

    public static boolean hasTrialOmen(Player player) {
        long now = player.level().getGameTime();
        boolean active = false;
        List<String> expired = new ArrayList<>();
        for (String tag : player.getTags()) {
            if (tag.startsWith(TAG_PREFIX)) {
                long expiresAt = parseExpiry(tag);
                if (expiresAt > now) {
                    active = true;
                } else {
                    expired.add(tag);
                }
            }
        }
        for (String tag : expired) {
            player.removeTag(tag);
        }
        return active;
    }

    public static boolean hasBadOmen(Player player) {
        return player.hasEffect(MobEffects.BAD_OMEN);
    }

    public static void transformBadOmenIntoTrialOmen(Player player) {
        MobEffectInstance badOmen = player.getEffect(MobEffects.BAD_OMEN);
        if (badOmen != null) {
            int duration = TRIAL_OMEN_PER_BAD_OMEN_LEVEL * (badOmen.getAmplifier() + 1);
            player.removeEffect(MobEffects.BAD_OMEN);
            grantTrialOmen(player, duration);
        }
    }

    public static void grantTrialOmen(Player player, int duration) {
        List<String> existing = new ArrayList<>();
        for (String tag : player.getTags()) {
            if (tag.startsWith(TAG_PREFIX)) {
                existing.add(tag);
            }
        }
        for (String tag : existing) {
            player.removeTag(tag);
        }
        player.addTag(TAG_PREFIX + (player.level().getGameTime() + duration));
    }

    private static long parseExpiry(String tag) {
        try {
            return Long.parseLong(tag.substring(TAG_PREFIX.length()));
        } catch (NumberFormatException exception) {
            return Long.MIN_VALUE;
        }
    }
}
