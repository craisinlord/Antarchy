package com.craisinlord.antarchy.content.antmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AntmailMailboxStore {
    private final Map<AntmailAddress, AntmailMailbox> mailboxes = new LinkedHashMap<>();

    public AntmailMailbox getOrCreate(AntmailAddress address) {
        return mailboxes.computeIfAbsent(address, AntmailMailbox::new);
    }

    public AntmailMailbox get(AntmailAddress address) {
        return mailboxes.get(address);
    }

    public boolean contains(AntmailAddress address) {
        return mailboxes.containsKey(address);
    }

    public void put(AntmailMailbox mailbox) {
        mailboxes.put(mailbox.address(), mailbox);
    }

    public Collection<AntmailMailbox> mailboxes() {
        return List.copyOf(mailboxes.values());
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag mailboxTags = new ListTag();
        for (AntmailMailbox mailbox : mailboxes.values()) mailboxTags.add(mailbox.toTag());
        tag.put("Mailboxes", mailboxTags);
        return tag;
    }

    public static AntmailMailboxStore fromTag(CompoundTag tag) {
        AntmailMailboxStore store = new AntmailMailboxStore();
        ListTag mailboxTags = tag.getList("Mailboxes", 10);
        for (int index = 0; index < mailboxTags.size(); index++) {
            try {
                AntmailMailbox mailbox = AntmailMailbox.fromTag(mailboxTags.getCompound(index));
                store.mailboxes.put(mailbox.address(), mailbox);
            } catch (RuntimeException ignored) {
            }
        }
        return store;
    }
}
