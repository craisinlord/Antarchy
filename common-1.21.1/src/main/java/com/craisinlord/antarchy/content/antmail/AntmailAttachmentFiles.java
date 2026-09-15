package com.craisinlord.antarchy.content.antmail;

import com.craisinlord.antarchy.content.computer.ComputerFileSystem;
import com.craisinlord.antarchy.content.computer.paint.AntPaintCanvas;
import com.craisinlord.antarchy.content.computer.paint.AntPaintFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class AntmailAttachmentFiles {
    private AntmailAttachmentFiles() {
    }

    public static Optional<AntmailAttachment> fromComputerFile(ComputerFileSystem.ComputerFile file) {
        if (file == null || file.type() != ComputerFileSystem.ComputerFile.Type.TEXT) return Optional.empty();
        try {
            return Optional.of(new AntmailAttachment.TextFile(filename(file.path()), file.contents()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public static AntmailAttachment.TextFile fromTextFile(String path, String contents) {
        return new AntmailAttachment.TextFile(filename(path), contents);
    }

    public static AntmailAttachment.PaintImage fromPaintFile(AntPaintFile file) {
        if (file == null) throw new IllegalArgumentException("Paint file cannot be null");
        AntPaintCanvas canvas = file.canvas();
        if (canvas == null || canvas.copyPixels().length != AntPaintCanvas.PIXEL_COUNT) {
            throw new IllegalArgumentException("Invalid AntPaint canvas");
        }
        boolean[] source = canvas.copyPixels();
        byte[] pixels = new byte[source.length];
        for (int index = 0; index < source.length; index++) pixels[index] = source[index] ? (byte) 1 : (byte) 0;
        return new AntmailAttachment.PaintImage(file.filename(), AntPaintCanvas.WIDTH, AntPaintCanvas.HEIGHT, pixels);
    }

    public static AntPaintFile toPaintFile(AntmailAttachment.PaintImage image, String fileId, long createdTime, long modifiedTime) {
        if (image == null) throw new IllegalArgumentException("Paint attachment cannot be null");
        if (image.width() != AntPaintCanvas.WIDTH || image.height() != AntPaintCanvas.HEIGHT) {
            throw new IllegalArgumentException("Unsupported AntPaint canvas size");
        }
        byte[] source = image.pixels();
        boolean[] pixels = new boolean[source.length];
        for (int index = 0; index < source.length; index++) {
            if (source[index] != 0 && source[index] != 1) throw new IllegalArgumentException("Invalid AntPaint pixel");
            pixels[index] = source[index] == 1;
        }
        AntPaintCanvas canvas = new AntPaintCanvas();
        for (int index = 0; index < pixels.length; index++) {
            if (pixels[index]) canvas.set(index % AntPaintCanvas.WIDTH, index / AntPaintCanvas.WIDTH, true);
        }
        return new AntPaintFile(fileId, image.fileName(), canvas, createdTime, modifiedTime);
    }

    public static ComputerFileSystem.ComputerFile toComputerTextFile(String path, AntmailAttachment.TextFile file) {
        if (file == null) throw new IllegalArgumentException("Text attachment cannot be null");
        String normalized = ComputerFileSystem.normalize(path);
        if (normalized.isEmpty() || normalized.equals(ComputerFileSystem.ROOT)) throw new IllegalArgumentException("Invalid text file path");
        return ComputerFileSystem.ComputerFile.text(normalized, file.contents());
    }

    public static List<ComputerFileSystem.ComputerFile> selectableTextFiles(Collection<ComputerFileSystem.ComputerFile> files) {
        if (files == null) return List.of();
        List<ComputerFileSystem.ComputerFile> result = new ArrayList<>();
        for (ComputerFileSystem.ComputerFile file : files) {
            if (fromComputerFile(file).isPresent()) result.add(file);
        }
        return List.copyOf(result);
    }

    public static List<AntmailAttachment> selectablePaintFiles(Collection<AntPaintFile> files) {
        if (files == null) return List.of();
        List<AntmailAttachment> result = new ArrayList<>();
        for (AntPaintFile file : files) {
            try {
                result.add(fromPaintFile(file));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return List.copyOf(result);
    }

    private static String filename(String path) {
        String normalized = ComputerFileSystem.normalize(path);
        if (normalized.isEmpty() || normalized.equals(ComputerFileSystem.ROOT)) throw new IllegalArgumentException("Invalid file path");
        int separator = normalized.lastIndexOf('/');
        return normalized.substring(separator + 1);
    }
}
