package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.client.BloodglassClientState;
import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState;
import com.craisinlord.antarchy.content.client.ThoraxisWeatherClientState;
import com.craisinlord.antarchy.content.network.*;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import com.craisinlord.antarchy.fabric.client.BloodCrystalKatanaTrailClientState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class AntarchyFabricClientNetworking {
    private AntarchyFabricClientNetworking() {
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
    }
}
