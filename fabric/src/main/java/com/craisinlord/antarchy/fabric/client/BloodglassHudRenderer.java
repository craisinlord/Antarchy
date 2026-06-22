package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.BloodglassClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class BloodglassHudRenderer {
    private static final ResourceLocation HEART_CONTAINER =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/heart/container.png");
    private static final ResourceLocation HEART_FULL =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/gui/sprites/hud/bloodglass_heart.png");
    private static final ResourceLocation HEART_CRACKED =
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/gui/sprites/hud/bloodglass_heart_cracked.png");
    private static final int HEART_SIZE = 9;
    private static final int HEART_PITCH = 8;

    private BloodglassHudRenderer() {}

    public static void render(GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        Player player = mc.player;
        if (player == null || player.isDeadOrDying()) return;
        // Don't show in creative or spectator — no health bar visible there
        if (player.isCreative() || player.isSpectator()) return;

        int total = BloodglassClientState.getShieldsMax();
        if (total <= 0) return;

        int active = BloodglassClientState.getShieldsActive();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // Vanilla health bar bottom row is at y = h - 39; each extra row (absorption, extra hearts) is 10px above.
        // The armor protection icons sit one row above the bottom health row (h - 49).
        // We place bloodglass hearts above both health+absorption stack AND the armor row.
        int maxHealth = Mth.ceil(player.getMaxHealth());
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int healthHearts = Mth.ceil(maxHealth / 2.0f);
        int absorptionHearts = Mth.ceil(absorption / 2.0f);
        int totalVanillaRows = Mth.ceil((healthHearts + absorptionHearts) / 10.0f);
        boolean hasArmor = player.getArmorValue() > 0;
        // +1 to clear the armor icon row only when armor is actually equipped
        int baseTop = h - 39 - (totalVanillaRows * 10) - (hasArmor ? 10 : 0);

        int left = w / 2 - 91;

        // Wrap across multiple rows if more than 10 bloodglass hearts
        for (int i = 0; i < total; i++) {
            int row = i / 10;
            int col = i % 10;
            int x = left + col * HEART_PITCH;
            int y = baseTop - row * 10;
            ResourceLocation tex = (i < active) ? HEART_FULL : HEART_CRACKED;
            gui.blit(HEART_CONTAINER, x, y, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
            gui.blit(tex, x, y, 0, 0, HEART_SIZE, HEART_SIZE, HEART_SIZE, HEART_SIZE);
        }
    }
}
