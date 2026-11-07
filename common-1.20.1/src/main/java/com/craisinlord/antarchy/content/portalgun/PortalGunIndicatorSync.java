package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class PortalGunIndicatorSync {
    private PortalGunIndicatorSync() {
    }

    public static void handleRequest(ServerPlayer player, PortalGunIndicatorRequestPayload request) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack stack = main.getItem() instanceof PortalGunItem && request.gunId().equals(PortalGunItem.getGunId(main))
                ? main
                : off.getItem() instanceof PortalGunItem && request.gunId().equals(PortalGunItem.getGunId(off)) ? off : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Optional<UUID> blueId = portalId(level, request.gunId(), PortalGunPortalEntity.PortalSide.BLUE);
        Optional<UUID> orangeId = portalId(level, request.gunId(), PortalGunPortalEntity.PortalSide.ORANGE);
        PortalGunPortalEntity.PortalSide moonSide = PortalGunItem.getMoonSide(stack);
        boolean blue = blueId.isPresent()
                || moonSide == PortalGunPortalEntity.PortalSide.BLUE
                || isMoonPortal(level, orangeId);
        boolean orange = orangeId.isPresent()
                || moonSide == PortalGunPortalEntity.PortalSide.ORANGE
                || isMoonPortal(level, blueId);
        AntarchyNetwork.sendToPlayer(player, new PortalGunIndicatorPayload(request.gunId(), blue, orange));
    }

    private static Optional<UUID> portalId(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        return PortalGunSavedData.getPortalId(level.getServer(), gunId, side, level.dimension().location());
    }

    private static boolean isMoonPortal(ServerLevel level, Optional<UUID> portalId) {
        return portalId.isPresent()
                && level.getEntity(portalId.get()) instanceof PortalGunPortalEntity portal
                && portal.isMoonPortal();
    }
}
