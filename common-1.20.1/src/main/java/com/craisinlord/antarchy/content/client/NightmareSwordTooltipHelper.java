package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.NightmareSwordItem;
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
        return NightmareSwordItem.damageLine(damage);
    }
}
