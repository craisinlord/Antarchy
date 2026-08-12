package com.craisinlord.antarchy.content.advancement;

import net.minecraft.advancements.Advancement;
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

        Advancement advancement = server.getAdvancements().getAdvancement(advancementId);
        if (advancement == null) {
            return false;
        }

        return player.getAdvancements().award(advancement, "impossible");
    }

    @Nullable
    public static Advancement get(MinecraftServer server, ResourceLocation advancementId) {
        return server.getAdvancements().getAdvancement(advancementId);
    }
}
