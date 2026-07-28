package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartAttackPayload;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartInteractPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
public final class MultipartNetworking {
    private MultipartNetworking() {
    }

    public static void register() {
        AntarchyForgeNetworkCore.registerC2S(MultipartAttackPayload.class, MultipartAttackPayload.STREAM_CODEC, MultipartNetworking::handleAttack);
        AntarchyForgeNetworkCore.registerC2S(MultipartInteractPayload.class, MultipartInteractPayload.STREAM_CODEC, MultipartNetworking::handleInteract);
    }

    private static void handleAttack(ServerPlayer serverPlayer, MultipartAttackPayload payload) {
        {
            Entity entity = serverPlayer.serverLevel().getEntity(payload.parentId());
            if (!(entity instanceof MultipartEntityOwner owner)) {
                return;
            }

            Entity[] parts = owner.antarchy$getMultipartParts();
            if (parts == null) {
                parts = owner.antarchy$createMultipartParts();
                owner.antarchy$setMultipartParts(parts);
                owner.antarchy$syncMultipartParts();
            }

            if (payload.partIndex() < 0 || payload.partIndex() >= parts.length) {
                return;
            }

            Entity part = parts[payload.partIndex()];
            if (part == null || part.isRemoved()) {
                return;
            }

            if (serverPlayer.distanceToSqr(part) > 10000.0D) {
                return;
            }

            DamageSource source = serverPlayer.level().damageSources().playerAttack(serverPlayer);
            owner.antarchy$hurtMultipartPart(part, source, payload.damage());
        }
    }

    private static void handleInteract(ServerPlayer serverPlayer, MultipartInteractPayload payload) {
        {
            Entity entity = serverPlayer.serverLevel().getEntity(payload.parentId());
            if (!(entity instanceof MultipartEntityOwner owner)) {
                return;
            }

            Entity[] parts = owner.antarchy$getMultipartParts();
            if (parts == null) {
                parts = owner.antarchy$createMultipartParts();
                owner.antarchy$setMultipartParts(parts);
                owner.antarchy$syncMultipartParts();
            }

            if (payload.partIndex() < 0 || payload.partIndex() >= parts.length) {
                return;
            }

            Entity part = parts[payload.partIndex()];
            if (part == null || part.isRemoved()) {
                return;
            }

            InteractionHand hand = payload.handId() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            owner.antarchy$interactMultipartPart(part, serverPlayer, part.position(), hand);
        }
    }
}
