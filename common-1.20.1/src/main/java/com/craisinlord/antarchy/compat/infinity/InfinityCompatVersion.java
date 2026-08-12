package com.craisinlord.antarchy.compat.infinity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfinityCompatVersion {
    private static final Pattern VERSION_PARTS = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");
    private static final int REQUIRED_MAJOR = 2;
    private static final int REQUIRED_MINOR = 7;
    private static final int REQUIRED_PATCH = 2;

    private InfinityCompatVersion() {
    }

    public static boolean isSupported(String version) {
        int[] parsed = parse(version);
        if (parsed == null) {
            return false;
        }
        if (parsed[0] != REQUIRED_MAJOR) {
            return parsed[0] > REQUIRED_MAJOR;
        }
        if (parsed[1] != REQUIRED_MINOR) {
            return parsed[1] > REQUIRED_MINOR;
        }
        return parsed[2] >= REQUIRED_PATCH;
    }

    public static String requiredVersion() {
        return REQUIRED_MAJOR + "." + REQUIRED_MINOR + "." + REQUIRED_PATCH;
    }

    private static int[] parse(String version) {
        if (version == null || version.isBlank()) {
            return null;
        }

        Matcher matcher = VERSION_PARTS.matcher(version);
        if (!matcher.find()) {
            return null;
        }

        return new int[]{
                parsePart(matcher.group(1)),
                parsePart(matcher.group(2)),
                parsePart(matcher.group(3))
        };
    }

    private static int parsePart(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }
}
