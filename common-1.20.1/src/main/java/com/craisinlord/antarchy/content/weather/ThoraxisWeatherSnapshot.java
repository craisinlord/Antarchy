package com.craisinlord.antarchy.content.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record ThoraxisWeatherSnapshot(
        ResourceLocation dimension,
        ThoraxisWeatherKind kind,
        long expiresAt,
        BlockPos anchor
) {
    public boolean isActive() {
        return this.kind != ThoraxisWeatherKind.NONE;
    }
}
