package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.antmail.AntmailAddress;
import com.craisinlord.antarchy.content.antmail.AntmailAttachment;
import com.craisinlord.antarchy.content.antmail.AntmailDeliveryResult;
import com.craisinlord.antarchy.content.antmail.AntmailDraft;
import com.craisinlord.antarchy.content.antmail.AntmailMessage;
import com.craisinlord.antarchy.content.antmail.AntmailMailbox;
import com.craisinlord.antarchy.content.antmail.AntmailServerData;
import com.craisinlord.antarchy.content.antmail.AntmailValidation;
import com.craisinlord.antarchy.content.antmail.AntmailWire;
import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class AntmailServerHandler {
    private static BiConsumer<ServerPlayer, AntmailResultPayload> resultSender = (player, payload) -> { };

    private AntmailServerHandler() {
    }

    public static void setResultSender(BiConsumer<ServerPlayer, AntmailResultPayload> sender) {
        resultSender = sender;
    }

    public static void handle(ServerPlayer player, AntmailSetupPayload payload) {
        ComputerBlockEntity computer = computer(player, payload.pos());
        if (computer == null || !computer.canUseFileSystem(player)) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", "", "registration_failed:unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailServerData.Registration registration = data.register(player.server, player.serverLevel(), payload.pos(), payload.username());
        if (registration.valid()) data.setLastUsedAddress(player, registration.address());
        String mailbox = registration.valid() ? AntmailWire.encodeTag(data.mailboxOrCreate(registration.address()).toTag(false)) : "";
        String detail = registration.valid() ? "registration_success" : "registration_failed:" + registration.reason();
        result(player, payload.pos(), registration.valid() ? AntmailDeliveryResult.Status.DELIVERED.ordinal() : AntmailDeliveryResult.Status.FAILED.ordinal(), registration.valid() ? registration.address().fullAddress() : "", "", detail, mailbox);
    }

    public static void handle(ServerPlayer player, AntmailStateRequestPayload payload) {
        ComputerBlockEntity computer = authorizedComputer(player, payload.pos());
        if (computer == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", "", "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        data.drainAddress(player.server, player.serverLevel(), payload.pos());
        AntmailAddress address = data.addressAt(player.serverLevel().dimension().location(), payload.pos());
        if (address == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND.ordinal(), "", "", "unconfigured", "");
            return;
        }
        AntmailMailbox mailbox = data.mailboxOrCreate(address);
        if (payload.knownVersion() == data.mailboxVersion(address)) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.DELIVERED.ordinal(), address.fullAddress(), "", "unchanged", "");
            return;
        }
        int folder = payload.folder() < AntmailStateRequestPayload.INBOX || payload.folder() > AntmailStateRequestPayload.DRAFTS
                ? AntmailStateRequestPayload.INBOX : payload.folder();
        int page = Math.max(0, Math.min(payload.page(), AntmailValidation.MAX_MAILBOX_MESSAGES / AntmailMailbox.PAGE_SIZE));
        result(player, payload.pos(), AntmailDeliveryResult.Status.DELIVERED.ordinal(), address.fullAddress(), "", "", AntmailWire.encodeTag(mailbox.toPageTag(folder, page)));
    }

    public static void handle(ServerPlayer player, AntmailMessageRequestPayload payload) {
        ComputerBlockEntity computer = authorizedComputer(player, payload.pos());
        if (computer == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", payload.messageId(), "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailAddress address = data.addressAt(player.serverLevel().dimension().location(), payload.pos());
        if (address == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND.ordinal(), "", payload.messageId(), "unconfigured", "");
            return;
        }
        try {
            UUID id = UUID.fromString(payload.messageId());
            AntmailMailbox mailbox = data.mailboxOrCreate(address);
            AntmailMessage message = mailbox.findInbox(id);
            if (message == null) message = mailbox.findSent(id);
            if (message == null) {
                result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), "not_found", "");
                return;
            }
            result(player, payload.pos(), AntmailDeliveryResult.Status.DELIVERED.ordinal(), address.fullAddress(), payload.messageId(), "message_detail", AntmailWire.encodeTag(message.toTag()));
        } catch (IllegalArgumentException exception) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), "invalid_id", "");
        }
    }

    public static void handle(ServerPlayer player, AntmailSendPayload payload) {
        ComputerBlockEntity computer = authorizedComputer(player, payload.pos());
        if (computer == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", "", "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailAddress sender = data.addressAt(player.serverLevel().dimension().location(), payload.pos());
        if (sender == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", "", "unconfigured", "");
            return;
        }
        try {
            AntmailAddress recipient = AntmailAddress.parse(payload.recipient());
            List<AntmailAttachment> attachments = AntmailWire.decodeAttachments(payload.attachments());
            AntmailMessage message = AntmailMessage.create(sender, recipient, payload.subject(), payload.body(), player.serverLevel().getGameTime(), attachments);
            AntmailDeliveryResult delivery = data.deliver(player.server, message);
            result(player, payload.pos(), delivery.status().ordinal(), sender.fullAddress(), message.id().toString(), delivery.detail(), AntmailWire.encodeTag(data.mailboxOrCreate(sender).toTag(false)));
        } catch (RuntimeException exception) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.MESSAGE_INVALID.ordinal(), "", "", "invalid_message", "");
        }
    }

    public static void handle(ServerPlayer player, AntmailReadPayload payload) {
        ComputerBlockEntity computer = authorizedComputer(player, payload.pos());
        if (computer == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", payload.messageId(), "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailAddress address = data.addressAt(player.serverLevel().dimension().location(), payload.pos());
        if (address == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND.ordinal(), "", payload.messageId(), "unconfigured", "");
            return;
        }
        try {
            UUID id = UUID.fromString(payload.messageId());
            AntmailMailbox mailbox = data.mailboxOrCreate(address);
            boolean changed = payload.read() ? mailbox.markRead(id) : markUnread(mailbox, id);
            if (changed) data.touchMailbox(address);
            result(player, payload.pos(), changed ? AntmailDeliveryResult.Status.DELIVERED.ordinal() : AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), changed ? "" : "not_found", AntmailWire.encodeTag(mailbox.toTag(false)));
        } catch (IllegalArgumentException exception) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), "invalid_id", "");
        }
    }

    public static void handle(ServerPlayer player, AntmailDeletePayload payload) {
        ComputerBlockEntity computer = authorizedComputer(player, payload.pos());
        if (computer == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", payload.messageId(), "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailAddress address = data.addressAt(player.serverLevel().dimension().location(), payload.pos());
        if (address == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND.ordinal(), "", payload.messageId(), "unconfigured", "");
            return;
        }
        try {
            UUID id = UUID.fromString(payload.messageId());
            AntmailMailbox mailbox = data.mailboxOrCreate(address);
            boolean changed = payload.sent() ? mailbox.removeSent(id) : mailbox.removeInbox(id);
            if (changed) data.touchMailbox(address);
            result(player, payload.pos(), changed ? AntmailDeliveryResult.Status.DELIVERED.ordinal() : AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), changed ? "" : "not_found", AntmailWire.encodeTag(mailbox.toTag(false)));
        } catch (IllegalArgumentException exception) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), "invalid_id", "");
        }
    }

    public static void handle(ServerPlayer player, AntmailDraftPayload payload) {
        ComputerBlockEntity computer = authorizedComputer(player, payload.pos());
        if (computer == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", "", "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailAddress address = data.addressAt(player.serverLevel().dimension().location(), payload.pos());
        if (address == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND.ordinal(), "", "", "unconfigured", "");
            return;
        }
        AntmailMailbox mailbox = data.mailboxOrCreate(address);
        try {
            if (payload.action() == AntmailDraftPayload.SAVE) {
                AntmailDraft draft = AntmailDraft.fromTag(AntmailWire.decodeTag(payload.data()));
                AntmailDraft stored = new AntmailDraft(draft.id(), address, draft.recipient(), draft.subject(), draft.body(), draft.attachments());
                if (!mailbox.saveDraft(stored)) throw new IllegalArgumentException("draft_limit");
            } else if (payload.action() == AntmailDraftPayload.DELETE) {
                if (!mailbox.removeDraft(UUID.fromString(payload.data()))) throw new IllegalArgumentException("not_found");
            } else {
                throw new IllegalArgumentException("invalid_action");
            }
            data.touchMailbox(address);
            result(player, payload.pos(), AntmailDeliveryResult.Status.DELIVERED.ordinal(), address.fullAddress(), "", "draft_saved", AntmailWire.encodeTag(mailbox.toTag(false)));
        } catch (RuntimeException exception) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.MESSAGE_INVALID.ordinal(), address.fullAddress(), "", "draft_failed", AntmailWire.encodeTag(mailbox.toTag(false)));
        }
    }

    public static void handle(ServerPlayer player, AntmailRetryPayload payload) {
        ComputerBlockEntity computer = authorizedComputer(player, payload.pos());
        if (computer == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", payload.messageId(), "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailAddress sender = data.addressAt(player.serverLevel().dimension().location(), payload.pos());
        if (sender == null) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND.ordinal(), "", payload.messageId(), "unconfigured", "");
            return;
        }
        try {
            AntmailDeliveryResult delivery = data.retry(player.server, sender, UUID.fromString(payload.messageId()));
            AntmailMailbox mailbox = data.mailboxOrCreate(sender);
            result(player, payload.pos(), delivery == null ? AntmailDeliveryResult.Status.MESSAGE_INVALID.ordinal() : delivery.status().ordinal(), sender.fullAddress(), payload.messageId(), delivery == null ? "not_found" : delivery.detail(), AntmailWire.encodeTag(mailbox.toTag(false)));
        } catch (IllegalArgumentException exception) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.MESSAGE_INVALID.ordinal(), sender.fullAddress(), payload.messageId(), "invalid_id", "");
        }
    }

    private static ComputerBlockEntity authorizedComputer(ServerPlayer player, BlockPos pos) {
        ComputerBlockEntity computer = computer(player, pos);
        return computer != null && computer.canUseFileSystem(player) ? computer : null;
    }

    private static boolean markUnread(AntmailMailbox mailbox, UUID id) {
        for (com.craisinlord.antarchy.content.antmail.AntmailMessage message : mailbox.inbox()) {
            if (message.id().equals(id)) {
                message.markUnread();
                return true;
            }
        }
        return false;
    }

    private static ComputerBlockEntity computer(ServerPlayer player, BlockPos pos) {
        if (!player.serverLevel().hasChunkAt(pos) || player.distanceToSqr(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D) > 64D) return null;
        BlockEntity blockEntity = player.serverLevel().getBlockEntity(pos);
        return blockEntity instanceof ComputerBlockEntity computer ? computer : null;
    }

    private static void result(ServerPlayer player, BlockPos pos, int status, String address, String messageId, String detail, String data) {
        long version = 0L;
        try {
            if (!address.isBlank()) version = AntmailServerData.access(player.server).mailboxVersion(AntmailAddress.parse(address));
        } catch (IllegalArgumentException ignored) {
        }
        resultSender.accept(player, new AntmailResultPayload(pos, status, address, messageId, detail, data, version));
    }
}
