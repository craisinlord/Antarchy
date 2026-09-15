package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AntmailDraft(UUID id, AntmailAddress sender, AntmailAddress recipient, String subject, String body, List<AntmailAttachment> attachments) {
    public AntmailDraft {
        id = id == null ? UUID.randomUUID() : id;
        subject = subject == null ? "" : subject;
        body = body == null ? "" : body;
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
        if (subject.length() > AntmailValidation.MAX_SUBJECT_LENGTH || body.length() > AntmailValidation.MAX_BODY_LENGTH || attachments.size() > AntmailValidation.MAX_ATTACHMENTS) {
            throw new IllegalArgumentException("Draft exceeds Antmail limits");
        }
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id.toString());
        if (sender != null) tag.putString("From", sender.fullAddress());
        if (recipient != null) tag.putString("To", recipient.fullAddress());
        tag.putString("Subject", subject);
        tag.putString("Body", body);
        ListTag attachmentTags = new ListTag();
        for (AntmailAttachment attachment : attachments) attachmentTags.add(attachment.toTag());
        tag.put("Attachments", attachmentTags);
        return tag;
    }

    public static AntmailDraft fromTag(CompoundTag tag) {
        List<AntmailAttachment> attachments = new ArrayList<>();
        ListTag attachmentTags = tag.getList("Attachments", 10);
        for (int index = 0; index < attachmentTags.size(); index++) attachments.add(AntmailAttachment.fromTag(attachmentTags.getCompound(index)));
        UUID id = UUID.fromString(tag.getString("Id"));
        AntmailAddress sender = tag.contains("From") ? AntmailAddress.parse(tag.getString("From")) : null;
        AntmailAddress recipient = tag.contains("To") ? AntmailAddress.parse(tag.getString("To")) : null;
        return new AntmailDraft(id, sender, recipient, tag.getString("Subject"), tag.getString("Body"), attachments);
    }
}
