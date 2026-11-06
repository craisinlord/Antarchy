package com.craisinlord.antarchy.content.entity.royal;

import java.util.Locale;

public enum RoyalPunishmentType {
    ROYAL_EXILE("royal_exile"),
    ROYAL_TITHE("royal_tithe"),
    KINGS_TARGET("kings_target"),
    KINGS_SEAL("kings_seal");

    private final String id;

    RoyalPunishmentType(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static RoyalPunishmentType fromId(String id) {
        String normalized = id.toLowerCase(Locale.ROOT).replace('-', '_');
        for (RoyalPunishmentType type : values()) {
            if (type.id.equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
