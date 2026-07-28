package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class DorrieJumpHudRenderer {
    private static final ResourceLocation GUI_ICONS_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/icons.png");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private DorrieJumpHudRenderer() {}

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null || player.isDeadOrDying()) return;

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof DorrieEntity dorrie)) return;

        int charge = dorrie.getJumpCharge();
        if (charge <= 0) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x0 = width / 2 - BAR_WIDTH / 2;
        int y0 = height - 29;

        guiGraphics.blit(GUI_ICONS_LOCATION, x0, y0, 0, 84, BAR_WIDTH, BAR_HEIGHT);

        float chargeRatio = Mth.clamp(charge / 100.0F, 0.0F, 1.0F);
        int filledWidth = Mth.clamp((int) (chargeRatio * BAR_WIDTH), 0, BAR_WIDTH);
        if (filledWidth > 0) {
            guiGraphics.setColor(0.2F, 0.8F, 1.0F, 0.4F + chargeRatio * 0.6F);
            guiGraphics.blit(GUI_ICONS_LOCATION, x0, y0, 0, 89, filledWidth, BAR_HEIGHT);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
