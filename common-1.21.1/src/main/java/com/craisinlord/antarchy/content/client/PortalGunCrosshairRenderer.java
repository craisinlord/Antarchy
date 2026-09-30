package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class PortalGunCrosshairRenderer {
    private static final ResourceLocation CROSSHAIR = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/gui/portal_gun_crosshair.png");
    private static final int WIDTH = 34;
    private static final int HEIGHT = 33;
    private static ClientLevel cachedLevel;
    private static long cachedGameTime = Long.MIN_VALUE;
    private static UUID cachedOwnerId;
    private static Set<PortalKey> cachedPortals = Set.of();

    private PortalGunCrosshairRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (PortalGunGrabClientState.isActive()) {
            return;
        }
        if (minecraft.player == null || minecraft.screen != null || minecraft.options.hideGui || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        ItemStack held = minecraft.player.getMainHandItem();
        if (!(held.getItem() instanceof PortalGunItem)) {
            held = minecraft.player.getOffhandItem();
            if (!(held.getItem() instanceof PortalGunItem)) {
                return;
            }
        }
        UUID gunId = PortalGunItem.getGunId(held);
        UUID ownerId = PortalGunItem.getPortalOwnerId(held, minecraft.player.getUUID());
        String channelName = PortalGunItem.getChannelName(held);
        PortalGunIndicatorClientState.requestIfNeeded(minecraft, gunId);
        Set<PortalKey> portals = portalsFor(minecraft, ownerId, channelName);
        PortalGunIndicatorPayload serverState = PortalGunIndicatorClientState.get(gunId);
        boolean blueAvailable = serverState == null
                ? portals.contains(new PortalKey(channelName, PortalGunPortalEntity.PortalSide.BLUE))
                : serverState.blueAvailable();
        boolean orangeAvailable = serverState == null
                ? portals.contains(new PortalKey(channelName, PortalGunPortalEntity.PortalSide.ORANGE))
                : serverState.orangeAvailable();

        int left = (minecraft.getWindow().getGuiScaledWidth() - WIDTH) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - HEIGHT) / 2;
        guiGraphics.blit(CROSSHAIR, left + 6, top, 0.0F, 0.0F, 17, HEIGHT, WIDTH, HEIGHT);
        guiGraphics.blit(CROSSHAIR, left + 11, top, 17.0F, 0.0F, 17, HEIGHT, WIDTH, HEIGHT);
        if (!blueAvailable) {
            guiGraphics.fill(left + 6, top, left + 23, top + HEIGHT, 0x99000000);
        }
        if (!orangeAvailable) {
            guiGraphics.fill(left + 11, top, left + 28, top + HEIGHT, 0x99000000);
        }
        int indicatorSize = AntarchySettings.portalGunIndicatorSize();
        if (indicatorSize > 0) {
            int dot = Math.max(1, Math.round(HEIGHT * indicatorSize / 100.0F));
            int gap = Math.max(1, dot / 3);
            int indicatorLeft = left + (WIDTH - dot * 2 - gap) / 2;
            int indicatorTop = top + HEIGHT + 2;
            guiGraphics.fill(indicatorLeft, indicatorTop, indicatorLeft + dot, indicatorTop + dot,
                    blueAvailable ? 0xFF21A8FF : 0xFF33414A);
            int orangeLeft = indicatorLeft + dot + gap;
            guiGraphics.fill(orangeLeft, indicatorTop, orangeLeft + dot, indicatorTop + dot,
                    orangeAvailable ? 0xFFFF8A24 : 0xFF4A4035);
        }
    }

    private static Set<PortalKey> portalsFor(Minecraft minecraft, UUID ownerId, String channelName) {
        ClientLevel level = minecraft.level;
        long gameTime = level.getGameTime();
        if (cachedLevel != level || cachedGameTime != gameTime || !ownerId.equals(cachedOwnerId)) {
            Set<PortalKey> portals = new HashSet<>();
            if (minecraft.player != null) {
                AABB bounds = minecraft.player.getBoundingBox().inflate(128.0D);
                for (PortalGunPortalEntity portal : level.getEntitiesOfClass(PortalGunPortalEntity.class, bounds, Entity::isAlive)) {
                    UUID portalOwnerId = portal.getOwnerId();
                    String portalChannelName = portal.getChannelName();
                    if (ownerId.equals(portalOwnerId) && portalChannelName != null && !portalChannelName.isEmpty()) {
                        portals.add(new PortalKey(portalChannelName, portal.getPortalSide()));
                        if (portal.getLinkedPortalId() != null) {
                            portals.add(new PortalKey(portalChannelName, PortalGunPortalEntity.PortalSide.BLUE));
                            portals.add(new PortalKey(portalChannelName, PortalGunPortalEntity.PortalSide.ORANGE));
                        }
                    }
                }
            }
            cachedLevel = level;
            cachedGameTime = gameTime;
            cachedOwnerId = ownerId;
            cachedPortals = Set.copyOf(portals);
        }
        if (channelName == null || channelName.isEmpty()) {
            return Set.of();
        }
        return cachedPortals;
    }

    private record PortalKey(String channelName, PortalGunPortalEntity.PortalSide side) {
    }
}
