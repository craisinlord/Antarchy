package com.craisinlord.antarchy.config;

import com.craisinlord.antarchy.Antarchy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigResetGuard {
    public static final int RESET_GENERATION = 6;
    private static final String MARKER_FILE_NAME = ".config_generation";

    private ConfigResetGuard() {
    }

    public static void wipeIfNeeded(Path configDir, Path... configFiles) {
        Path markerPath = configDir.resolve(MARKER_FILE_NAME);
        if (readGeneration(markerPath) >= RESET_GENERATION) {
            return;
        }

        for (Path configFile : configFiles) {
            try {
                if (Files.deleteIfExists(configFile)) {
                    Antarchy.LOGGER.info("Reset config file {} to defaults (config generation {})", configFile, RESET_GENERATION);
                }
            } catch (IOException e) {
                Antarchy.LOGGER.warn("Failed to delete config file {} during config reset", configFile, e);
            }
        }

        writeGeneration(markerPath);
    }

    private static int readGeneration(Path markerPath) {
        if (!Files.exists(markerPath)) {
            return 0;
        }

        try {
            return Integer.parseInt(Files.readString(markerPath).trim());
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    private static void writeGeneration(Path markerPath) {
        try {
            Files.createDirectories(markerPath.getParent());
            Files.writeString(markerPath, Integer.toString(RESET_GENERATION));
        } catch (IOException e) {
            Antarchy.LOGGER.warn("Failed to write config generation marker at {}", markerPath, e);
        }
    }
}
