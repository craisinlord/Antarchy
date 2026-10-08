package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class PortalGunIndicatorSync {
    private static BiConsumer<ServerPlayer, PortalGunIndicatorPayload> sender = (player, payload) -> {};

    private PortalGunIndicatorSync() {
    }

    public static void setSender(BiConsumer<ServerPlayer, PortalGunIndicatorPayload> payloadSender) {
        sender = payloadSender;
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
        boolean blue = PortalGunSavedData.getPortalId(
                player.serverLevel().getServer(),
                request.gunId(),
                PortalGunPortalEntity.PortalSide.BLUE,
                player.serverLevel().dimension().location()
        ).isPresent();
        boolean orange = PortalGunSavedData.getPortalId(
                player.serverLevel().getServer(),
                request.gunId(),
                PortalGunPortalEntity.PortalSide.ORANGE,
                player.serverLevel().dimension().location()
        ).isPresent();
        sender.accept(player, new PortalGunIndicatorPayload(request.gunId(), blue, orange));
    }
}
