package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class HordeHudRenderer {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );

    private HordeHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.player.isDeadOrDying() || !minecraft.level.dimension().equals(CAVARYN)) {
            return;
        }

        float intensity = HordeClientState.intensity();
        if (intensity <= 0.01F) {
            return;
        }

        float pulse = 0.9F + 0.1F * Mth.sin(minecraft.player.tickCount * ((float) Math.PI * 2.0F / 90.0F));
        int alpha = Mth.clamp((int) (intensity * pulse * 0x2C), 0, 0x2C);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int color = (alpha << 24) | 0xD84A16;
        guiGraphics.fill(0, 0, width, height, color);
    }
}
