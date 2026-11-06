package com.craisinlord.antarchy.content.worldgen.elythia;

public final class ElythiaStrata {
    public static final int SHELL_DEPTH = 8;
    public static final int EXPOSURE_RADIUS = 4;
    public static final int BIOME_JITTER = 8;
    public static final int TIDE_RADIUS = 6;
    public static final int MIN_TIDE = 3;
    public static final int MAX_TIDE = 8;
    public static final int MAX_LIP_RUN = 20;
    public static final int MAX_LIP_GAP = 40;
    public static final int TIDE_BELOW = 2;
    private static final int MAX_BAND_JITTER = 4;
    private static final double FOLD_MASK_WAVELENGTH = 640.0D;
    private static final double FOLD_MASK_RAMP = 0.25D;
    private static final double CAP_WAVELENGTH = 48.0D;
    private static final double VEIN_REGION_WAVELENGTH = 420.0D;
    private static final double PEACH_VEIN_BONUS = 0.45D;
    private static final double VEIN_SLOPE_X = 0.62D;
    private static final double VEIN_SLOPE_Z = -0.41D;
    private static final double VEIN_SPACING = 34.0D;
    private static final double VEIN_HALF_WIDTH = 0.8D;
    private static final double VEIN_SHEET_CHANCE = 0.6D;
    private static final double TIDE_WAVELENGTH = 24.0D;
    private static final double TIDE_FLECK_CHANCE = 0.12D;
    private static final long CAP_SALT = 0x38D5A1F07C926E4BL;
    private static final long VEIN_REGION_SALT = 0x19B7E46D2A8F03C5L;
    private static final long VEIN_WARP_SALT = 0x62C40F9E7B13A8D6L;
    private static final long VEIN_SHEET_SALT = 0x0E95D7B3C1A64F28L;
    private static final long TIDE_SALT = 0x47A2F8C60D1B93E5L;
    private static final long TIDE_FLECK_SALT = 0x2B81D6F3A95E074CL;
    private static final long WARP_SALT = 0x2C9277B5A3F1D04EL;
    private static final long FOLD_SALT = 0x51E7A9C3D86B1F27L;
    private static final long MASK_SALT = 0x7A3D19E5C0B4F862L;
    private static final long BAND_SALT = 0x1F86D3A92B7E5C04L;
    private static final long JITTER_X_SALT = 0x4B0E92D7A6C3158FL;
    private static final long JITTER_Z_SALT = 0x6D2F81B5E47C09A3L;

    public enum Layer {
        BASE,
        ZEBRA,
        CONGLOMERATE,
        ARKROSE,
        TIDESTONE
    }

    private ElythiaStrata() {
    }

    public static double bedOffset(long seed, ElythiaStrataProfile profile, int x, int z) {
        return warp(seed, profile, x, z) + foldMask(seed, profile, x, z) * profile.foldAmplitude() * fold(seed, profile, x, z);
    }

    public static Layer layerAt(long seed, ElythiaStrataProfile profile, int y, double bedOffset) {
        return layerAtBed(seed, profile, (int) Math.floor(y + bedOffset), summitProgress(profile, y));
    }

    public static Layer layerAtBed(long seed, ElythiaStrataProfile profile, int bed) {
        return layerAtBed(seed, profile, bed, 0.0D);
    }

