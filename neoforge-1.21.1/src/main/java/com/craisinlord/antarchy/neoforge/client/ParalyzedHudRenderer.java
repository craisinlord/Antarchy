package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeMisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public final class ParalyzedHudRenderer {

    private ParalyzedHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isDeadOrDying()) {
            return;
        }

        MobEffectInstance effect = player.getEffect(AntarchyNeoforgeMisc.PARALYZED);
        if (effect == null) {
            return;
        }

        int duration = effect.getDuration();
        float fade = effect.isInfiniteDuration()
                ? 1.0F
                : Math.min(1.0F, Math.max(duration, 20) / 20.0F);
        float pulse = 0.85F + 0.15F * Mth.sin(player.tickCount * ((float) Math.PI * 2.0F / 120.0F));

        int alpha = Mth.clamp((int) (fade * pulse * 0xC0), 0, 0xFF);

        int width  = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int color = (alpha << 24) | 0x303030;
        guiGraphics.fill(0, 0, width, height, color);
        int innerAlpha = Mth.clamp(alpha / 3, 0, 0xFF);
        int innerColor = (innerAlpha << 24) | 0x606060;
        int margin = Math.min(width, height) / 6;
        guiGraphics.fill(margin, margin, width - margin, height - margin, innerColor);
    }
}
