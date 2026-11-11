package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

import java.lang.ref.WeakReference;

public final class FungalReserveAtmosphere {
    public static final ResourceKey<Biome> FUNGAL_RESERVE = ResourceKey.create(
            Registries.BIOME,
            new ResourceLocation(Antarchy.MODID, "fungal_reserve")
    );
    private static final float BLEND_PER_TICK = 0.025F;
    private static final float FOG_END = 32.0F;
    private static final float FOG_START_FRACTION = 0.05F;
    private static final float LIGHT_DIM = 0.3F;

    private static WeakReference<ClientLevel> trackedLevel = new WeakReference<>(null);
    private static float previous;
    private static float current;

    private FungalReserveAtmosphere() {
    }

    public static void tick(ClientLevel level, Player player) {
        float target = level.getBiome(player.blockPosition()).is(FUNGAL_RESERVE) ? 1.0F : 0.0F;
        if (level != trackedLevel.get()) {
            trackedLevel = new WeakReference<>(level);
            previous = target;
            current = target;
            return;
        }
        previous = current;
        if (current < target) {
            current = Math.min(target, current + BLEND_PER_TICK);
        } else if (current > target) {
            current = Math.max(target, current - BLEND_PER_TICK);
        }
    }

    public static float factor(float partialTick) {
        return Mth.lerp(partialTick, previous, current);
    }

    public static float fogStart(float baseStart, float baseEnd, float partialTick) {
        float blend = factor(partialTick);
        if (blend <= 0.0F) {
            return baseStart;
        }
        float end = fogEnd(baseEnd, partialTick);
        return Math.min(Mth.lerp(blend, baseStart, end * FOG_START_FRACTION), end * 0.5F);
    }

    public static float fogEnd(float baseEnd, float partialTick) {
        float blend = factor(partialTick);
        if (blend <= 0.0F) {
            return baseEnd;
        }
        return Mth.lerp(blend, baseEnd, Math.min(baseEnd, FOG_END));
    }

    public static float lightDim() {
        return LIGHT_DIM * current;
    }

    public static float lightScale(int blockLight) {
        float torchlight = 1.0F - Mth.clamp(blockLight / 15.0F, 0.0F, 1.0F);
        return 1.0F - lightDim() * torchlight;
    }
}
