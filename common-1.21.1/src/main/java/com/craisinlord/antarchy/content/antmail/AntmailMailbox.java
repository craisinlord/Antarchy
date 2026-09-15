package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AntmailMailbox {
    private final AntmailAddress address;
    private final List<AntmailMessage> inbox = new ArrayList<>();
    private final List<AntmailMessage> sent = new ArrayList<>();
    private final List<AntmailDraft> drafts = new ArrayList<>();

    public AntmailMailbox(AntmailAddress address) {
        this.address = address;
    }

    public AntmailAddress address() { return address; }
    public List<AntmailMessage> inbox() { return List.copyOf(inbox); }
    public List<AntmailMessage> sent() { return List.copyOf(sent); }
    public List<AntmailDraft> drafts() { return List.copyOf(drafts); }
    public int unreadCount() { return (int) inbox.stream().filter(message -> !message.read()).count(); }

    public boolean addIncoming(AntmailMessage message) {
        if (!address.equals(message.recipient()) || inbox.size() >= AntmailValidation.MAX_MAILBOX_MESSAGES || contains(inbox, message.id())) return false;
        inbox.add(message);
        return true;
    }

    public boolean addSent(AntmailMessage message) {
        if (!address.equals(message.sender()) || sent.size() >= AntmailValidation.MAX_MAILBOX_MESSAGES || contains(sent, message.id())) return false;
        sent.add(message);
        return true;
    }

    public boolean saveDraft(AntmailDraft draft) {
        if (!address.equals(draft.sender()) || drafts.size() >= AntmailValidation.MAX_DRAFTS && findDraft(draft.id()) == null) return false;
        drafts.removeIf(existing -> existing.id().equals(draft.id()));
        drafts.add(draft);
        return true;
    }

    public boolean removeDraft(UUID id) { return drafts.removeIf(draft -> draft.id().equals(id)); }
    public boolean removeInbox(UUID id) { return inbox.removeIf(message -> message.id().equals(id)); }
    public boolean removeSent(UUID id) { return sent.removeIf(message -> message.id().equals(id)); }

    public boolean markRead(UUID id) {
        for (AntmailMessage message : inbox) if (message.id().equals(id)) { message.markRead(); return true; }
        return false;
    }

    public AntmailDraft findDraft(UUID id) { return drafts.stream().filter(draft -> draft.id().equals(id)).findFirst().orElse(null); }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Address", address.fullAddress());
        tag.put("Inbox", messagesToTag(inbox));
        tag.put("Sent", messagesToTag(sent));
        ListTag draftTags = new ListTag();
        for (AntmailDraft draft : drafts) draftTags.add(draft.toTag());
        tag.put("Drafts", draftTags);
        return tag;
    }

    public static AntmailMailbox fromTag(CompoundTag tag) {
        AntmailMailbox mailbox = new AntmailMailbox(AntmailAddress.parse(tag.getString("Address")));
        readMessages(tag.getList("Inbox", 10), mailbox.inbox);
        readMessages(tag.getList("Sent", 10), mailbox.sent);
        ListTag draftTags = tag.getList("Drafts", 10);
        for (int index = 0; index < draftTags.size() && mailbox.drafts.size() < AntmailValidation.MAX_DRAFTS; index++) {
            try { mailbox.drafts.add(AntmailDraft.fromTag(draftTags.getCompound(index))); } catch (RuntimeException ignored) { }
        }
        return mailbox;
    }

    private static ListTag messagesToTag(List<AntmailMessage> messages) {
        ListTag tags = new ListTag();
        for (AntmailMessage message : messages) tags.add(message.toTag());
        return tags;
    }

    private static void readMessages(ListTag tags, List<AntmailMessage> target) {
        for (int index = 0; index < tags.size() && target.size() < AntmailValidation.MAX_MAILBOX_MESSAGES; index++) {
            try { target.add(AntmailMessage.fromTag(tags.getCompound(index))); } catch (RuntimeException ignored) { }
        }
    }

    private static boolean contains(List<AntmailMessage> messages, UUID id) {
        return messages.stream().anyMatch(message -> message.id().equals(id));
    }
}
