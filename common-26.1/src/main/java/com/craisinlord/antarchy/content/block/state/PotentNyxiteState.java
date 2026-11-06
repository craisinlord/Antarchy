package com.craisinlord.antarchy.content.block.state;

import net.minecraft.util.StringRepresentable;

public enum PotentNyxiteState implements StringRepresentable {
    DRY("dry"),
    WET("wet"),
    DORMANT("dormant"),
    ERUPTING("erupting"),
    CONTINUOUS("continuous");

    private final String serializedName;

    PotentNyxiteState(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
