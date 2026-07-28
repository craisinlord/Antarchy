package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.client.BloodglassClientState;
import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState;
import com.craisinlord.antarchy.content.client.ThoraxisWeatherClientState;
import com.craisinlord.antarchy.content.entity.multipart.MultipartFramework;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartAttackPayload;
import com.craisinlord.antarchy.content.entity.multipart.network.MultipartInteractPayload;
import com.craisinlord.antarchy.content.network.*;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import com.craisinlord.antarchy.fabric.client.BloodCrystalKatanaTrailClientState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class AntarchyFabricClientNetworking {
    private AntarchyFabricClientNetworking() {
    }

    public static void bootstrapMultipartClient() {
        MultipartFramework.bootstrap(
                AntarchyFabricNetworking::createMultipartPart,
                new MultipartFramework.NetworkBridge() {
                    @Override
                    public void sendAttack(java.util.UUID parentId, int partIndex, float damage) {
                        sendToServer(new MultipartAttackPayload(parentId, partIndex, damage), MultipartAttackPayload.STREAM_CODEC, MultipartAttackPayload.TYPE);
                    }

                    @Override
                    public void sendInteract(java.util.UUID parentId, int partIndex, int handId) {
                        sendToServer(new MultipartInteractPayload(parentId, partIndex, handId), MultipartInteractPayload.STREAM_CODEC, MultipartInteractPayload.TYPE);
                    }
                }
        );
    }

    public static <T extends com.craisinlord.antarchy.compat.network.CustomPacketPayload> void sendToServer(
            T payload,
            com.craisinlord.antarchy.compat.network.StreamCodec<io.netty.buffer.ByteBuf, T> streamCodec,
            com.craisinlord.antarchy.compat.network.CustomPacketPayload.Type<T> type
    ) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        streamCodec.encode(buf, payload);
        ClientPlayNetworking.send(type.id(), buf);
    }

    private static <T extends com.craisinlord.antarchy.compat.network.CustomPacketPayload> void registerReceiver(
            com.craisinlord.antarchy.compat.network.CustomPacketPayload.Type<T> type,
            com.craisinlord.antarchy.compat.network.StreamCodec<io.netty.buffer.ByteBuf, T> streamCodec,
            java.util.function.Consumer<T> handler
    ) {
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (client, handlerConn, buf, sender) -> {
            T payload = streamCodec.decode(buf);
            client.execute(() -> handler.accept(payload));
        });
    }

    public static void register() {
        registerReceiver(GravityStatePayload.TYPE, GravityStatePayload.STREAM_CODEC, payload ->
                AntarchyFabricNetworking.handleGravityState(Minecraft.getInstance().player, payload));
        registerReceiver(BloodglassStatePayload.TYPE, BloodglassStatePayload.STREAM_CODEC, payload ->
                BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax()));
        registerReceiver(BloodCrystalKatanaTrailPayload.TYPE, BloodCrystalKatanaTrailPayload.STREAM_CODEC, payload ->
                BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks()));
        registerReceiver(ScorpionWhipTetherPayload.TYPE, ScorpionWhipTetherPayload.STREAM_CODEC, payload ->
                ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId()));
        registerReceiver(BrutalflyElytraAnimationPayload.TYPE, BrutalflyElytraAnimationPayload.STREAM_CODEC, payload ->
                BrutalflyElytraClientState.trigger(payload.entityId(), payload.durationTicks(), payload.strength()));
        registerReceiver(ThoraxisWeatherPayload.TYPE, ThoraxisWeatherPayload.STREAM_CODEC, payload ->
                ThoraxisWeatherClientState.apply(
                        new ResourceLocation(payload.dimensionId()),
                        ThoraxisWeatherKind.byId(payload.weatherId()),
                        payload.expiresAt(),
                        payload.anchorX(),
                        payload.anchorY(),
                        payload.anchorZ()
                ));
        registerReceiver(HerculesBeetleImpactShakePayload.TYPE, HerculesBeetleImpactShakePayload.STREAM_CODEC, payload ->
                AntarchyFabricNetworking.triggerHerculesBeetleImpactShake(payload.durationTicks()));
        registerReceiver(ImpactShakePayload.TYPE, ImpactShakePayload.STREAM_CODEC, AntarchyFabricNetworking::triggerImpactShake);
        registerReceiver(HordeIntensityPayload.TYPE, HordeIntensityPayload.STREAM_CODEC, payload ->
                HordeClientState.update(payload.intensity()));
    }
}
