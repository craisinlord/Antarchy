package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class AntmailWire {
    private AntmailWire() {
    }

    public static String encodeAttachments(List<AntmailAttachment> attachments) {
        AntmailValidation.Result validation = AntmailValidation.validateAttachments(attachments);
        if (!validation.valid()) throw new IllegalArgumentException(validation.reason());
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (AntmailAttachment attachment : attachments) list.add(attachment.toTag());
        root.put("Attachments", list);
        String encoded = Base64.getEncoder().encodeToString(root.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (encoded.length() > AntmailValidation.MAX_ATTACHMENT_WIRE_CHARS) throw new IllegalArgumentException("Attachment payload is too large");
        return encoded;
    }

    public static String encodeTag(CompoundTag tag) {
        return Base64.getEncoder().encodeToString(tag.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static CompoundTag decodeTag(String encoded) {
        try {
            return TagParser.parseTag(new String(Base64.getDecoder().decode(encoded), java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid Antmail data", exception);
        }
    }

    public static List<AntmailAttachment> decodeAttachments(String encoded) {
        if (encoded == null || encoded.isBlank()) return List.of();
        if (encoded.length() > AntmailValidation.MAX_ATTACHMENT_WIRE_CHARS) throw new IllegalArgumentException("Attachment payload is too large");
        try {
            String snbt = new String(Base64.getDecoder().decode(encoded), java.nio.charset.StandardCharsets.UTF_8);
            CompoundTag root = TagParser.parseTag(snbt);
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
