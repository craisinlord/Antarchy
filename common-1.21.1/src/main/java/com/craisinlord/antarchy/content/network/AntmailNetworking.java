package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.antmail.AntmailAttachment;
import com.craisinlord.antarchy.content.antmail.AntmailWire;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class AntmailNetworking {
    private static Consumer<Object> sender = payload -> { };

    private AntmailNetworking() {
    }

    public static void setSender(Consumer<Object> sender) {
        AntmailNetworking.sender = sender;
    }

    public static void setup(BlockPos pos, String username) {
        sender.accept(new AntmailSetupPayload(pos, username));
    }

    public static void requestState(BlockPos pos) {
        sender.accept(new AntmailStateRequestPayload(pos));
    }

    public static void send(BlockPos pos, String recipient, String subject, String body, List<AntmailAttachment> attachments) {
        sender.accept(new AntmailSendPayload(pos, recipient, subject, body, AntmailWire.encodeAttachments(attachments)));
    }

    public static void markRead(BlockPos pos, UUID messageId) {
        sender.accept(new AntmailReadPayload(pos, messageId.toString(), true));
    }

    public static void markUnread(BlockPos pos, UUID messageId) {
        sender.accept(new AntmailReadPayload(pos, messageId.toString(), false));
    }
}
