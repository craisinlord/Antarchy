package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class PortalGunResetManager {
    public static final int ARM = 0;
    public static final int RELEASE = 1;
    public static final int RESET_ALL = 2;
    public static final int RESET_BLUE = 3;
    private static final Map<UUID, ResetState> STATES = new HashMap<>();

    private PortalGunResetManager() {
    }

    public static void handleInput(ServerPlayer player, int action) {
        ItemStack stack = heldPortalGun(player);
        if (stack.isEmpty()) {
            STATES.remove(player.getUUID());
            return;
        }
        UUID gunId = PortalGunItem.ensureGunIdentity(stack, player);
        if (action == ARM) {
            STATES.put(player.getUUID(), new ResetState(gunId, player.serverLevel().getGameTime(), false));
            return;
        }
        ResetState state = activeState(player, gunId);
        if (action == RESET_ALL) {
            PortalGunItem item = (PortalGunItem) stack.getItem();
            item.resetPortals(player, stack);
            STATES.remove(player.getUUID());
        } else if (action == RESET_BLUE && state != null && !state.sideReset()) {
            ((PortalGunItem) stack.getItem()).resetPortalSide(player, stack, PortalGunPortalEntity.PortalSide.BLUE);
            STATES.put(player.getUUID(), new ResetState(gunId, state.startedAt(), true));
        } else if (action == RELEASE) {
            STATES.remove(player.getUUID());
        }
    }

    public static boolean handleOrangeReset(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof PortalGunItem)) {
            return false;
        }
        UUID gunId = PortalGunItem.getGunId(stack);
        ResetState state = activeState(player, gunId);
        if (state == null) {
            return false;
        }
        if (!state.sideReset()) {
            ((PortalGunItem) stack.getItem()).resetPortalSide(player, stack, PortalGunPortalEntity.PortalSide.ORANGE);
            STATES.put(player.getUUID(), new ResetState(gunId, state.startedAt(), true));
        }
        return true;
    }

    public static void clear(MinecraftServer server) {
        STATES.clear();
    }

    private static ResetState activeState(ServerPlayer player, UUID gunId) {
        ResetState state = STATES.get(player.getUUID());
        if (state == null || gunId == null || !state.gunId().equals(gunId)
                || player.serverLevel().getGameTime() - state.startedAt() > 100L) {
            STATES.remove(player.getUUID());
            return null;
        }
        return state;
    }

    private static ItemStack heldPortalGun(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof PortalGunItem) {
            return main;
        }
        ItemStack offhand = player.getOffhandItem();
        return offhand.getItem() instanceof PortalGunItem ? offhand : ItemStack.EMPTY;
    }

    private record ResetState(UUID gunId, long startedAt, boolean sideReset) {
    }
}
