package com.craisinlord.antarchy.content.computer.paint;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AntPaintFilename {
    public static final int MAX_LENGTH = 32;
    public static final String EXTENSION = ".antpaint";
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9 _.-]+");

    private AntPaintFilename() {
    }

    public static boolean isValid(String filename) {
        if (filename == null || filename.isBlank() || filename.length() > MAX_LENGTH) {
            return false;
        }
        if (filename.equals(".") || filename.equals("..") || filename.contains("..")) {
            return false;
        }
        return SAFE_NAME.matcher(filename).matches();
    }

    public static String normalize(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        String value = filename.trim();
        if (value.toLowerCase(Locale.ROOT).endsWith(EXTENSION)) {
            value = value.substring(0, value.length() - EXTENSION.length());
        }
        value = value.trim();
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid AntPaint filename");
        }
        return value + EXTENSION;
    }
}
