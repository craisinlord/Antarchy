package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.content.antmail.AntmailAddress;
import com.craisinlord.antarchy.content.antmail.AntmailAttachment;
import com.craisinlord.antarchy.content.antmail.AntmailDeliveryResult;
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
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), "", "", "unauthorized", "");
            return;
        }
        AntmailServerData data = AntmailServerData.access(player.server);
        AntmailServerData.Registration registration = data.register(player.server, player.serverLevel(), payload.pos(), payload.username());
        result(player, payload.pos(), registration.valid() ? AntmailDeliveryResult.Status.DELIVERED.ordinal() : AntmailDeliveryResult.Status.FAILED.ordinal(), registration.valid() ? registration.address().fullAddress() : "", "", registration.reason(), "");
    }

    public static void handle(ServerPlayer player, AntmailStateRequestPayload payload) {
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
        result(player, payload.pos(), AntmailDeliveryResult.Status.DELIVERED.ordinal(), address.fullAddress(), "", "", AntmailWire.encodeTag(mailbox.toTag()));
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
            result(player, payload.pos(), delivery.status().ordinal(), recipient.fullAddress(), message.id().toString(), delivery.detail(), "");
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
            if (changed) data.setDirty();
            result(player, payload.pos(), changed ? AntmailDeliveryResult.Status.DELIVERED.ordinal() : AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), changed ? "" : "not_found", "");
        } catch (IllegalArgumentException exception) {
            result(player, payload.pos(), AntmailDeliveryResult.Status.FAILED.ordinal(), address.fullAddress(), payload.messageId(), "invalid_id", "");
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
        resultSender.accept(player, new AntmailResultPayload(pos, status, address, messageId, detail, data));
    }
}
