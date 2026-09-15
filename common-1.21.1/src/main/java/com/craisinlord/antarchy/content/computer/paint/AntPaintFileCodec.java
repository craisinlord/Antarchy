package com.craisinlord.antarchy.content.computer.paint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class AntPaintFileCodec {
    public static final int VERSION = 1;
    public static final int MAX_ENCODED_BYTES = 1024;
    private static final int MAGIC = 0x41505431;

    private AntPaintFileCodec() {
    }

    public static byte[] encode(AntPaintFile file) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream(MAX_ENCODED_BYTES);
            DataOutputStream data = new DataOutputStream(output);
            data.writeInt(MAGIC);
            data.writeByte(VERSION);
            writeString(data, file.fileId());
            writeString(data, file.filename());
            data.writeLong(file.createdTime());
            data.writeLong(file.modifiedTime());
            data.writeByte(AntPaintCanvas.WIDTH);
            data.writeByte(AntPaintCanvas.HEIGHT);
            data.write(file.canvas().pack());
            data.flush();
            byte[] encoded = output.toByteArray();
            if (encoded.length > MAX_ENCODED_BYTES) {
                throw new IllegalArgumentException("AntPaint file exceeds encoded size limit");
            }
            return encoded;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to encode AntPaint file", exception);
        }
    }

    public static AntPaintFile decode(byte[] encoded) {
        if (encoded == null || encoded.length > MAX_ENCODED_BYTES) {
            throw new IllegalArgumentException("Invalid AntPaint file size");
        }
        try {
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(encoded));
            if (data.readInt() != MAGIC || data.readUnsignedByte() != VERSION) {
                throw new IllegalArgumentException("Unsupported AntPaint file format");
            }
            String fileId = readString(data, 128);
            String filename = readString(data, AntPaintFilename.MAX_LENGTH + AntPaintFilename.EXTENSION.length());
            long created = data.readLong();
            long modified = data.readLong();
            if (data.readUnsignedByte() != AntPaintCanvas.WIDTH || data.readUnsignedByte() != AntPaintCanvas.HEIGHT) {
                throw new IllegalArgumentException("Unsupported AntPaint canvas size");
            }
            byte[] pixels = data.readNBytes(AntPaintCanvas.PACKED_LENGTH);
            if (pixels.length != AntPaintCanvas.PACKED_LENGTH || data.available() != 0) {
                throw new IllegalArgumentException("Invalid AntPaint pixel payload");
            }
            return new AntPaintFile(fileId, filename, AntPaintCanvas.unpack(pixels), created, modified);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid AntPaint file data", exception);
        }
    }

    private static void writeString(DataOutputStream data, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 128) {
            throw new IllegalArgumentException("AntPaint string exceeds size limit");
        }
        data.writeShort(bytes.length);
        data.write(bytes);
    }

    private static String readString(DataInputStream data, int maxBytes) throws IOException {
        int length = data.readUnsignedShort();
        if (length > maxBytes) {
            throw new IllegalArgumentException("AntPaint string exceeds size limit");
        }
        byte[] bytes = new byte[length];
        data.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
