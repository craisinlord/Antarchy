package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class PortalGunCrosshairRenderer {
    private static final int WIDTH = 34;
    private static final int HEIGHT = 33;
    private static final int HALF_WIDTH = 17;
    private static final float MISSING_BRIGHTNESS = 0.3F;
    private static final float MISSING_ALPHA = 0.75F;
    private static ClientLevel cachedLevel;
    private static long cachedGameTime = Long.MIN_VALUE;
    private static UUID cachedGunId;
    private static Set<PortalGunPortalEntity.PortalSide> cachedPortals = Set.of();

    private PortalGunCrosshairRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null || minecraft.options.hideGui
                || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        ItemStack held = minecraft.player.getMainHandItem();
        if (!(held.getItem() instanceof PortalGunItem)) {
            held = minecraft.player.getOffhandItem();
            if (!(held.getItem() instanceof PortalGunItem)) {
                return;
            }
        }
        ResourceLocation crosshair = ((PortalGunItem) held.getItem()).getVariant().crosshairTexture();
        UUID gunId = PortalGunItem.getGunId(held);
        PortalGunIndicatorClientState.requestIfNeeded(minecraft, gunId);
        Set<PortalGunPortalEntity.PortalSide> portals = portalsFor(minecraft, gunId);
        PortalGunIndicatorPayload serverState = PortalGunIndicatorClientState.get(gunId);
        boolean blueAvailable = serverState == null
                ? portals.contains(PortalGunPortalEntity.PortalSide.BLUE)
                : serverState.blueAvailable();
        boolean orangeAvailable = serverState == null
                ? portals.contains(PortalGunPortalEntity.PortalSide.ORANGE)
                : serverState.orangeAvailable();

        int left = (minecraft.getWindow().getGuiScaledWidth() - WIDTH) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - HEIGHT) / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawHalf(guiGraphics, crosshair, left + 6, top, 0.0F, blueAvailable);
        drawHalf(guiGraphics, crosshair, left + 11, top, HALF_WIDTH, orangeAvailable);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void drawHalf(GuiGraphics guiGraphics, ResourceLocation crosshair, int x, int y, float u, boolean available) {
        if (available) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            guiGraphics.setColor(MISSING_BRIGHTNESS, MISSING_BRIGHTNESS, MISSING_BRIGHTNESS, MISSING_ALPHA);
        }
        guiGraphics.blit(crosshair, x, y, u, 0.0F, HALF_WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    private static Set<PortalGunPortalEntity.PortalSide> portalsFor(Minecraft minecraft, UUID gunId) {
        if (gunId == null) {
            return Set.of();
        }
        ClientLevel level = minecraft.level;
        long gameTime = level.getGameTime();
        if (cachedLevel != level || cachedGameTime != gameTime || !gunId.equals(cachedGunId)) {
            Set<PortalGunPortalEntity.PortalSide> portals = EnumSet.noneOf(PortalGunPortalEntity.PortalSide.class);
            for (PortalGunPortalEntity portal : com.craisinlord.antarchy.content.portalgun.PortalGunPortalRegistry.all(level)) {
                if (portal.isAlive() && gunId.equals(portal.getGunId())) {
                    portals.add(portal.getPortalSide());
                }
            }
            cachedLevel = level;
            cachedGameTime = gameTime;
            cachedGunId = gunId;
            cachedPortals = Set.copyOf(portals);
        }
        return cachedPortals;
    }
}
