package com.craisinlord.antarchy.content;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class AntarchyAdvancements {
    private AntarchyAdvancements() {
    }

    public static boolean award(ServerPlayer player, ResourceLocation advancementId) {
        MinecraftServer server = player.serverLevel().getServer();
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
    public static AdvancementHolder get(MinecraftServer server, ResourceLocation advancementId) {
        return server.getAdvancements().get(advancementId);
    }
}
