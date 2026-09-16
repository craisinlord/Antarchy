package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.antmail.AntmailAttachment;
import com.craisinlord.antarchy.content.antmail.AntmailDraft;
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
        requestState(pos, AntmailStateRequestPayload.INBOX, 0, 0L);
    }

    public static void requestState(BlockPos pos, int folder, int page) {
        requestState(pos, folder, page, 0L);
    }

    public static void requestState(BlockPos pos, int folder, int page, long knownVersion) {
        sender.accept(new AntmailStateRequestPayload(pos, folder, page, knownVersion));
    }

    public static void requestMessage(BlockPos pos, UUID messageId) {
        sender.accept(new AntmailMessageRequestPayload(pos, messageId.toString()));
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

    public static void delete(BlockPos pos, UUID messageId, boolean sent) {
        sender.accept(new AntmailDeletePayload(pos, messageId.toString(), sent));
    }

    public static void saveDraft(BlockPos pos, AntmailDraft draft) {
        sender.accept(new AntmailDraftPayload(pos, AntmailDraftPayload.SAVE, AntmailWire.encodeTag(draft.toTag())));
    }

    public static void deleteDraft(BlockPos pos, UUID draftId) {
        sender.accept(new AntmailDraftPayload(pos, AntmailDraftPayload.DELETE, draftId.toString()));
    }

    public static void retry(BlockPos pos, UUID messageId) {
        sender.accept(new AntmailRetryPayload(pos, messageId.toString()));
    }
}
