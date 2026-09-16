package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AntmailMailbox {
    public static final int PAGE_SIZE = 20;
    private final AntmailAddress address;
    private final List<AntmailMessage> inbox = new ArrayList<>();
    private final List<AntmailMessage> sent = new ArrayList<>();
    private final List<AntmailDraft> drafts = new ArrayList<>();
    private int reportedUnreadCount = -1;

    public AntmailMailbox(AntmailAddress address) {
        this.address = address;
    }

    public AntmailAddress address() { return address; }
    public List<AntmailMessage> inbox() { return List.copyOf(inbox); }
    public List<AntmailMessage> sent() { return List.copyOf(sent); }
    public List<AntmailDraft> drafts() { return List.copyOf(drafts); }
    public int unreadCount() { return reportedUnreadCount >= 0 ? reportedUnreadCount : (int) inbox.stream().filter(message -> !message.read()).count(); }

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
    public AntmailMessage findSent(UUID id) { return sent.stream().filter(message -> message.id().equals(id)).findFirst().orElse(null); }
    public AntmailMessage findInbox(UUID id) { return inbox.stream().filter(message -> message.id().equals(id)).findFirst().orElse(null); }

    public boolean markRead(UUID id) {
        for (AntmailMessage message : inbox) if (message.id().equals(id)) { message.markRead(); return true; }
        return false;
    }

    public AntmailDraft findDraft(UUID id) { return drafts.stream().filter(draft -> draft.id().equals(id)).findFirst().orElse(null); }

    public CompoundTag toTag() {
        return toTag(true);
    }

    public CompoundTag toTag(boolean includeMessageAttachments) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Address", address.fullAddress());
        tag.put("Inbox", messagesToTag(inbox, includeMessageAttachments));
        tag.put("Sent", messagesToTag(sent, includeMessageAttachments));
        ListTag draftTags = new ListTag();
        for (AntmailDraft draft : drafts) draftTags.add(draft.toTag());
        tag.put("Drafts", draftTags);
        return tag;
    }

    public CompoundTag toPageTag(int folder, int page) {
        int safePage = Math.max(0, page);
        int start = safePage * PAGE_SIZE;
        CompoundTag tag = new CompoundTag();
        tag.putString("Address", address.fullAddress());
        tag.putInt("Folder", folder);
        tag.putInt("Page", safePage);
        tag.putInt("PageSize", PAGE_SIZE);
        tag.putInt("TotalInbox", inbox.size());
        tag.putInt("TotalSent", sent.size());
        tag.putInt("UnreadCount", (int) inbox.stream().filter(message -> !message.read()).count());
        tag.put("Inbox", folder == 0 ? messagesToTag(page(inbox, start), false) : new ListTag());
        tag.put("Sent", folder == 1 ? messagesToTag(page(sent, start), false) : new ListTag());
        ListTag draftTags = new ListTag();
        for (AntmailDraft draft : drafts) draftTags.add(draft.toTag());
        tag.put("Drafts", draftTags);
        return tag;
    }

    private static <T> List<T> page(List<T> values, int start) {
        if (start >= values.size()) return List.of();
        return values.subList(start, Math.min(start + PAGE_SIZE, values.size()));
    }

    public static AntmailMailbox fromTag(CompoundTag tag) {
        AntmailMailbox mailbox = new AntmailMailbox(AntmailAddress.parse(tag.getString("Address")));
        if (tag.contains("UnreadCount")) mailbox.reportedUnreadCount = Math.max(0, tag.getInt("UnreadCount"));
        readMessages(tag.getList("Inbox", 10), mailbox.inbox);
        readMessages(tag.getList("Sent", 10), mailbox.sent);
        ListTag draftTags = tag.getList("Drafts", 10);
        for (int index = 0; index < draftTags.size() && mailbox.drafts.size() < AntmailValidation.MAX_DRAFTS; index++) {
            try { mailbox.drafts.add(AntmailDraft.fromTag(draftTags.getCompound(index))); } catch (RuntimeException ignored) { }
        }
        return mailbox;
    }

    private static ListTag messagesToTag(List<AntmailMessage> messages, boolean includeMessageAttachments) {
        ListTag tags = new ListTag();
        for (AntmailMessage message : messages) tags.add(message.toTag(includeMessageAttachments));
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
