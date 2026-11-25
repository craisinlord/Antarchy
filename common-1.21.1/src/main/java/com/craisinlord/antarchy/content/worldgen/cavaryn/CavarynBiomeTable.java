package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesIntegration;
import java.util.List;

public final class CavarynBiomeTable {
    public static final String CAVARYN = "antarchy:cavaryn";
    public static final String GLOWCAP_CAVES = "antarchy:glowcap_caves";
    public static final String BIOWART_CAVES = "antarchy:biowart_caves";
    public static final String MOLEWORM_CAVES = "antarchy:cavaryn_moleworm_caves";
    public static final String WORMY_CAVES = "antarchy:wormy_caves";
    public static final String BROODING_TUNNELS = "antarchy:brooding_tunnels";
    public static final String TOXIC_CAVES = "antarchy:toxic_caves";
    public static final String FORLORN_HOLLOWS = "antarchy:forlorn_hollows";
    public static final double TOXIC_MAX_HUMIDITY = -0.2D;
    public static final double FORLORN_MIN_HUMIDITY = -0.06D;
    public static final double FORLORN_MAX_HUMIDITY = 0.14D;
    public static final double FORLORN_MIN_WEIRDNESS = -0.7D;
    public static final double FORLORN_MAX_WEIRDNESS = 0.95D;

    public static final List<String> BIOMES = List.of(CAVARYN, GLOWCAP_CAVES, BIOWART_CAVES, MOLEWORM_CAVES, WORMY_CAVES, BROODING_TUNNELS);

    public static final List<Entry> ENTRIES = List.of(
            new Entry(CAVARYN, -1.0D, 0.05D, -1.0D, 1.0D, -2.0D, -0.2D),
            new Entry(GLOWCAP_CAVES, 0.25D, 1.0D, -1.0D, 0.25D, -2.0D, -0.2D),
            new Entry(MOLEWORM_CAVES, 0.05D, 0.25D, -1.0D, 0.25D, -2.0D, -0.2D),
            new Entry(BIOWART_CAVES, 0.05D, 1.0D, 0.25D, 1.0D, -2.0D, -0.2D),
            new Entry(WORMY_CAVES, 0.14D, 1.0D, -0.7D, 0.95D, -0.2D, 2.0D),
            new Entry(BROODING_TUNNELS, -1.0D, 0.14D, -1.0D, 1.0D, -0.2D, 2.0D),
            new Entry(BROODING_TUNNELS, 0.14D, 1.0D, -1.0D, -0.7D, -0.2D, 2.0D),
            new Entry(BROODING_TUNNELS, 0.14D, 1.0D, 0.95D, 1.0D, -0.2D, 2.0D)
    );

    private CavarynBiomeTable() {
    }

    public static String winner(double humidity, double weirdness, double depth) {
        String best = baseWinner(humidity, weirdness, depth);
        return AlexsCavesIntegration.enabled() ? overlay(best, humidity, weirdness) : best;
    }

    public static String overlay(String base, double humidity, double weirdness) {
        if (CAVARYN.equals(base) && inToxic(humidity)) {
            return TOXIC_CAVES;
        }
        if (BROODING_TUNNELS.equals(base) && inForlorn(humidity, weirdness)) {
            return FORLORN_HOLLOWS;
        }
        return base;
    }

    private static boolean inToxic(double humidity) {
        return humidity <= TOXIC_MAX_HUMIDITY;
    }

    private static boolean inForlorn(double humidity, double weirdness) {
        return humidity >= FORLORN_MIN_HUMIDITY && humidity <= FORLORN_MAX_HUMIDITY
                && weirdness >= FORLORN_MIN_WEIRDNESS && weirdness <= FORLORN_MAX_WEIRDNESS;
    }

    private static boolean isOverlay(String biome) {
        return TOXIC_CAVES.equals(biome) || FORLORN_HOLLOWS.equals(biome);
    }

    private static String baseWinner(double humidity, double weirdness, double depth) {
        String best = null;
        double bestFitness = Double.MAX_VALUE;
        for (Entry entry : ENTRIES) {
            double fitness = entry.fitness(humidity, weirdness, depth);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                best = entry.biome();
            }
        }
        return best;
    }

    public static boolean isAmongBest(String biome, double humidity, double weirdness, double depth) {
        if (isOverlay(biome)) {
            return biome.equals(winner(humidity, weirdness, depth));
        }
        if (AlexsCavesIntegration.enabled() && isOverlay(winner(humidity, weirdness, depth))) {
            return false;
        }
        double own = Double.MAX_VALUE;
        double other = Double.MAX_VALUE;
        for (Entry entry : ENTRIES) {
            double fitness = entry.fitness(humidity, weirdness, depth);
            if (entry.biome().equals(biome)) {
                own = Math.min(own, fitness);
            } else {
                other = Math.min(other, fitness);
            }
        }
        return own <= other;
    }

    public static boolean isOnly(String biome, double humidity, double weirdness, double depth) {
        if (isOverlay(biome)) {
            return biome.equals(winner(humidity, weirdness, depth));
        }
        if (AlexsCavesIntegration.enabled() && isOverlay(winner(humidity, weirdness, depth))) {
            return false;
        }
        double own = Double.MAX_VALUE;
        double other = Double.MAX_VALUE;
        for (Entry entry : ENTRIES) {
            double fitness = entry.fitness(humidity, weirdness, depth);
            if (entry.biome().equals(biome)) {
                own = Math.min(own, fitness);
            } else {
                other = Math.min(other, fitness);
            }
        }
        return own < other;
    }

    public static boolean excludedByClimate(String biome, double humidity, double weirdness) {
        if (TOXIC_CAVES.equals(biome)) {
            return !AlexsCavesIntegration.enabled() || !inToxic(humidity);
        }
        if (FORLORN_HOLLOWS.equals(biome)) {
            return !AlexsCavesIntegration.enabled() || !inForlorn(humidity, weirdness);
        }
        for (Entry own : ENTRIES) {
            if (own.biome().equals(biome) && !beatenInDepthBand(own, biome, humidity, weirdness)) {
                return false;
            }
        }
        return true;
    }

    private static boolean beatenInDepthBand(Entry own, String biome, double humidity, double weirdness) {
        double ownFitness = own.climateFitness(humidity, weirdness);
        for (Entry other : ENTRIES) {
            if (!other.biome().equals(biome) && other.sameDepthBand(own) && other.climateFitness(humidity, weirdness) < ownFitness) {
                return true;
            }
        }
        return false;
    }

    public record Entry(String biome, double humidityMin, double humidityMax, double weirdnessMin, double weirdnessMax, double depthMin, double depthMax) {
        double fitness(double humidity, double weirdness, double depth) {
            double d = distance(depth, this.depthMin, this.depthMax);
            return this.climateFitness(humidity, weirdness) + d * d;
        }

        double climateFitness(double humidity, double weirdness) {
            double h = distance(humidity, this.humidityMin, this.humidityMax);
            double w = distance(weirdness, this.weirdnessMin, this.weirdnessMax);
            return h * h + w * w;
        }

        boolean sameDepthBand(Entry other) {
            return this.depthMin == other.depthMin && this.depthMax == other.depthMax;
        }

        private static double distance(double value, double min, double max) {
            if (value < min) {
                return min - value;
            }
            if (value > max) {
                return value - max;
            }
            return 0.0D;
        }
    }
}
