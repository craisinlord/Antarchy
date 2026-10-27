package com.craisinlord.antarchy.content.network;

public enum TransitStyle {
    ELYTHIA_IN,
    ELYTHIA_HOME,
    THORAXIS_IN,
    THORAXIS_OUT,
    CAVARYN_IN,
    CAVARYN_OUT,
    PRISM_IN,
    PRISM_OUT;

    private static final TransitStyle[] VALUES = values();

    public static TransitStyle byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : ELYTHIA_IN;
    }

    public boolean elythia() {
        return this == ELYTHIA_IN || this == ELYTHIA_HOME;
    }

    public boolean thoraxis() {
        return this == THORAXIS_IN || this == THORAXIS_OUT;
    }

    public boolean cavaryn() {
        return this == CAVARYN_IN || this == CAVARYN_OUT;
    }

    public boolean prism() {
        return this == PRISM_IN || this == PRISM_OUT;
    }
}
