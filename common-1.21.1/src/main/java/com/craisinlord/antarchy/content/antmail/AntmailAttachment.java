package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public sealed interface AntmailAttachment permits AntmailAttachment.TextFile, AntmailAttachment.PaintImage {
    String TYPE_TAG = "Type";
    String NAME_TAG = "Name";

    String fileName();

    int byteSize();

    CompoundTag toTag();

    static AntmailAttachment fromTag(CompoundTag tag) {
        return switch (tag.getString(TYPE_TAG)) {
            case "text_file" -> TextFile.fromTag(tag);
            case "paint_image" -> PaintImage.fromTag(tag);
            default -> throw new IllegalArgumentException("Unknown Antmail attachment type");
        };
    }

    record TextFile(String fileName, String contents) implements AntmailAttachment {
        public TextFile {
            fileName = validateFileName(fileName);
            contents = Objects.requireNonNull(contents, "contents");
            if (contents.length() > AntmailValidation.MAX_TEXT_ATTACHMENT_CHARACTERS) {
                throw new IllegalArgumentException("Text attachment is too large");
            }
        }

        @Override
        public int byteSize() {
            return contents.getBytes(StandardCharsets.UTF_8).length;
        }

        @Override
        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TYPE_TAG, "text_file");
            tag.putString(NAME_TAG, fileName);
            tag.putString("Contents", contents);
            return tag;
        }

        static TextFile fromTag(CompoundTag tag) {
            return new TextFile(tag.getString(NAME_TAG), tag.getString("Contents"));
        }
    }

    record PaintImage(String fileName, int width, int height, byte[] pixels) implements AntmailAttachment {
        public PaintImage {
            fileName = validateFileName(fileName);
            if (width != AntmailValidation.MAX_PAINT_WIDTH || height != AntmailValidation.MAX_PAINT_HEIGHT) {
                throw new IllegalArgumentException("Invalid Paint image dimensions");
            }
            if (pixels == null || pixels.length != width * height) {
                throw new IllegalArgumentException("Paint pixel data does not match dimensions");
            }
            for (byte pixel : pixels) {
                if (pixel != 0 && pixel != 1) throw new IllegalArgumentException("Paint pixel data must be monochrome");
            }
            pixels = Arrays.copyOf(pixels, pixels.length);
        }

        @Override
        public byte[] pixels() {
            return Arrays.copyOf(pixels, pixels.length);
        }

        @Override
        public int byteSize() {
            return pixels.length;
        }

        @Override
        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TYPE_TAG, "paint_image");
            tag.putString(NAME_TAG, fileName);
            tag.putInt("Width", width);
            tag.putInt("Height", height);
            tag.putByteArray("Pixels", pixels);
            return tag;
        }

        static PaintImage fromTag(CompoundTag tag) {
            return new PaintImage(tag.getString(NAME_TAG), tag.getInt("Width"), tag.getInt("Height"), tag.getByteArray("Pixels"));
        }
    }

    private static String validateFileName(String value) {
        if (value == null || value.isBlank() || value.length() > AntmailValidation.MAX_FILE_NAME_LENGTH || value.contains("/") || value.contains("\\")) {
            throw new IllegalArgumentException("Invalid attachment filename");
        }
        return value;
    }
}
