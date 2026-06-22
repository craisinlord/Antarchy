package com.craisinlord.antarchy.content.gravity;

import java.util.Optional;

public enum AntarchyGravityDirection {
    DOWN(0, "down", false),
    UP(1, "up", true);

    private final int id;
    private final String serializedName;
    private final boolean inverted;

    AntarchyGravityDirection(int id, String serializedName, boolean inverted) {
        this.id = id;
        this.serializedName = serializedName;
        this.inverted = inverted;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public boolean isInverted() {
        return this.inverted;
    }

    public static AntarchyGravityDirection byId(int id) {
        return id == UP.id ? UP : DOWN;
    }

    public static Optional<AntarchyGravityDirection> parse(String value) {
        for (AntarchyGravityDirection direction : values()) {
            if (direction.serializedName.equalsIgnoreCase(value)) {
                return Optional.of(direction);
            }
        }

        return Optional.empty();
    }
}
