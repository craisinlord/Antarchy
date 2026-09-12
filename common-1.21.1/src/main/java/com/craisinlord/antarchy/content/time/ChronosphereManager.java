package com.craisinlord.antarchy.content.time;

import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ChronosphereManager {
    public static final double RATE = 0.35D;
    private static final Map<UUID, UUID> ACTIVE_FIELDS = new HashMap<>();

    private ChronosphereManager() {
    }

    public static void refresh(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        int enchantmentLevel = AntarchyEnchantments.chronosphereLevel(player);
        UUID playerId = player.getUUID();
        UUID fieldId = ACTIVE_FIELDS.get(playerId);
        TimeDilationFieldEntity field = fieldId != null && level.getEntity(fieldId) instanceof TimeDilationFieldEntity existing
                ? existing
                : null;
        if (enchantmentLevel <= 0 || !player.isAlive()) {
            ACTIVE_FIELDS.remove(playerId);
            if (field != null) {
                field.discard();
            }
            return;
        }
        List<TimeDilationFieldEntity> fields = new ArrayList<>();
        if (field == null) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof TimeDilationFieldEntity candidate && candidate.isChronosphere() && candidate.isOwnedBy(player)) {
                    fields.add(candidate);
                }
            }
        }
        if (field == null && fields.isEmpty()) {
            field = TimeDilationApi.createField(level, player.position(), radius(enchantmentLevel), RATE, -1, player.getUUID(), false);
            field.configureChronosphere(player);
        } else if (field == null) {
            field = fields.getFirst();
            field.configureChronosphere(player);
            for (int index = 1; index < fields.size(); index++) {
                fields.get(index).discard();
            }
        }
        field.setFieldRadius(radius(enchantmentLevel));
        ACTIVE_FIELDS.put(playerId, field.getUUID());
    }

    public static double radius(int level) {
        return switch (level) {
            case 1 -> 1.5D;
            case 2 -> 2.5D;
            default -> 3.5D;
        };
    }
}
