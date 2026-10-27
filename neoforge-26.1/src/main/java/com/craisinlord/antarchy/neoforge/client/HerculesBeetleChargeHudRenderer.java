package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class HerculesBeetleChargeHudRenderer {
    private static final Identifier CHARGE_BAR_BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_background.png");
    private static final Identifier CHARGE_BAR_PROGRESS =
            Identifier.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_progress.png");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private HerculesBeetleChargeHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null || player.isDeadOrDying()) {
            return;
        }
        if (!(player.getVehicle() instanceof HerculesBeetleEntity beetle)) {
            return;
        }

        int charge = beetle.getMountedCharge();
        if (charge <= 0) {
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x0 = width / 2 - BAR_WIDTH / 2;
        int y0 = height - 29;
        float chargeRatio = Mth.clamp(charge / 100.0F, 0.0F, 1.0F);
        int filledWidth = Mth.clamp((int) (chargeRatio * BAR_WIDTH), 0, BAR_WIDTH);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CHARGE_BAR_BACKGROUND, x0, y0, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        if (filledWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CHARGE_BAR_PROGRESS, x0, y0, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT, ARGB.colorFromFloat(0.45F + chargeRatio * 0.55F, 1.0F, 0.78F, 0.2F));
        }
    }
}
