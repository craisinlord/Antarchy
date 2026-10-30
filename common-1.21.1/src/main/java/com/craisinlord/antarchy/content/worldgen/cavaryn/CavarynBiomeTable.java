package com.craisinlord.antarchy.content.worldgen.cavaryn;

import java.util.List;

public final class CavarynBiomeTable {
    public static final String CAVARYN = "antarchy:cavaryn";
    public static final String GLOWCAP_CAVES = "antarchy:glowcap_caves";
    public static final String BIOWART_CAVES = "antarchy:biowart_caves";
    public static final String MOLEWORM_CAVES = "antarchy:cavaryn_moleworm_caves";
    public static final String WORMY_CAVES = "antarchy:wormy_caves";
    public static final String BROODING_TUNNELS = "antarchy:brooding_tunnels";

    public static final List<String> BIOMES = List.of(CAVARYN, GLOWCAP_CAVES, BIOWART_CAVES, MOLEWORM_CAVES, WORMY_CAVES, BROODING_TUNNELS);

    public static final List<Entry> ENTRIES = List.of(
            new Entry(CAVARYN, -1.0, 0.05, -1.0, 1.0, -2.0, -0.2),
            new Entry(GLOWCAP_CAVES, 0.25, 1.0, -1.0, 0.25, -2.0, -0.2),
            new Entry(MOLEWORM_CAVES, 0.05, 0.25, -1.0, 0.25, -2.0, -0.2),
            new Entry(BIOWART_CAVES, 0.05, 1.0, 0.25, 1.0, -2.0, -0.2),
            new Entry(WORMY_CAVES, 0.14, 1.0, -0.7, 0.95, -0.2, 2.0),
            new Entry(BROODING_TUNNELS, -1.0, 0.14, -1.0, 1.0, -0.2, 2.0),
            new Entry(BROODING_TUNNELS, 0.14, 1.0, -1.0, -0.7, -0.2, 2.0),
            new Entry(BROODING_TUNNELS, 0.14, 1.0, 0.95, 1.0, -0.2, 2.0)
    );

    private CavarynBiomeTable() {
    }

    public static String winner(double humidity, double weirdness, double depth) {
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

    public record Entry(String biome, double humidityMin, double humidityMax, double weirdnessMin, double weirdnessMax, double depthMin, double depthMax) {
        double fitness(double humidity, double weirdness, double depth) {
            double h = distance(humidity, this.humidityMin, this.humidityMax);
            double w = distance(weirdness, this.weirdnessMin, this.weirdnessMax);
            double d = distance(depth, this.depthMin, this.depthMax);
            return h * h + w * w + d * d;
        }

        private static double distance(double value, double min, double max) {
            if (value < min) {
                return min - value;
            }
            if (value > max) {
                return value - max;
            }
            return 0.0;
        }
    }
}
