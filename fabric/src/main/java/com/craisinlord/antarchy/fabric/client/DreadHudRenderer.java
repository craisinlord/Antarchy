package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

public final class DreadHudRenderer {
    private static final Holder<MobEffect> DREAD_EFFECT = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(AntarchyFabricContent.DREAD.get());

    private DreadHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || player.isDeadOrDying() || !player.hasEffect(DREAD_EFFECT)) {
            return;
        }

        float pulse = Math.max(0.0F, Mth.sin(player.tickCount * ((float) Math.PI * 2.0F / 80.0F)));
        int alpha = Mth.clamp(18 + Mth.floor(pulse * 64.0F), 0, 255);
        int color = (alpha << 24) | 0x120000;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        guiGraphics.fill(0, 0, width, height, color);
        DreadClientHandler.render(guiGraphics);
    }
}
