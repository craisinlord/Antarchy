package com.craisinlord.antarchy.content.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public final class AntmailPayloadTypes {
    private AntmailPayloadTypes() {
    }

    public static List<CustomPacketPayload.Type<?>> all() {
        return List.of(
                AntmailSetupPayload.TYPE,
                AntmailStateRequestPayload.TYPE,
                AntmailMessageRequestPayload.TYPE,
                AntmailSendPayload.TYPE,
                AntmailReadPayload.TYPE,
                AntmailDeletePayload.TYPE,
                AntmailDraftPayload.TYPE,
                AntmailRetryPayload.TYPE,
                AntmailResultPayload.TYPE
        );
    }
}
