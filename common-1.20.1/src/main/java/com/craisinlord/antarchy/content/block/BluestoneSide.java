package com.craisinlord.antarchy.content.block;

import net.minecraft.util.StringRepresentable;

public enum BluestoneSide implements StringRepresentable {
    NONE("none"),
    SIDE("side"),
    DOWN("down");

    private final String name;

    BluestoneSide(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
