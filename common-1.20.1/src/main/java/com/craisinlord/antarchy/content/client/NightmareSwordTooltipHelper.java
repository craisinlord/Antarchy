package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.NightmareSwordItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class NightmareSwordTooltipHelper {
    private NightmareSwordTooltipHelper() {
    }

    public static Component damageLine() {
        Player player = Minecraft.getInstance().player;
        float damage = player != null
                ? NightmareSwordItem.calculateDamage(player)
                : (float) AntarchySettings.nightmareSwordBaseDamage();
        return Component.translatable("tooltip.antarchy.nightmare_sword_damage", formatDamage(damage)).withStyle(ChatFormatting.RED);
    }

    private static String formatDamage(float damage) {
        float rounded = Math.round(damage * 10.0F) / 10.0F;
        if (rounded == (int) rounded) {
            return String.valueOf((int) rounded);
        }
        return String.valueOf(rounded);
    }
}
