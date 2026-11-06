package com.craisinlord.antarchy.content.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class AntarchyAdvancements {
    private AntarchyAdvancements() {
    }

    public static boolean award(ServerPlayer player, Identifier advancementId) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }

        AdvancementHolder holder = server.getAdvancements().get(advancementId);
        if (holder == null) {
            return false;
        }

        return player.getAdvancements().award(holder, "impossible");
    }

    @Nullable
    public static AdvancementHolder get(MinecraftServer server, Identifier advancementId) {
        return server.getAdvancements().get(advancementId);
    }
}
