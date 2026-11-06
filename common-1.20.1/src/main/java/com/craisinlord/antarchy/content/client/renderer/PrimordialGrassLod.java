package com.craisinlord.antarchy.content.client.renderer;

final class PrimordialGrassLod {
    static final int HASH_SHIFT = 16;
    static final double HYSTERESIS = 8.0D;
    static final int MAX_BANDS = 8;
    static final int MAX_FACES = 2;
    private static final int[] SHAPE_BANDS = {1, 3, 6, 8};
    private static final double ANY = Double.POSITIVE_INFINITY;
    private static final Tier[][] QUALITIES = {
            {
                    new Tier(12.0D, 3, 1.0F, 2),
                    new Tier(32.0D, 1, 0.75F, 2),
                    new Tier(64.0D, 1, 0.4F, 2),
                    new Tier(ANY, 1, 0.15F, 1)
            },
            {
                    new Tier(16.0D, 6, 1.0F, 2),
                    new Tier(32.0D, 3, 1.0F, 2),
                    new Tier(64.0D, 1, 0.75F, 2),
                    new Tier(96.0D, 1, 0.5F, 2),
                    new Tier(128.0D, 1, 0.35F, 1),
                    new Tier(ANY, 1, 0.2F, 1)
            },
            {
                    new Tier(32.0D, 8, 1.0F, 2),
                    new Tier(64.0D, 3, 1.0F, 2),
                    new Tier(128.0D, 1, 0.667F, 2),
                    new Tier(ANY, 1, 0.667F, 1)
            }
    };
    private static final int[][] SHAPE_FIRST = new int[MAX_BANDS + 1][MAX_FACES + 1];
    private static final int SHAPE_VERTICES;

    static {
        int first = 0;
        for (int bands : SHAPE_BANDS) {
            for (int faces = 1; faces <= MAX_FACES; faces++) {
                SHAPE_FIRST[bands][faces] = first;
                first += stripVertices(bands, faces);
            }
        }
        SHAPE_VERTICES = first;
    }

    private PrimordialGrassLod() {
    }

    record Tier(double maxDistance, int bands, float keep, int faces) {
        int first() {
            return SHAPE_FIRST[bands][faces];
        }

        int vertices() {
            return stripVertices(bands, faces);
        }
    }

    static Tier[] tiers(int quality) {
        return QUALITIES[Math.max(0, Math.min(QUALITIES.length - 1, quality))];
    }

    static int tierFor(Tier[] tiers, int current, double distance) {
        for (int i = 0; i < tiers.length - 1; i++) {
            double limit = tiers[i].maxDistance + (i == current ? HYSTERESIS : 0.0D);
            if (distance < limit) return i;
        }
        return tiers.length - 1;
    }

    static int stripVertices(int bands, int faces) {
        return (bands + 1) * 2 * faces + 2 * (faces - 1);
    }

    static int[] shapeBands() {
        return SHAPE_BANDS.clone();
    }

    static int shapeVertices() {
        return SHAPE_VERTICES;
    }

    static int visibleCount(int[] packed, int count, float keep) {
        int limit = Math.round(keep * 256.0F) << HASH_SHIFT;
        int low = 0;
        int high = count;
        while (low < high) {
            int middle = low + high >>> 1;
            if (packed[middle] < limit) low = middle + 1;
            else high = middle;
        }
        return low;
    }
}
