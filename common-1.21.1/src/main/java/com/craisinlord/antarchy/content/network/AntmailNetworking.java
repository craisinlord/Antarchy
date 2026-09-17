package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.antmail.AntmailAttachment;
import com.craisinlord.antarchy.content.antmail.AntmailDebug;
import com.craisinlord.antarchy.content.antmail.AntmailDraft;
import com.craisinlord.antarchy.content.antmail.AntmailWire;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class AntmailNetworking {
    private static Consumer<Object> sender = payload -> { };
    private static volatile boolean senderConfigured;
    private static final AtomicBoolean senderErrorLogged = new AtomicBoolean();

    private AntmailNetworking() {
    }

    public static void setSender(Consumer<Object> sender) {
        AntmailNetworking.sender = sender;
        senderConfigured = sender != null;
    }

    public static void setup(BlockPos pos, String username) {
        AntmailDebug.log("C2S setup request pos=" + pos + " usernameLength=" + username.length());
        sendPayload(new AntmailSetupPayload(pos, username));
    }

    public static void requestState(BlockPos pos) {
        requestState(pos, AntmailStateRequestPayload.INBOX, 0, 0L);
    }

    public static void requestState(BlockPos pos, int folder, int page) {
        requestState(pos, folder, page, 0L);
    }

    public static void requestState(BlockPos pos, int folder, int page, long knownVersion) {
        AntmailDebug.log("C2S state request pos=" + pos + " folder=" + folder + " page=" + page + " knownVersion=" + knownVersion);
        sendPayload(new AntmailStateRequestPayload(pos, folder, page, knownVersion));
    }

    public static void requestMessage(BlockPos pos, UUID messageId) {
        sendPayload(new AntmailMessageRequestPayload(pos, messageId.toString()));
    }

    public static void send(BlockPos pos, String recipient, String subject, String body, List<AntmailAttachment> attachments) {
        sendPayload(new AntmailSendPayload(pos, recipient, subject, body, AntmailWire.encodeAttachments(attachments)));
    }

    public static void markRead(BlockPos pos, UUID messageId) {
        sendPayload(new AntmailReadPayload(pos, messageId.toString(), true));
    }

    public static void markUnread(BlockPos pos, UUID messageId) {
        sendPayload(new AntmailReadPayload(pos, messageId.toString(), false));
    }

    public static void delete(BlockPos pos, UUID messageId, boolean sent) {
        sendPayload(new AntmailDeletePayload(pos, messageId.toString(), sent));
    }

    public static void saveDraft(BlockPos pos, AntmailDraft draft) {
        sendPayload(new AntmailDraftPayload(pos, AntmailDraftPayload.SAVE, AntmailWire.encodeTag(draft.toTag())));
    }

    public static void deleteDraft(BlockPos pos, UUID draftId) {
        sendPayload(new AntmailDraftPayload(pos, AntmailDraftPayload.DELETE, draftId.toString()));
    }

    public static void retry(BlockPos pos, UUID messageId) {
        sendPayload(new AntmailRetryPayload(pos, messageId.toString()));
    }

    private static void sendPayload(Object payload) {
        if (!senderConfigured) {
            if (senderErrorLogged.compareAndSet(false, true)) AntmailDebug.error("Network sender is not configured; dropping " + payload.getClass().getSimpleName(), new IllegalStateException("AntmailNetworking.setSender was not called"));
        }
        else sender.accept(payload);
    }
}
