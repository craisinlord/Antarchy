package com.craisinlord.antarchy.content.weather;

import java.util.Optional;

public enum ThoraxisWeatherKind {
    NONE("none"),
    INVERSION_STORM("inversion_storm"),
    BLOOD_RAIN("blood_rain"),
    SHARKNADO("sharknado"),
    SANDSTORM("sandstorm");

    private final String id;

    ThoraxisWeatherKind(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public boolean isActive() {
        return this != NONE;
    }

    public static ThoraxisWeatherKind byId(String value) {
        if (value == null) {
            return NONE;
        }

        for (ThoraxisWeatherKind kind : values()) {
            if (kind.id.equalsIgnoreCase(value)) {
                return kind;
            }
        }

        return NONE;
    }

    public static Optional<ThoraxisWeatherKind> parse(String value) {
        ThoraxisWeatherKind kind = byId(value);
        return kind == NONE ? Optional.empty() : Optional.of(kind);
    }
}
