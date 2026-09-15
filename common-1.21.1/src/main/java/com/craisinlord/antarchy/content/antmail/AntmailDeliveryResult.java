package com.craisinlord.antarchy.content.antmail;

import java.util.UUID;

public record AntmailDeliveryResult(Status status, UUID messageId, AntmailAddress sender, AntmailAddress recipient, String detail) {
    public enum Status {
        DELIVERED,
        QUEUED,
        INVALID_ADDRESS,
        ADDRESS_NOT_FOUND,
        MAILBOX_FULL,
        MESSAGE_INVALID,
        DUPLICATE,
        FAILED
    }

    public static AntmailDeliveryResult delivered(AntmailMessage message) {
        return new AntmailDeliveryResult(Status.DELIVERED, message.id(), message.sender(), message.recipient(), "");
    }

    public static AntmailDeliveryResult queued(AntmailMessage message) {
        return new AntmailDeliveryResult(Status.QUEUED, message.id(), message.sender(), message.recipient(), "");
    }

    public static AntmailDeliveryResult failed(Status status, AntmailMessage message, String detail) {
        return new AntmailDeliveryResult(status, message == null ? null : message.id(), message == null ? null : message.sender(), message == null ? null : message.recipient(), detail);
    }
}
