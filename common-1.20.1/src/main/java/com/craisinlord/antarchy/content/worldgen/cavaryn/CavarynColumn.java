package com.craisinlord.antarchy.content.worldgen.cavaryn;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

interface CavarynColumn {
    double SCALE = 0.35D;
    double FLOOR = -1.0D / SCALE - 1.0E-9D;

    void load(int qx, int qz);

    boolean isEmpty();

    double minY();

    double maxY();

    double signedDistance(int x, int y, int z, double floor);

    static double density(double distance) {
        return Mth.clamp(distance * SCALE, -1.0D, 1.0D);
    }

    interface Source {
        boolean bound();

        @Nullable
        CavarynColumn newColumn();
    }
}
