package com.craisinlord.antarchy.content.client;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class AlexsCavesFog {
    private static final TagKey<Biome> CAVE_BIOMES = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("alexscaves", "alexs_caves_biomes"));
    private static final float FADE_MILLIS = 1500.0F;
    private static final long SNAP_MILLIS = 1000L;

    private static ResourceKey<Level> lastDimension;
    private static long lastMillis;
    private static float amount;

    private AlexsCavesFog() {
    }

    public static float amount(ClientLevel level, Camera camera) {
        long now = Util.getMillis();
        float target = level.getBiome(camera.getBlockPosition()).is(CAVE_BIOMES) ? 1.0F : 0.0F;
        long elapsed = now - lastMillis;
        if (level.dimension() != lastDimension || elapsed > SNAP_MILLIS) {
            amount = target;
        } else {
            amount = Mth.approach(amount, target, elapsed / FADE_MILLIS);
        }
        lastDimension = level.dimension();
        lastMillis = now;
        return amount;
    }
}
