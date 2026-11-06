package com.craisinlord.antarchy.content.client;

public final class PrimordialGrassWind {
    public static final double CALM_PERIOD = 192.0D;
    public static final double GUST_INTERVAL = 800.0D;
    public static final double FRONT_SPEED = 0.31D;
    public static final int GUST_TABLE_ENTRIES = 8;
    public static final int GUST_TABLE_CENTER = 4;
    private static final double TWO_PI = Math.PI * 2.0D;

    private PrimordialGrassWind() {
    }

    public static Sample sample(double ticks, double x, double z) {
        double angle = ElythiaWind.angle(ticks);
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        double phase = x * 0.07D + z * 0.09D;
        double calm = Math.sin(ticks * Math.PI * 2.0D / CALM_PERIOD + phase) * 0.04D;
        double gust = gust(ticks, x, z, dx, dz);
        return new Sample(dx, dz, calm, gust);
    }

    public static double gust(double ticks, double x, double z, double dx, double dz) {
        double local = ticks - (x * dx + z * dz) / FRONT_SPEED;
        long cell = (long) Math.floor(local / GUST_INTERVAL);
        double start = cell * GUST_INTERVAL + hash(cell) * 160.0D;
        double strength = 0.65D + hash(cell + 417L) * 0.35D;
        return envelope(local - start) * strength;
    }

    public static ShaderFrame shaderFrame(double ticks, double cameraX, double cameraZ, float[] gustTable) {
        double angle = ElythiaWind.angle(ticks);
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        double localBase = ticks - (cameraX * dx + cameraZ * dz) / FRONT_SPEED;
        long baseCell = (long) Math.floor(localBase / GUST_INTERVAL);
        float gustLocal = (float) (localBase - baseCell * GUST_INTERVAL);
        for (int i = 0; i < GUST_TABLE_ENTRIES; i++) {
            long cell = baseCell + i - GUST_TABLE_CENTER;
            gustTable[i * 2] = (float) (hash(cell) * 160.0D);
            gustTable[i * 2 + 1] = (float) (0.65D + hash(cell + 417L) * 0.35D);
        }
        double phase = ticks * TWO_PI / CALM_PERIOD + cameraX * 0.07D + cameraZ * 0.09D;
        float calmPhase = (float) (phase - Math.floor(phase / TWO_PI) * TWO_PI);
        if (calmPhase >= (float) TWO_PI) {
            calmPhase = 0.0F;
        }
        return new ShaderFrame((float) dx, (float) dz, gustLocal, calmPhase);
    }

    public static double shaderGust(ShaderFrame frame, float[] gustTable, double relX, double relZ) {
        double local = frame.gustLocal() - (relX * frame.directionX() + relZ * frame.directionZ()) / FRONT_SPEED;
        double idx = Math.floor(local / GUST_INTERVAL);
        int index = (int) Math.clamp(idx + GUST_TABLE_CENTER, 0.0D, GUST_TABLE_ENTRIES - 1.0D);
        double age = local - idx * GUST_INTERVAL - gustTable[index * 2];
        return envelope(age) * gustTable[index * 2 + 1];
    }

    public static double envelope(double age) {
        if (age < 0.0D || age >= 136.0D) return 0.0D;
        if (age < 25.0D) return smooth(age / 25.0D);
        if (age < 54.0D) return 1.0D;
        return 1.0D - smooth((age - 54.0D) / 82.0D);
    }

    public static float gustLoudness(double gust) {
        return 0.03F + 0.75F * (float) Math.pow(Math.clamp(gust, 0.0D, 1.0D), 1.6D);
    }

    public static double flexibility(double height) {
        double h = Math.clamp(height, 0.0D, 1.0D);
        return (1.7D * (1.0D - Math.exp(-h * 9.0D)) + 3.3D * Math.pow(h, 1.9D))
                / (1.7D * (1.0D - Math.exp(-9.0D)) + 3.3D);
    }

    private static double smooth(double t) {
        t = Math.clamp(t, 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    private static double hash(long n) {
        n = (n ^ (n >>> 30)) * 0xBF58476D1CE4E5B9L;
        n = (n ^ (n >>> 27)) * 0x94D049BB133111EBL;
        return ((n ^ (n >>> 31)) >>> 11) * 0x1.0p-53;
    }

    public record ShaderFrame(float directionX, float directionZ, float gustLocal, float calmPhase) {
    }

    public record Sample(double directionX, double directionZ, double calm, double gust) {
        public double displacement(double height, double exposure) {
            return calm * height * height + gust * flexibility(height) * exposure * 0.34D;
        }
    }
}
