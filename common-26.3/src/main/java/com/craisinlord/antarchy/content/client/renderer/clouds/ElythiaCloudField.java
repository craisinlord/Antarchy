package com.craisinlord.antarchy.content.client.renderer.clouds;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class ElythiaCloudField {
    private static final double TWO_PI = Math.PI * 2.0D;

    public enum Kind {
        VOXEL,
        WISP
    }

    public enum FeatureKind {
        CYCLONE,
        CURL,
        TWIN,
        WAVE_TRAIN,
        STREET,
        WISP_CYCLONE
    }

    public record Spec(
            String name,
            long seed,
            Kind kind,
            int size,
            int baseSpacing,
            double coverage,
            int maxHeight,
            double spin,
            int cyclones,
            double cycloneMinRadius,
            double cycloneMaxRadius,
            int curls,
            int twins,
            int waveTrains,
            int streets,
            int eddies,
            double featureScale,
            float cellWidth,
            float cellHeight
    ) {
    }

    public record Marker(FeatureKind kind, double x, double z, double radius, double orientation) {
    }

    public static final Spec LOW = new Spec("low", 0x5EA1C10DL, Kind.VOXEL, 256, 16, 0.07D, 2, 1.0D,
            0, 18.0D, 26.0D, 10, 3, 4, 2, 28, 1.0D, 6.0F, 4.0F);
    public static final Spec MID = new Spec("mid", 0x2D7F00A3L, Kind.VOXEL, 256, 16, 0.04D, 3, 1.0D,
            3, 18.0D, 26.0D, 5, 2, 3, 1, 15, 1.0D, 8.0F, 4.0F);
    public static final Spec HIGH = new Spec("high", 0x71C3E9B5L, Kind.WISP, 256, 64, 0.12D, 1, 1.0D,
            1, 60.0D, 75.0D, 0, 0, 0, 0, 0, 1.0D, 8.0F, 4.0F);

    private final Spec spec;
    private final float[] density;
    private final byte[] heights;
    private final List<Marker> markers;

    private ElythiaCloudField(Spec spec, float[] density, byte[] heights, List<Marker> markers) {
        this.spec = spec;
        this.density = density;
        this.heights = heights;
        this.markers = List.copyOf(markers);
    }

    public Spec spec() {
        return this.spec;
    }

    public int size() {
        return this.spec.size();
    }

    public int height(int x, int z) {
        int n = this.spec.size();
        return this.heights[Math.floorMod(z, n) * n + Math.floorMod(x, n)];
    }

    public float density(int x, int z) {
        int n = this.spec.size();
        return this.density[Math.floorMod(z, n) * n + Math.floorMod(x, n)];
    }

    public List<Marker> markers() {
        return this.markers;
    }

    public static ElythiaCloudField generate(Spec spec) {
        Random random = new Random(spec.seed());
        int n = spec.size();
        List<Vortex> vortices = new ArrayList<>();
        List<Mask> masks = new ArrayList<>();
        List<Marker> markers = new ArrayList<>();
        List<double[]> claimed = new ArrayList<>();
        List<CycloneMask> cyclones = new ArrayList<>();

        for (int i = 0; i < spec.cyclones(); i++) {
            double radius = lerp(spec.cycloneMinRadius(), spec.cycloneMaxRadius(), random.nextDouble());
            double[] center = claim(random, n, radius, claimed);
            if (center == null) {
                continue;
            }
            if (spec.kind() == Kind.WISP) {
                vortices.add(new Vortex(center[0], center[1], radius, spec.spin() * (0.5D + random.nextDouble() * 0.15D), true));
                markers.add(new Marker(FeatureKind.WISP_CYCLONE, center[0], center[1], radius, 0.0D));
                continue;
            }
            double turns = spec.spin() * (1.4D + random.nextDouble() * 0.4D);
            int arms = 2 + random.nextInt(2);
            double eye = radius * (0.1D + random.nextDouble() * 0.03D);
            CycloneMask mask = new CycloneMask(center[0], center[1], radius, arms, eye,
                    random.nextDouble() * TWO_PI, random.nextLong());
            vortices.add(new Vortex(center[0], center[1], radius, turns, true));
            masks.add(mask);
            cyclones.add(mask);
            markers.add(new Marker(FeatureKind.CYCLONE, center[0], center[1], radius, 0.0D));
        }

        for (int i = 0; i < spec.curls(); i++) {
            double radius = (11.0D + random.nextDouble() * 5.0D) * spec.featureScale();
            double[] center = claim(random, n, radius * 1.2D, claimed);
            if (center == null) {
                continue;
            }
            double orientation = random.nextDouble() * TWO_PI;
            vortices.add(new Vortex(center[0], center[1], radius, eddySign(spec, random) * (0.8D + random.nextDouble() * 0.3D), false));
            masks.add(new TongueMask(center[0], center[1], orientation, radius, radius * 0.42D));
            markers.add(new Marker(FeatureKind.CURL, center[0], center[1], radius, orientation));
        }

        for (int i = 0; i < spec.twins(); i++) {
            double radius = (9.0D + random.nextDouble() * 4.0D) * spec.featureScale();
            double separation = radius * 1.3D;
            double reach = separation * 0.5D + radius;
            double[] center = claim(random, n, reach * 1.1D, claimed);
            if (center == null) {
                continue;
            }
            double orientation = random.nextDouble() * TWO_PI;
            double ax = Math.cos(orientation) * separation * 0.5D;
            double az = Math.sin(orientation) * separation * 0.5D;
            double turns = 0.9D + random.nextDouble() * 0.2D;
            vortices.add(new Vortex(center[0] + ax, center[1] + az, radius, turns, false));
            vortices.add(new Vortex(center[0] - ax, center[1] - az, radius, -turns, false));
            masks.add(new StripMask(center[0], center[1], orientation, separation * 0.5D + radius * 0.6D, radius * 0.42D, 0.9D));
            markers.add(new Marker(FeatureKind.TWIN, center[0], center[1], reach, orientation));
        }

        for (int i = 0; i < spec.waveTrains(); i++) {
            int count = 4 + random.nextInt(3);
            double radius = (5.0D + random.nextDouble() * 3.0D) * spec.featureScale();
            double spacing = radius * 1.9D;
            double halfLength = (count - 1) * spacing * 0.5D + radius;
            double[] center = claim(random, n, halfLength, claimed);
            if (center == null) {
                continue;
            }
            double orientation = random.nextDouble() * TWO_PI;
            double sign = eddySign(spec, random);
            for (int k = 0; k < count; k++) {
                double along = (k - (count - 1) * 0.5D) * spacing;
                vortices.add(new Vortex(center[0] + Math.cos(orientation) * along, center[1] + Math.sin(orientation) * along,
                        radius, sign * 0.85D, false));
            }
            masks.add(new ShelfMask(center[0], center[1], orientation, halfLength, radius * 1.1D));
            markers.add(new Marker(FeatureKind.WAVE_TRAIN, center[0], center[1], halfLength, orientation));
        }

        for (int i = 0; i < spec.streets(); i++) {
            int pairs = 3;
            double radius = (5.0D + random.nextDouble() * 2.0D) * spec.featureScale();
            double spacing = radius * 2.4D;
            double halfLength = pairs * spacing * 0.5D + radius;
            double[] center = claim(random, n, halfLength, claimed);
            if (center == null) {
                continue;
            }
            double orientation = random.nextDouble() * TWO_PI;
            double cos = Math.cos(orientation);
            double sin = Math.sin(orientation);
            for (int k = 0; k < pairs * 2; k++) {
                double along = (k - (pairs * 2 - 1) * 0.5D) * spacing * 0.5D;
                double side = (k % 2 == 0 ? 1.0D : -1.0D) * radius * 0.8D;
                vortices.add(new Vortex(center[0] + cos * along - sin * side, center[1] + sin * along + cos * side,
                        radius, (k % 2 == 0 ? 0.75D : -0.75D), false));
            }
            masks.add(new StripMask(center[0], center[1], orientation, halfLength, radius * 0.9D, 0.8D));
            markers.add(new Marker(FeatureKind.STREET, center[0], center[1], halfLength, orientation));
        }

        for (int i = 0; i < spec.eddies(); i++) {
            double radius = 8.0D + random.nextDouble() * 5.0D;
            double x = random.nextDouble() * n;
            double z = random.nextDouble() * n;
            if (!isFree(n, x, z, radius, claimed)) {
                continue;
            }
            vortices.add(new Vortex(x, z, radius, eddySign(spec, random) * (0.6D + random.nextDouble() * 0.4D), false));
        }

        float[] background = new float[n * n];
        float[] bases = new float[n * n];
        float[] warpedX = new float[n * n];
        float[] warpedZ = new float[n * n];
        for (int z = 0; z < n; z++) {
            for (int x = 0; x < n; x++) {
                double wx = x + 0.5D;
                double wz = z + 0.5D;
                for (Vortex vortex : vortices) {
                    double[] warped = vortex.warp(wx, wz, n);
                    wx = warped[0];
                    wz = warped[1];
                }
                double base = spec.kind() == Kind.WISP
                        ? wispBase(wx, wz, n, spec.seed())
                        : fbm(wx, wz, spec.baseSpacing(), n, spec.seed());
                int i = z * n + x;
                background[i] = (float) (base + weatherShift(x + 0.5D, z + 0.5D, n, spec));
                bases[i] = (float) base;
                warpedX[i] = (float) wx;
                warpedZ[i] = (float) wz;
            }
        }

        float backgroundThreshold = quantile(background, 1.0D - spec.coverage());
        float[] raw = new float[n * n];
        for (int i = 0; i < raw.length; i++) {
            double value = background[i] - backgroundThreshold + 0.5D;
            for (Mask mask : masks) {
                value = mask.apply(value, warpedX[i], warpedZ[i], bases[i], n);
            }
            raw[i] = (float) value;
        }
        float threshold = 0.5F;
        float[] density = new float[n * n];
        byte[] heights = new byte[n * n];

        if (spec.kind() == Kind.WISP) {
            boolean[] wisps = new boolean[n * n];
            for (int i = 0; i < raw.length; i++) {
                wisps[i] = raw[i] > threshold;
            }
            fillHoles(wisps, n);
            removeSmallComponents(wisps, n, 5);
            for (int i = 0; i < wisps.length; i++) {
                density[i] = wisps[i] ? 1.0F : 0.0F;
                heights[i] = (byte) (wisps[i] ? 1 : 0);
            }
            return new ElythiaCloudField(spec, density, heights, markers);
        }

        boolean[] filled = new boolean[n * n];
        for (int i = 0; i < raw.length; i++) {
            filled[i] = raw[i] > threshold;
        }
        filled = open2x2(filled, n);
        fillHoles(filled, n);
        fillHoles(filled, n);
        removeSmallComponents(filled, n, 8);
        for (CycloneMask cyclone : cyclones) {
            cyclone.clearEye(filled, n);
        }

        int[] distance = distanceToEmpty(filled, n);
        for (int z = 0; z < n; z++) {
            for (int x = 0; x < n; x++) {
                int i = z * n + x;
                if (!filled[i]) {
                    continue;
                }
                double step = 3.0D;
                for (CycloneMask cyclone : cyclones) {
                    if (cyclone.normalizedDistance(x + 0.5D, z + 0.5D, n) < 0.65D) {
                        step = 2.0D;
                    }
                }
                int height = 1 + (int) ((distance[i] - 1) / step);
                heights[i] = (byte) Math.max(1, Math.min(spec.maxHeight(), height));
                density[i] = 1.0F;
            }
        }
        return new ElythiaCloudField(spec, density, heights, markers);
    }

    private static double eddySign(Spec spec, Random random) {
        return random.nextDouble() < 0.7D ? spec.spin() : -spec.spin();
    }

    private static boolean isFree(int n, double x, double z, double radius, List<double[]> claimed) {
        for (double[] other : claimed) {
            double dx = wrap(x - other[0], n);
            double dz = wrap(z - other[1], n);
            double min = radius + other[2];
            if (dx * dx + dz * dz < min * min) {
                return false;
            }
        }
        return true;
    }

    private static double[] claim(Random random, int n, double radius, List<double[]> claimed) {
        for (int attempt = 0; attempt < 200; attempt++) {
            double x = random.nextDouble() * n;
            double z = random.nextDouble() * n;
            boolean free = true;
            for (double[] other : claimed) {
                double dx = wrap(x - other[0], n);
                double dz = wrap(z - other[1], n);
                double min = radius + other[2] + 4.0D;
                if (dx * dx + dz * dz < min * min) {
                    free = false;
                    break;
                }
            }
            if (free) {
                claimed.add(new double[]{x, z, radius});
                return new double[]{x, z};
            }
        }
        return null;
    }

    private static double wispBase(double px, double pz, int n, long seed) {
        double x = px + (valueNoise(px, pz, 64, 64, n, seed ^ 0x2545F491L) - 0.5D) * 28.0D;
        double z = pz + (valueNoise(px, pz, 64, 32, n, seed ^ 0x4F1BBCDCL) - 0.5D) * 40.0D;
        double streak = 0.0D;
        double amplitude = 0.6D;
        double norm = 0.0D;
        int spacingX = 64;
        int spacingZ = 8;
        for (int octave = 0; octave < 3; octave++) {
            streak += amplitude * valueNoise(x, z, spacingX, spacingZ, n, seed + octave * 0x632BE5ABL);
            norm += amplitude;
            amplitude *= 0.5D;
            spacingX /= 2;
            spacingZ = Math.max(2, spacingZ / 2);
        }
        streak /= norm;
        double patch = valueNoise(x, z, 64, 64, n, seed ^ 0x3C6EF372L);
        double dash = valueNoise(x, z, 8, 16, n, seed ^ 0x6A09E667L);
        return streak * (0.8D + 0.4D * patch) * (0.7D + 0.5D * dash);
    }

    private static double weatherShift(double x, double z, int n, Spec spec) {
        long seed = spec.seed() ^ 0x1F83D9ABL;
        double weather = valueNoise(x, z, 64, 64, n, seed) * 0.7D + valueNoise(x, z, 32, 32, n, seed + 1L) * 0.3D;
        double amplitude = spec.kind() == Kind.WISP ? 0.1D : 0.35D;
        return (smooth(0.3D, 0.7D, weather) - 0.5D) * amplitude;
    }

    private static double fbm(double x, double z, int spacing, int n, long seed) {
        double total = 0.0D;
        double amplitude = 0.5D;
        double norm = 0.0D;
        int s = spacing;
        for (int octave = 0; octave < 4 && s >= 2; octave++) {
            total += amplitude * valueNoise(x, z, s, s, n, seed + octave * 0x9E3779B9L);
            norm += amplitude;
            amplitude *= 0.5D;
            s /= 2;
        }
        return total / norm;
    }

    private static double valueNoise(double x, double z, int spacingX, int spacingZ, int n, long seed) {
        int periodX = Math.max(1, n / spacingX);
        int periodZ = Math.max(1, n / spacingZ);
        double gx = x / spacingX;
        double gz = z / spacingZ;
        int x0 = (int) Math.floor(gx);
        int z0 = (int) Math.floor(gz);
        double fx = fade(gx - x0);
        double fz = fade(gz - z0);
        double a = lattice(Math.floorMod(x0, periodX), Math.floorMod(z0, periodZ), seed);
        double b = lattice(Math.floorMod(x0 + 1, periodX), Math.floorMod(z0, periodZ), seed);
        double c = lattice(Math.floorMod(x0, periodX), Math.floorMod(z0 + 1, periodZ), seed);
        double d = lattice(Math.floorMod(x0 + 1, periodX), Math.floorMod(z0 + 1, periodZ), seed);
        return lerp(lerp(a, b, fx), lerp(c, d, fx), fz);
    }

    private static double lattice(int x, int z, long seed) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 11) * 0x1.0p-53;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double smooth(double edge0, double edge1, double value) {
        double t = (value - edge0) / (edge1 - edge0);
        t = Math.max(0.0D, Math.min(1.0D, t));
        return t * t * (3.0D - 2.0D * t);
    }

    private static double wrap(double delta, int n) {
        return delta - n * Math.rint(delta / n);
    }

    private static float quantile(float[] values, double q) {
        float[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int index = (int) Math.max(0, Math.min(sorted.length - 1, Math.round(q * (sorted.length - 1))));
        return sorted[index];
    }

    private static boolean get(boolean[] cells, int n, int x, int z) {
        return cells[Math.floorMod(z, n) * n + Math.floorMod(x, n)];
    }

    private static boolean[] open2x2(boolean[] cells, int n) {
        boolean[] eroded = new boolean[n * n];
        for (int z = 0; z < n; z++) {
            for (int x = 0; x < n; x++) {
                eroded[z * n + x] = get(cells, n, x, z) && get(cells, n, x + 1, z)
                        && get(cells, n, x, z + 1) && get(cells, n, x + 1, z + 1);
            }
        }
        boolean[] opened = new boolean[n * n];
        for (int z = 0; z < n; z++) {
            for (int x = 0; x < n; x++) {
                opened[z * n + x] = get(eroded, n, x, z) || get(eroded, n, x - 1, z)
                        || get(eroded, n, x, z - 1) || get(eroded, n, x - 1, z - 1);
            }
        }
        return opened;
    }

    private static void fillHoles(boolean[] cells, int n) {
        boolean[] copy = Arrays.copyOf(cells, cells.length);
        for (int z = 0; z < n; z++) {
            for (int x = 0; x < n; x++) {
                if (copy[z * n + x]) {
                    continue;
                }
                int neighbours = (get(copy, n, x + 1, z) ? 1 : 0) + (get(copy, n, x - 1, z) ? 1 : 0)
                        + (get(copy, n, x, z + 1) ? 1 : 0) + (get(copy, n, x, z - 1) ? 1 : 0);
                if (neighbours >= 3) {
                    cells[z * n + x] = true;
                }
            }
        }
    }

    private static void removeSmallComponents(boolean[] cells, int n, int minSize) {
        boolean[] visited = new boolean[n * n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        List<Integer> component = new ArrayList<>();
        for (int start = 0; start < cells.length; start++) {
            if (!cells[start] || visited[start]) {
                continue;
            }
            component.clear();
            visited[start] = true;
            queue.add(start);
            while (!queue.isEmpty()) {
                int index = queue.poll();
                component.add(index);
                int x = index % n;
                int z = index / n;
                int[] neighbours = {
                        z * n + Math.floorMod(x + 1, n),
                        z * n + Math.floorMod(x - 1, n),
                        Math.floorMod(z + 1, n) * n + x,
                        Math.floorMod(z - 1, n) * n + x
                };
                for (int next : neighbours) {
                    if (cells[next] && !visited[next]) {
                        visited[next] = true;
                        queue.add(next);
                    }
                }
            }
            if (component.size() < minSize) {
                for (int index : component) {
                    cells[index] = false;
                }
            }
        }
    }

    private static int[] distanceToEmpty(boolean[] cells, int n) {
        int[] distance = new int[n * n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i]) {
                distance[i] = Integer.MAX_VALUE;
            } else {
                distance[i] = 0;
                queue.add(i);
            }
        }
        while (!queue.isEmpty()) {
            int index = queue.poll();
            int x = index % n;
            int z = index / n;
            int[] neighbours = {
                    z * n + Math.floorMod(x + 1, n),
                    z * n + Math.floorMod(x - 1, n),
                    Math.floorMod(z + 1, n) * n + x,
                    Math.floorMod(z - 1, n) * n + x
            };
            for (int next : neighbours) {
                if (distance[next] > distance[index] + 1) {
                    distance[next] = distance[index] + 1;
                    queue.add(next);
                }
            }
        }
        return distance;
    }

    private record Vortex(double x, double z, double radius, double turns, boolean cyclone) {
        private double[] warp(double wx, double wz, int n) {
            double dx = wrap(wx - this.x, n);
            double dz = wrap(wz - this.z, n);
            double u = Math.sqrt(dx * dx + dz * dz) / this.radius;
            if (u >= 1.0D) {
                return new double[]{wx, wz};
            }
            double profile = this.cyclone ? (1.0D - u) * (1.0D - u) : (1.0D - u * u) * (1.0D - u * u);
            double angle = TWO_PI * this.turns * profile;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            return new double[]{this.x + dx * cos - dz * sin, this.z + dx * sin + dz * cos};
        }
    }

    private interface Mask {
        double apply(double value, double wx, double wz, double base, int n);
    }

    private record CycloneMask(double x, double z, double radius, int arms, double eye, double phase, long wobbleSeed) implements Mask {
        @Override
        public double apply(double value, double wx, double wz, double base, int n) {
            double dx = wrap(wx - this.x, n);
            double dz = wrap(wz - this.z, n);
            double r = Math.sqrt(dx * dx + dz * dz);
            double u = r / this.radius;
            if (u >= 1.0D) {
                return value;
            }
            double wobble = (valueNoise(wx, wz, 32, 32, n, this.wobbleSeed) - 0.5D) * 1.2D
                    + (valueNoise(wx, wz, 8, 8, n, this.wobbleSeed ^ 0x5851F42DL) - 0.5D) * 0.6D;
            double theta = Math.atan2(dz, dx) + wobble;
            double edge = (valueNoise(wx, wz, 4, 4, n, this.wobbleSeed ^ 0x14057B7EL) - 0.5D) * 0.3D;
            double band = smooth(0.3D + edge, 0.8D + edge, 0.5D + 0.5D * Math.cos(this.arms * theta + this.phase));
            double armDensity = band * (0.55D + 0.7D * base);
            double wallOffset = (r - this.eye * 1.9D) / (this.eye * 0.7D);
            double wall = Math.exp(-wallOffset * wallOffset) * (0.7D + 0.4D * base);
            double inner = Math.max(armDensity, wall);
            double weight = 1.0D - smooth(0.6D, 1.0D, u);
            return lerp(value, inner, weight) * smooth(this.eye * 0.85D, this.eye * 1.35D, r);
        }

        private double normalizedDistance(double px, double pz, int n) {
            double dx = wrap(px - this.x, n);
            double dz = wrap(pz - this.z, n);
            return Math.sqrt(dx * dx + dz * dz) / this.radius;
        }

        private void clearEye(boolean[] cells, int n) {
            int reach = (int) Math.ceil(this.eye) + 1;
            for (int ix = (int) Math.floor(this.x) - reach; ix <= (int) Math.floor(this.x) + reach; ix++) {
                for (int iz = (int) Math.floor(this.z) - reach; iz <= (int) Math.floor(this.z) + reach; iz++) {
                    double dx = ix + 0.5D - this.x;
                    double dz = iz + 0.5D - this.z;
                    if (dx * dx + dz * dz < this.eye * this.eye) {
                        cells[Math.floorMod(iz, n) * n + Math.floorMod(ix, n)] = false;
                    }
                }
            }
        }
    }

    private record TongueMask(double x, double z, double orientation, double radius, double width) implements Mask {
        @Override
        public double apply(double value, double wx, double wz, double base, int n) {
            double dx = wrap(wx - this.x, n);
            double dz = wrap(wz - this.z, n);
            double r = Math.sqrt(dx * dx + dz * dz);
            if (r >= this.radius) {
                return value;
            }
            double cos = Math.cos(this.orientation);
            double sin = Math.sin(this.orientation);
            double a = dx * cos + dz * sin;
            double b = -dx * sin + dz * cos;
            double window = 1.0D - smooth(0.55D, 1.0D, r / this.radius);
            double tongue = (1.0D - smooth(this.width * 0.5D, this.width, Math.abs(b)))
                    * smooth(-this.width * 0.3D, this.width * 0.6D, a);
            return lerp(value, tongue * (0.7D + 0.5D * base), window * 0.9D);
        }
    }

    private record StripMask(double x, double z, double orientation, double halfLength, double width, double strength) implements Mask {
        @Override
        public double apply(double value, double wx, double wz, double base, int n) {
            double dx = wrap(wx - this.x, n);
            double dz = wrap(wz - this.z, n);
            double cos = Math.cos(this.orientation);
            double sin = Math.sin(this.orientation);
            double a = dx * cos + dz * sin;
            double b = -dx * sin + dz * cos;
            double window = (1.0D - smooth(this.halfLength * 0.85D, this.halfLength * 1.2D, Math.abs(a)))
                    * (1.0D - smooth(this.width * 2.0D, this.width * 3.5D, Math.abs(b)));
            if (window <= 0.0D) {
                return value;
            }
            double strip = (1.0D - smooth(this.width * 0.5D, this.width, Math.abs(b)))
                    * (1.0D - smooth(this.halfLength * 0.75D, this.halfLength, Math.abs(a)));
            return lerp(value, strip * (0.7D + 0.5D * base), window * this.strength);
        }
    }

    private record ShelfMask(double x, double z, double orientation, double halfLength, double width) implements Mask {
        @Override
        public double apply(double value, double wx, double wz, double base, int n) {
            double dx = wrap(wx - this.x, n);
            double dz = wrap(wz - this.z, n);
            double cos = Math.cos(this.orientation);
            double sin = Math.sin(this.orientation);
            double a = dx * cos + dz * sin;
            double b = -dx * sin + dz * cos;
            double window = (1.0D - smooth(this.halfLength * 0.85D, this.halfLength * 1.1D, Math.abs(a)))
                    * (1.0D - smooth(this.width * 1.5D, this.width * 2.5D, Math.abs(b)));
            if (window <= 0.0D) {
                return value;
            }
            double shelf = smooth(this.width * 0.5D, -this.width * 0.5D, b);
            return lerp(value, shelf * (0.65D + 0.5D * base), window * 0.85D);
        }
    }
}
