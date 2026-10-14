package com.craisinlord.antarchy.config;

import com.craisinlord.antarchy.Antarchy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigResetGuard {
    public static final int RESET_GENERATION = 9;
    private static final String MARKER_FILE_NAME = ".config_generation";

    private ConfigResetGuard() {
    }

    public static void migrateIfNeeded(Path configDir, Path... configFiles) {
        Path markerPath = configDir.resolve(MARKER_FILE_NAME);
        if (readGeneration(markerPath) >= RESET_GENERATION) {
            return;
        }

        for (Path configFile : configFiles) {
            if (Files.exists(configFile)) {
                Antarchy.LOGGER.info("Preserving config file {} during config migration (config generation {})", configFile, RESET_GENERATION);
            }
        }

        writeGeneration(markerPath);
    }

    @Deprecated
    public static void wipeIfNeeded(Path configDir, Path... configFiles) {
        migrateIfNeeded(configDir, configFiles);
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
