package com.craisinlord.antarchy.content.time;

import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ChronosphereManager {
    public static final double RATE = 0.35D;
    private static final Map<UUID, UUID> ACTIVE_FIELDS = new HashMap<>();
    private ChronosphereManager() {}

    public static void refresh(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        int enchantmentLevel = AntarchyEnchantments.chronosphereLevel(player);
        UUID playerId = player.getUUID();
        TimeDilationFieldEntity field = null;
        UUID known = ACTIVE_FIELDS.get(playerId);
        if (known != null && level.getEntity(known) instanceof TimeDilationFieldEntity existing) field = existing;
        if (enchantmentLevel <= 0 || !player.isAlive()) {
            ACTIVE_FIELDS.remove(playerId);
            if (field != null) field.discard();
            return;
        }
        if (field == null) {
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (entity instanceof TimeDilationFieldEntity candidate && candidate.isChronosphere() && candidate.isOwnedBy(player)) {
                    field = candidate;
                    break;
                }
            }
        }
        if (field == null) {
            field = TimeDilationApi.createField(level, player.position(), radius(enchantmentLevel), RATE, -1, player.getUUID());
            field.configureChronosphere(player);
        } else {
            field.configureChronosphere(player);
        }
        field.setFieldRadius(radius(enchantmentLevel));
        ACTIVE_FIELDS.put(playerId, field.getUUID());
    }

    public static double radius(int level) {
        return level == 1 ? 1.5D : level == 2 ? 2.5D : 3.5D;
    }

}