    public static double summitProgress(ElythiaStrataProfile profile, int y) {
        if (!profile.hasSummit()) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, (y - profile.summitStart()) / (double) (profile.summitEnd() - profile.summitStart())));
    }

    public static int capDepth(long seed, ElythiaStrataProfile profile, int x, int z) {
        if (!profile.hasCap()) {
            return 0;
        }
        double noise = valueNoise(seed ^ CAP_SALT, x / CAP_WAVELENGTH, z / CAP_WAVELENGTH);
        return profile.minCap() + (int) Math.round((noise + 1.0D) * 0.5D * (profile.maxCap() - profile.minCap()));
    }

    public static Layer capLayer(int depth, int capDepth) {
        if (capDepth <= 0 || depth > capDepth + 1) {
            return Layer.BASE;
        }
        return depth < capDepth ? Layer.ZEBRA : Layer.CONGLOMERATE;
    }

    public static boolean veinRegion(long seed, ElythiaStrataProfile profile, int x, int z, boolean nearPeach) {
        if (profile.veinThreshold() >= ElythiaStrataProfile.NO_VEINS) {
            return false;
        }
        double threshold = profile.veinThreshold() - (nearPeach ? PEACH_VEIN_BONUS : 0.0D);
        return valueNoise(seed ^ VEIN_REGION_SALT, x / VEIN_REGION_WAVELENGTH, z / VEIN_REGION_WAVELENGTH) > threshold;
    }

    public static boolean inVein(long seed, int x, int y, int z) {
        double wobble = 6.0D * valueNoise(seed ^ VEIN_WARP_SALT, x / 28.0D, z / 28.0D);
        double position = (y * 0.55D + x * VEIN_SLOPE_X + z * VEIN_SLOPE_Z + wobble) / VEIN_SPACING;
        int sheet = (int) Math.floor(position + 0.5D);
        if (unit(hash(seed ^ VEIN_SHEET_SALT, sheet, 0)) > VEIN_SHEET_CHANCE) {
            return false;
        }
        return Math.abs(position - sheet) * VEIN_SPACING < VEIN_HALF_WIDTH;
    }

    public static boolean isTide(long seed, int x, int y, int z, int waterline) {
        int above = y - waterline;
        if (above < -TIDE_BELOW) {
            return false;
        }
        double noise = valueNoise(seed ^ TIDE_SALT, x / TIDE_WAVELENGTH, z / TIDE_WAVELENGTH);
        return above <= MIN_TIDE + (int) Math.round((noise + 1.0D) * 0.5D * (MAX_TIDE - MIN_TIDE));
    }

    public static Layer tideLayer(long seed, int x, int y, int z) {
        return unit(hash(seed ^ TIDE_FLECK_SALT ^ y * 0x9E3779B97F4A7C15L, x, z)) < TIDE_FLECK_CHANCE ? Layer.CONGLOMERATE : Layer.TIDESTONE;
    }

    private static Layer layerAtBed(long seed, ElythiaStrataProfile profile, int bed, double summit) {
        int spacing = profile.bandSpacing();
        int band = Math.floorDiv(bed, spacing);
        long key = hash(seed ^ BAND_SALT, band, spacing);
        int baseThickness = profile.minZebra() + (int) (unit(key) * (profile.maxZebra() - profile.minZebra() + 1));
        int thickness = baseThickness + (int) Math.round(summit * (spacing - 2 - baseThickness));
        int slack = Math.min(spacing - 1 - thickness, MAX_BAND_JITTER);
        int start = band * spacing + 1 + (int) (unit(mix(key + 1)) * (slack + 1));
        if (bed >= start && bed < start + thickness) {
            return Layer.ZEBRA;
        }
        if (bed == start - 1 && profile.partingEvery() > 0 && Math.floorMod(band, profile.partingEvery()) == 0) {
            return Layer.CONGLOMERATE;
        }
        return Layer.BASE;
    }

    public static double foldMask(long seed, ElythiaStrataProfile profile, int x, int z) {
        if (profile.foldAmplitude() <= 0.0D) {
            return 0.0D;
        }
        double noise = valueNoise(seed ^ MASK_SALT, x / FOLD_MASK_WAVELENGTH, z / FOLD_MASK_WAVELENGTH);
        return smoothstep((noise - profile.foldThreshold()) / FOLD_MASK_RAMP);
    }

    public static int paintBottom(int top, int lowestNearby, int capDepth) {
        return Math.min(top - Math.max(SHELL_DEPTH, capDepth + 1), lowestNearby);
    }

    public static int biomeJitterX(long seed, int x, int z) {
        return jitter(seed ^ JITTER_X_SALT, x, z);
    }

    public static int biomeJitterZ(long seed, int x, int z) {
        return jitter(seed ^ JITTER_Z_SALT, x, z);
    }

    private static double warp(long seed, ElythiaStrataProfile profile, int x, int z) {
        double wavelength = profile.warpWavelength();
        double broad = valueNoise(seed ^ WARP_SALT, x / wavelength, z / wavelength);
        double fine = valueNoise(mix(seed ^ WARP_SALT), x * 3.0D / wavelength, z * 3.0D / wavelength);
        return profile.warpAmplitude() * (0.7D * broad + 0.3D * fine);
    }

    private static double fold(long seed, ElythiaStrataProfile profile, int x, int z) {
        double wavelength = profile.foldWavelength();
        return valueNoise(seed ^ FOLD_SALT, x / wavelength, z / wavelength);
    }

    private static int jitter(long seed, int x, int z) {
        return (int) (unit(hash(seed, x, z)) * (BIOME_JITTER * 2 + 1)) - BIOME_JITTER;
    }

    private static double valueNoise(long seed, double x, double z) {
        int cellX = (int) Math.floor(x);
        int cellZ = (int) Math.floor(z);
        double blendX = smoothstep(x - cellX);
        double blendZ = smoothstep(z - cellZ);
        double north = lerp(blendX, corner(seed, cellX, cellZ), corner(seed, cellX + 1, cellZ));
        double south = lerp(blendX, corner(seed, cellX, cellZ + 1), corner(seed, cellX + 1, cellZ + 1));
        return lerp(blendZ, north, south);
    }

    private static double corner(long seed, int x, int z) {
        return unit(hash(seed, x, z)) * 2.0D - 1.0D;
    }

    private static double lerp(double delta, double from, double to) {
        return from + delta * (to - from);
    }

    private static double smoothstep(double value) {
        double clamped = Math.max(0.0D, Math.min(1.0D, value));
        return clamped * clamped * (3.0D - 2.0D * clamped);
    }

    private static long hash(long seed, int x, int z) {
        return mix(seed ^ x * 0x632BE59BD9B4E019L ^ z * 0x85157AF5D66D3EABL);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }
}
