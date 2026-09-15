package com.craisinlord.antarchy.content.computer.paint;

import java.util.Objects;

public record AntPaintFile(String fileId, String filename, AntPaintCanvas canvas, long createdTime, long modifiedTime) {
    public AntPaintFile {
        Objects.requireNonNull(fileId, "fileId");
        filename = AntPaintFilename.normalize(filename);
        Objects.requireNonNull(canvas, "canvas");
        if (fileId.isBlank()) {
            throw new IllegalArgumentException("fileId cannot be blank");
        }
    }

    public AntPaintFile copy() {
        return new AntPaintFile(fileId, filename, canvas.copy(), createdTime, modifiedTime);
    }

    public AntPaintFile withCanvas(AntPaintCanvas value, long modified) {
        return new AntPaintFile(fileId, filename, value, createdTime, modified);
    }

    public AntPaintFile withFilename(String value, long modified) {
        return new AntPaintFile(fileId, value, canvas, createdTime, modified);
    }
}
