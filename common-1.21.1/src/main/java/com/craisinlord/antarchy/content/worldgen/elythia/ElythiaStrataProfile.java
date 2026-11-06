package com.craisinlord.antarchy.content.worldgen.elythia;

import java.util.Map;
import java.util.Optional;

public record ElythiaStrataProfile(
        int bandSpacing,
        int minZebra,
        int maxZebra,
        int partingEvery,
        double warpAmplitude,
        double warpWavelength,
        double foldAmplitude,
        double foldWavelength,
        double foldThreshold,
        int summitStart,
        int summitEnd,
        int minCap,
        int maxCap,
        double veinThreshold
) {
    public static final double NO_VEINS = 2.0D;
    public static final ElythiaStrataProfile PRIMORDIAL_MASSIF = new ElythiaStrataProfile(13, 2, 3, 3, 3.0D, 96.0D, 28.0D, 260.0D, 0.2D, 236, 290, 0, 0, 0.5D);
    public static final ElythiaStrataProfile SHELFBACK_HIGHLANDS = new ElythiaStrataProfile(11, 1, 3, 0, 5.0D, 72.0D, 0.0D, 260.0D, 0.0D, 0, 0, 7, 11, 0.6D);
    public static final ElythiaStrataProfile ZEBRA_SCARPS = new ElythiaStrataProfile(10, 5, 7, 0, 3.0D, 96.0D, 45.0D, 260.0D, -2.0D, 0, 0, 0, 0, NO_VEINS);
    public static final ElythiaStrataProfile GLIMMERING_FALLS = new ElythiaStrataProfile(14, 1, 2, 0, 6.0D, 64.0D, 0.0D, 260.0D, 0.0D, 0, 0, 0, 0, NO_VEINS);
    public static final ElythiaStrataProfile FORMATION = new ElythiaStrataProfile(9, 3, 3, 0, 3.0D, 96.0D, 45.0D, 260.0D, -2.0D, 0, 0, 0, 0, NO_VEINS);

    private static final Map<String, ElythiaStrataProfile> BY_BIOME = Map.of(
            "antarchy:primordial_massif", PRIMORDIAL_MASSIF,
            "antarchy:shelfback_highlands", SHELFBACK_HIGHLANDS,
            "antarchy:zebra_scarps", ZEBRA_SCARPS,
            "antarchy:glimmering_falls", GLIMMERING_FALLS
    );

    public ElythiaStrataProfile {
        if (minZebra < 1 || maxZebra < minZebra || maxZebra + 1 > bandSpacing) {
            throw new IllegalArgumentException("Strata bands must fit inside their spacing");
        }
        if (warpWavelength <= 0.0D || foldWavelength <= 0.0D) {
            throw new IllegalArgumentException("Strata wavelengths must be positive");
        }
        if (summitEnd < summitStart || maxCap < minCap) {
            throw new IllegalArgumentException("Strata ranges must be ordered");
        }
    }

    public static Optional<ElythiaStrataProfile> forBiome(String id) {
        return Optional.ofNullable(BY_BIOME.get(id));
    }

    public static Map<String, ElythiaStrataProfile> all() {
        return BY_BIOME;
    }

    public boolean hasSummit() {
        return this.summitEnd > this.summitStart;
    }

    public boolean hasCap() {
        return this.maxCap > 0;
    }
}
