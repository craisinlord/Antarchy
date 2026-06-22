package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ThoraxisWeatherClientState {
    private static final Map<ResourceLocation, ThoraxisWeatherState> STATES = new HashMap<>();

    private ThoraxisWeatherClientState() {
    }

    public static void apply(ResourceLocation dimension, ThoraxisWeatherKind kind, long expiresAt, int anchorX, int anchorY, int anchorZ) {
        if (kind == ThoraxisWeatherKind.NONE) {
            STATES.remove(dimension);
            return;
        }

        STATES.put(dimension, new ThoraxisWeatherState(kind, expiresAt, new BlockPos(anchorX, anchorY, anchorZ)));
    }

    public static ThoraxisWeatherState current(Level level) {
        if (level == null) {
            return null;
        }

        ThoraxisWeatherState state = STATES.get(level.dimension().location());
        if (state == null) {
            return null;
        }

        long now = level.getGameTime();
        if (state.expiresAt > 0L && now > state.expiresAt) {
            STATES.remove(level.dimension().location());
            return null;
        }

        return state;
    }

    public record ThoraxisWeatherState(ThoraxisWeatherKind kind, long expiresAt, BlockPos anchor) {
    }
}
