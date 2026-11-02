package com.craisinlord.antarchy.content.weather;

import java.util.Locale;

public enum ThoraxisEvent {
    NONE(Scope.ANYWHERE, 0),
    WEIGHTLESS(Scope.ANYWHERE, 20),
    TEMPORAL_BUBBLES(Scope.ANYWHERE, 20),
    UPWARD_RAIN(Scope.ANYWHERE, 20),
    BLIND_SKY(Scope.UNDERSIDE, 15);

    public enum Scope { ANYWHERE, UNDERSIDE }

    public enum Phase { NONE, WARNING, ACTIVE }

    private static final ThoraxisEvent[] VALUES = values();

    private final Scope scope;
    private final int weight;

    ThoraxisEvent(Scope scope, int weight) {
        this.scope = scope;
        this.weight = weight;
    }

    public Scope scope() {
        return this.scope;
    }

    public int weight() {
        return this.weight;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static ThoraxisEvent byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : NONE;
    }

    public static ThoraxisEvent byId(String id) {
        for (ThoraxisEvent event : VALUES) {
            if (event.id().equals(id)) {
                return event;
            }
        }
        return NONE;
    }
}
