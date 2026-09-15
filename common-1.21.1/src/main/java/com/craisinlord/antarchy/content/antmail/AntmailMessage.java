package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AntmailMessage {
    private static final String ID_TAG = "Id";
    private static final String FROM_TAG = "From";
    private static final String TO_TAG = "To";
    private static final String SUBJECT_TAG = "Subject";
    private static final String BODY_TAG = "Body";
    private static final String CREATED_TAG = "Created";
    private static final String READ_TAG = "Read";
    private static final String ATTACHMENTS_TAG = "Attachments";

    private final UUID id;
    private final AntmailAddress sender;
    private final AntmailAddress recipient;
    private final String subject;
    private final String body;
    private final long createdAt;
    private final List<AntmailAttachment> attachments;
    private boolean read;

    public AntmailMessage(UUID id, AntmailAddress sender, AntmailAddress recipient, String subject, String body, long createdAt, List<AntmailAttachment> attachments, boolean read) {
        AntmailValidation.Result result = AntmailValidation.validateMessage(sender, recipient, subject, body, attachments);
        if (!result.valid()) {
            throw new IllegalArgumentException(result.reason());
        }
        this.id = id == null ? UUID.randomUUID() : id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.createdAt = createdAt;
        this.attachments = List.copyOf(attachments);
        this.read = read;
    }

    public static AntmailMessage create(AntmailAddress sender, AntmailAddress recipient, String subject, String body, long createdAt, List<AntmailAttachment> attachments) {
        return new AntmailMessage(UUID.randomUUID(), sender, recipient, subject, body, createdAt, attachments, false);
    }

    public UUID id() { return id; }
    public AntmailAddress sender() { return sender; }
    public AntmailAddress recipient() { return recipient; }
    public String subject() { return subject; }
    public String body() { return body; }
    public long createdAt() { return createdAt; }
    public List<AntmailAttachment> attachments() { return attachments; }
    public boolean read() { return read; }
    public void markRead() { read = true; }
    public void markUnread() { read = false; }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ID_TAG, id.toString());
        tag.putString(FROM_TAG, sender.fullAddress());
        tag.putString(TO_TAG, recipient.fullAddress());
        tag.putString(SUBJECT_TAG, subject);
        tag.putString(BODY_TAG, body);
        tag.putLong(CREATED_TAG, createdAt);
        tag.putBoolean(READ_TAG, read);
        ListTag attachmentTags = new ListTag();
        for (AntmailAttachment attachment : attachments) attachmentTags.add(attachment.toTag());
        tag.put(ATTACHMENTS_TAG, attachmentTags);
        return tag;
    }

    public static AntmailMessage fromTag(CompoundTag tag) {
        List<AntmailAttachment> attachments = new ArrayList<>();
        ListTag attachmentTags = tag.getList(ATTACHMENTS_TAG, 10);
        for (int index = 0; index < attachmentTags.size(); index++) attachments.add(AntmailAttachment.fromTag(attachmentTags.getCompound(index)));
        return new AntmailMessage(UUID.fromString(tag.getString(ID_TAG)), AntmailAddress.parse(tag.getString(FROM_TAG)), AntmailAddress.parse(tag.getString(TO_TAG)), tag.getString(SUBJECT_TAG), tag.getString(BODY_TAG), tag.getLong(CREATED_TAG), attachments, tag.getBoolean(READ_TAG));
    }
}
