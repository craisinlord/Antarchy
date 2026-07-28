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
                        ClientPlayNetworking.send(new MultipartAttackPayload(parentId, partIndex, damage));
                    }

                    @Override
                    public void sendInteract(java.util.UUID parentId, int partIndex, int handId) {
                        ClientPlayNetworking.send(new MultipartInteractPayload(parentId, partIndex, handId));
                    }
                }
        );
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(GravityStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> AntarchyFabricNetworking.handleGravityState(context.player(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(BloodglassStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax())));
        ClientPlayNetworking.registerGlobalReceiver(BloodCrystalKatanaTrailPayload.TYPE, (payload, context) ->
                context.client().execute(() -> BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(ScorpionWhipTetherPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId())));
        ClientPlayNetworking.registerGlobalReceiver(BrutalflyElytraAnimationPayload.TYPE, (payload, context) ->
                context.client().execute(() -> BrutalflyElytraClientState.trigger(payload.entityId(), payload.durationTicks(), payload.strength())));
        ClientPlayNetworking.registerGlobalReceiver(ThoraxisWeatherPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ThoraxisWeatherClientState.apply(
                        ResourceLocation.parse(payload.dimensionId()),
                        ThoraxisWeatherKind.byId(payload.weatherId()),
                        payload.expiresAt(),
                        payload.anchorX(),
                        payload.anchorY(),
                        payload.anchorZ()
                )));
        ClientPlayNetworking.registerGlobalReceiver(HerculesBeetleImpactShakePayload.TYPE, (payload, context) ->
                context.client().execute(() -> AntarchyFabricNetworking.triggerHerculesBeetleImpactShake(payload.durationTicks())));
        ClientPlayNetworking.registerGlobalReceiver(ImpactShakePayload.TYPE, (payload, context) ->
                context.client().execute(() -> AntarchyFabricNetworking.triggerImpactShake(payload)));
        ClientPlayNetworking.registerGlobalReceiver(HordeIntensityPayload.TYPE, (payload, context) ->
                context.client().execute(() -> HordeClientState.update(payload.intensity())));
    }
}
