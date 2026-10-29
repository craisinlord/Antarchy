package com.craisinlord.antarchy.content.glimmerecho;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface GlimmerEcho {
    boolean isGlimmerEcho();

    void setGlimmerEcho(boolean glimmerEcho);

    default float glimmerEchoDamageMultiplier() {
        return 1.0F;
    }

    default void setGlimmerEchoOwner(@Nullable UUID owner) {
    }

    default void onGlimmerSummoned(ServerLevel level, GlimmerSite.Match site, ServerPlayer summoner) {
    }
}
