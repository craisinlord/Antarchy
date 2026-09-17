package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class AntmailWire {
    private static final int MAX_COMPRESSED_BYTES = 1024 * 1024;
    private static final long MAX_DECOMPRESSED_BYTES = 8L * 1024L * 1024L;
    private AntmailWire() {
    }

    public static String encodeAttachments(List<AntmailAttachment> attachments) {
        AntmailValidation.Result validation = AntmailValidation.validateAttachments(attachments);
        if (!validation.valid()) throw new IllegalArgumentException(validation.reason());
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (AntmailAttachment attachment : attachments) list.add(attachment.toTag());
        root.put("Attachments", list);
        String encoded = encodeTag(root);
        if (encoded.length() > AntmailValidation.MAX_ATTACHMENT_WIRE_CHARS) throw new IllegalArgumentException("Attachment payload is too large");
        return encoded;
    }

    public static String encodeTag(CompoundTag tag) {
        if (tag == null) throw new IllegalArgumentException("Antmail tag cannot be null");
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, output);
            byte[] compressed = output.toByteArray();
            if (compressed.length > MAX_COMPRESSED_BYTES) throw new IllegalArgumentException("Antmail data is too large");
            return Base64.getEncoder().encodeToString(compressed);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not encode Antmail data", exception);
        }
    }

    public static CompoundTag decodeTag(String encoded) {
        try {
            if (encoded == null || encoded.isBlank()) throw new IllegalArgumentException("Antmail data is empty");
            byte[] compressed = Base64.getDecoder().decode(encoded);
            if (compressed.length > MAX_COMPRESSED_BYTES) throw new IllegalArgumentException("Antmail data is too large");
            return NbtIo.readCompressed(new ByteArrayInputStream(compressed), NbtAccounter.create(MAX_DECOMPRESSED_BYTES));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid Antmail data", exception);
        }
    }

    public static List<AntmailAttachment> decodeAttachments(String encoded) {
        if (encoded == null || encoded.isBlank()) return List.of();
        if (encoded.length() > AntmailValidation.MAX_ATTACHMENT_WIRE_CHARS) throw new IllegalArgumentException("Attachment payload is too large");
        try {
            CompoundTag root = decodeTag(encoded);
            ListTag list = root.getList("Attachments", 10);
            List<AntmailAttachment> result = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) result.add(AntmailAttachment.fromTag(list.getCompound(i)));
            AntmailValidation.Result validation = AntmailValidation.validateAttachments(result);
            if (!validation.valid()) throw new IllegalArgumentException(validation.reason());
            return List.copyOf(result);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid attachment data", exception);
        }
    }
}
