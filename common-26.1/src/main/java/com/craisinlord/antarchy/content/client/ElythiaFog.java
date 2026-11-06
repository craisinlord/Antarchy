package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class ElythiaFog {
    private ElythiaFog() {
    }

    public static float start(ClientLevel level, float renderDistance, float partialTick) {
        if (!AntarchySettings.elythiaFogEnabled()) {
            return renderDistance - Mth.clamp(renderDistance / 10.0F, 4.0F, 64.0F);
        }
        float night = night(level, partialTick);
        return Math.min(Mth.lerp(night, 32.0F, 0.0F), end(level, renderDistance, partialTick) * 0.5F);
    }

    public static float end(ClientLevel level, float renderDistance, float partialTick) {
        if (!AntarchySettings.elythiaFogEnabled()) {
            return renderDistance;
        }
        return renderDistance * Mth.lerp(night(level, partialTick), 0.9F, 0.6F);
    }

    private static float night(ClientLevel level, float partialTick) {
        return Mth.clamp(starBrightness(level) * 2.0F, 0.0F, 1.0F);
    }
    private static float starBrightness(ClientLevel level) {
        double fraction = Mth.frac(level.getOverworldClockTime() / 24000.0D - 0.25D);
        double eased = 0.5D - Math.cos(fraction * Math.PI) / 2.0D;
        float timeOfDay = (float) (fraction * 2.0D + eased) / 3.0F;
        float brightness = Mth.clamp(1.0F - (Mth.cos(timeOfDay * Mth.TWO_PI) * 2.0F + 0.25F), 0.0F, 1.0F);
        return brightness * brightness * 0.5F;
    }

}
