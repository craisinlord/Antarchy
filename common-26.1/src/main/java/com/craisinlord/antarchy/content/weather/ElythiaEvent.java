package com.craisinlord.antarchy.content.weather;

import java.util.Locale;

public enum ElythiaEvent {
    NONE(Window.NONE, 0),
    AURORA(Window.NIGHT, 26),
    GRAND_AURORA(Window.NIGHT, 8),
    PRISM_AURORA(Window.NIGHT, 4),
    METEOR_SHOWER(Window.NIGHT, 12),
    STARFALL(Window.NIGHT, 4),
    DEER_MIGRATION(Window.DAY, 10),
    TOWERING_CLOUDS(Window.DAY, 6),
    LUMEN_RAIN(Window.RAIN, 25);

    public enum Window { NONE, NIGHT, DAY, RAIN }

    private static final ElythiaEvent[] VALUES = values();

    private final Window window;
    private final int chance;

    ElythiaEvent(Window window, int chance) {
        this.window = window;
        this.chance = chance;
    }

    public Window window() {
        return this.window;
    }

    public int chance() {
        return this.chance;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public boolean isAurora() {
        return this == AURORA || this == GRAND_AURORA || this == PRISM_AURORA;
    }

    public static ElythiaEvent byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : NONE;
    }

    public static ElythiaEvent byId(String id) {
        for (ElythiaEvent event : VALUES) {
            if (event.id().equals(id)) {
                return event;
            }
        }
        return NONE;
    }
}
